/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.search.index.pipeline.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;

import com.elasticpath.domain.search.IndexBuildStatus;
import com.elasticpath.domain.search.impl.IndexBuildStatusImpl;
import com.elasticpath.persistence.dao.IndexBuildStatusDao;
import com.elasticpath.search.index.grouper.impl.UidGroupingTaskImpl;
import com.elasticpath.search.index.pipeline.AbstractIndexingTask;
import com.elasticpath.search.index.pipeline.DocumentCreatingTask;
import com.elasticpath.search.index.pipeline.EntityLoadingTask;
import com.elasticpath.search.index.pipeline.IndexingStage;
import com.elasticpath.search.index.pipeline.stats.impl.IndexingStatsImpl;
import com.elasticpath.search.index.pipeline.stats.impl.PipelineStatusImpl;
import com.elasticpath.search.index.solr.service.impl.TestSolrDocumentPublisher;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.IndexType;

/**
 * Wire up an {@code IndexingPipelineImpl} and run some flow tests through it.
 */
@SuppressWarnings({ "PMD.TooManyStaticImports" })
public class IndexingPipelineImplTest {
	private static final IndexType INDEX_TYPE = IndexType.PRODUCT;
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private IndexingPipelineImpl indexingPipeline;

	private IndexingStatsImpl stats;

	private TestSolrDocumentPublisher testPublisher;

	private TimeService timeService;
	private IndexBuildStatusDao indexBuildStatusDao;
	private final IndexBuildStatusUpdater indexBuildStatusUpdater = new IndexBuildStatusUpdater();

	/**
	 * Here is our fake "Entity". We lie to the rest of the pipeline and call it IndexType.PRODUCT.
	 */
	class Payload {
		private final long uid;

		/**
		 * Herein we hold the unique number.
		 *
		 * @param uid a number with a varying degree of uniqueness.
		 */
		@SuppressWarnings("checkstyle:redundantmodifier")
		public Payload(final long uid) {
			this.uid = uid;
		}

		public long getUid() {
			return uid;
		}
	}

	/**
	 * Fake {@code DocumentCreatingTask} which simply puts the "uid" in a {@code SolrInputDocument}.
	 */
	class PayloadDocumentCreator extends AbstractIndexingTask<SolrInputDocument> implements
			DocumentCreatingTask<Payload> {

		private Payload payload;

		@Override
		public void run() {
			final SolrInputDocument sid = new SolrInputDocument();
			sid.setField("uid", payload.getUid());
			getNextStage().send(sid);
		}

		@Override
		public void setEntity(final Payload payload) {
			this.payload = payload;

		}

	}

	/**
	 * Totally fake loader that just iterates through the batch and hands it new {@code Payload} objects.
	 */
	class BatchPayloadLoader extends AbstractIndexingTask<Payload> implements EntityLoadingTask<Payload> {

		private Set<Long> payload;

		@Override
		public void run() {
			for (final Long identifier : payload) {
				getNextStage().send(new Payload(identifier));
			}
		}

		@Override
		public void setBatch(final Set<Long> payload) {
			this.payload = payload;
		}
	}

	/**
	 * Create a {@code IndexingPipelineImpl} with proper {@code IndexingStage}s, but with fake {@code IndexingTask}s.
	 *
	 * @return a manually configured, synchronous pipeline.
	 */
	private IndexingPipelineImpl makePipeline() {
		final IndexingPipelineImpl pipeline = new IndexingPipelineImpl();

		pipeline.setIndexingStatistics(stats);
		pipeline.setIndexType(INDEX_TYPE);

		final UidGroupingStage groupingStage = new UidGroupingStage();
		groupingStage.setTaskExecutor(new SyncTaskExecutor());
		groupingStage.setIndexGroupingTaskFactory(UidGroupingTaskImpl::new);

		final EntityLoadingStage<Payload> loadingStage = new EntityLoadingStage<>();
		loadingStage.setTaskExecutor(new SyncTaskExecutor());

		loadingStage.setLoaderFactory(BatchPayloadLoader::new);

		final DocumentCreatingStage<Payload> documentCreatingStage;
		documentCreatingStage = new DocumentCreatingStage<>();
		documentCreatingStage.setDocumentCreatorFactory(PayloadDocumentCreator::new);
		documentCreatingStage.setTaskExecutor(new SyncTaskExecutor());

		final DocumentPublishingStage documentPublishingStage;
		documentPublishingStage = new DocumentPublishingStage();
		documentPublishingStage.setIndexType(INDEX_TYPE);

		testPublisher = new TestSolrDocumentPublisher();
		documentPublishingStage.setDocumentPublisher(testPublisher);
		documentPublishingStage.setIndexType(INDEX_TYPE);

		@SuppressWarnings("unchecked")
		List<IndexingStage<?, ?>> stages = Arrays.asList(groupingStage, loadingStage, documentCreatingStage, documentPublishingStage);

		pipeline.setStages(stages);

		indexBuildStatusDao = context.mock(IndexBuildStatusDao.class);
		timeService = context.mock(TimeService.class);
		context.checking(new Expectations() {
			{
				allowing(timeService).getCurrentTime();
				will(returnValue(new Date()));
			}
		});
		indexBuildStatusUpdater.setIndexBuildStatusDao(indexBuildStatusDao);
		indexBuildStatusUpdater.initialize();
		
		pipeline.setIndexBuildStatusUpdater(indexBuildStatusUpdater);
		pipeline.setTimeService(timeService);
		pipeline.initialize();

		return pipeline;
	}

	/**
	 * Create a new pipeline before each test.
	 */
	@Before
	public void createPipeline() {

		stats = new IndexingStatsImpl();
		indexingPipeline = makePipeline();

	}

	/**
	 * This is a bad test, but it tests that things flow correctly through the pipeline and set the in/out count properly. It leverages that to also
	 * test resetting stats.
	 */
	@Test
	public void runMyBigFakePipeline() {
		final Collection<Long> numbers = createNumbers();

		int docsIn = numbers.size();

		indexingPipeline.start(numbers);

		assertEquals(docsIn, testPublisher.getUpdated().size());
		assertEquals(docsIn, stats.getPipelineStatus(INDEX_TYPE).getCompletedCount());
		assertEquals(docsIn, stats.getPipelineStatus(INDEX_TYPE).getIncomingCount());
		stats.reset();
		assertEquals(0, stats.getPipelineStatus(INDEX_TYPE).getCompletedCount());
		assertEquals(0, stats.getPipelineStatus(INDEX_TYPE).getIncomingCount());
	}

	/**
	 * Toss some things into the pipeline and then run the periodicMonitor and ensure it calls {@code PipelineStatus#notifyCompleted()}.
	 */
	@Test
	public void testPeriodicMonitorNotifiesOnCompletion() {
		final Collection<Long> numbers = createNumbers();

		/**
		 * Captures the notification to see if it's fired.
		 */
		class TestPipelineStatus extends PipelineStatusImpl {
			private boolean notified;

			@Override
			public void notifyCompleted() {
				notified = true;
			}

			/**
			 * Was {@code #notifyCompleted} called?
			 *
			 * @return true if it was called at least once.
			 */
			public boolean wasNotified() {
				return notified;
			}

		}
		TestPipelineStatus testStatus = new TestPipelineStatus();
		stats.attachPipelineStatus(INDEX_TYPE, testStatus);

		indexingPipeline.start(numbers);
		indexingPipeline.periodicMonitor();
		assertTrue(testStatus.wasNotified());
	}

	/**
	 * Tossing more work into a shutdown pipeline must cause an exception.
	 */
	@Test(expected = IllegalStateException.class)
	public void testRejectOnShutdown() {

		Collection<Long> numbers = createNumbers();

		indexingPipeline.destroy();
		indexingPipeline.start(numbers);

	}

	/** The index build status should be updated before and after all records are processed. */
	@Test
	public void testBuildStatusUpdated() {
		final IndexBuildStatus status = new IndexBuildStatusImpl();
		status.setIndexType(INDEX_TYPE);
		stats.attachIndexBuildStatus(INDEX_TYPE, status);
		context.checking(new Expectations() {
			{
				allowing(indexBuildStatusDao).saveOrUpdate(status);
				will(returnValue(status));
			}
		});
		
		final Collection<Long> numbers = createNumbers();
		indexingPipeline.start(numbers);
		assertEquals("Total records should be setup after start", numbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getTotalRecords());
		assertEquals("Processed records start at 0", 0, stats.getIndexBuildStatus(INDEX_TYPE).getProcessedRecords());

		// everything happens synchronously, so its safe to say all records have been processed
		indexingPipeline.destroy();
		assertEquals("Total shouldn't change", numbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getTotalRecords());
		assertEquals("All records should be finished", numbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getProcessedRecords());
	}

	/** When building more than once, the status should be reset, but performance stats should not. */
	@Test
	public void testBuildTwiceStatusReset() {
		final IndexBuildStatus status = new IndexBuildStatusImpl();
		status.setIndexType(INDEX_TYPE);
		stats.attachIndexBuildStatus(INDEX_TYPE, status);
		context.checking(new Expectations() {
			{
				allowing(indexBuildStatusDao).saveOrUpdate(status);
				will(returnValue(status));
			}
		});
		
		// first run
		final Collection<Long> numbers = createNumbers();
		indexingPipeline.start(numbers);
		indexingPipeline.periodicMonitor();
		assertEquals("Total should be setup", numbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getTotalRecords());
		assertEquals("All records should be finished", numbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getProcessedRecords());
		assertEquals("Performance stats should be the same on first run", numbers.size(), stats.getPipelineStatus(INDEX_TYPE)
				.getCompletedCount());
		assertEquals("Performance stats should be the same on first run", numbers.size(), stats.getPipelineStatus(INDEX_TYPE)
				.getIncomingCount());

		// second run
		Collection<Long> newNumbers = new ArrayList<>(numbers);
		newNumbers.add(1L);

		indexingPipeline.start(newNumbers);
		assertEquals("Total should be reset to the new size", newNumbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getTotalRecords());
		assertEquals("We should start at 0", 0, stats.getIndexBuildStatus(INDEX_TYPE).getProcessedRecords());

		indexingPipeline.destroy();
		assertEquals("Total should not change", newNumbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getTotalRecords());
		assertEquals("All should be finished", newNumbers.size(), stats.getIndexBuildStatus(INDEX_TYPE).getProcessedRecords());
		assertEquals("Performance stats should not be reset", numbers.size() + newNumbers.size(), stats.getPipelineStatus(INDEX_TYPE)
				.getCompletedCount());
		assertEquals("Performance stats should not be reset", numbers.size() + newNumbers.size(), stats.getPipelineStatus(INDEX_TYPE)
				.getIncomingCount());
	}

	/**
	 * New pipelines must not be busy after creation.
	 */
	@Test
	public void testIsBusy() {
		assertFalse(indexingPipeline.isBusy());
	}

	/**
	 * A simple test to ensure construction works.
	 */
	@Test
	public void testCreation() {
		assertNotNull(indexingPipeline);

	}

	private Collection<Long> createNumbers() {
		// CHECKSTYLE:OFF -- there's nothing magical about these numbers.
		final Collection<Long> numbers = new ArrayList<>();
		numbers.add(123L);
		numbers.add(456L);
		numbers.add(789L);
		numbers.add(101112L);
		// CHECKSTYLE:ON

		return numbers;

	}
}
