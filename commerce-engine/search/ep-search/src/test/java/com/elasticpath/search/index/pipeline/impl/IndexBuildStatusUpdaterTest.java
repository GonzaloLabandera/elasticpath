/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.search.index.pipeline.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.search.IndexBuildStatus;
import com.elasticpath.domain.search.impl.IndexBuildStatusImpl;
import com.elasticpath.persistence.dao.IndexBuildStatusDao;
import com.elasticpath.search.index.pipeline.impl.IndexBuildStatusUpdater.GuardedIndexBuildStatusDao;
import com.elasticpath.search.index.pipeline.impl.IndexBuildStatusUpdater.GuardedIndexBuildStatusDao.OperationHistory;
import com.elasticpath.service.search.IndexType;

/**
 * IndexBuildStatusUpdater does it work in a separate thread, and in production would be processing multiple threads, thereby making it
 * difficult to write meaningful tests for.
 * the tests included here are more 'exercising' tests that can be used by a develop as a starting point to debug problems if needed.
 * @author acanterla
 *
 */
public class IndexBuildStatusUpdaterTest {

	private static final int TEN_SECONDS = 10000;
	private static final int TWO_SECONDS = 2000;

	private static final int NUM_OF_THREADS = 20;

	private static final int RANDOM_NUMBER_OF_SUCCESSFUL_OPERATIONS = 20;


	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	
	private IndexBuildStatusUpdater indexBuildStatusUpdater;
	
	private IndexBuildStatusDao mockIndexBuildStatusDao;
	
	private IndexBuildStatus indexBuildStatus;
	
	private IndexBuildStatus indexBuildStatusThrowsException;

	private GuardedIndexBuildStatusDao circuitBreaker;

	private OperationHistory operationHistory;

	@Before
	public void setUp() {
		// To ensure JUnitRuleMockery thread safety
		context.setThreadingPolicy(new Synchroniser());

		mockIndexBuildStatusDao = context.mock(IndexBuildStatusDao.class, "mockIndexBuildStatusDao");
		indexBuildStatus = context.mock(IndexBuildStatus.class, "indexBuildStatus");
		indexBuildStatusThrowsException = context.mock(IndexBuildStatus.class, "indexBuildStatusthrowsException");
		
		indexBuildStatusUpdater = new IndexBuildStatusUpdater();
		indexBuildStatusUpdater.setIndexBuildStatusDao(mockIndexBuildStatusDao);
		indexBuildStatusUpdater.initialize();

		circuitBreaker = new GuardedIndexBuildStatusDao(mockIndexBuildStatusDao);
		operationHistory = new OperationHistory();

		context.checking(new Expectations() {
			{
				allowing(indexBuildStatus).getIndexType();
				will(returnValue(IndexType.PRODUCT));
				
				allowing(mockIndexBuildStatusDao).get(IndexType.PRODUCT);
				will(returnValue(null));
				
				allowing(mockIndexBuildStatusDao).get(IndexType.CATEGORY);
				will(returnValue(null));

				allowing(mockIndexBuildStatusDao).saveOrUpdate(with(any(IndexBuildStatus.class)));
				will(throwException(new EpServiceException("epServiceException")));
			}
		});
	}

	@Test
	public void testCircuitBreaker() {
		// Assure Updater is out of Degraded Mode
		assertFalse(circuitBreaker.isInDegradedMode());

		recordActivity(GuardedIndexBuildStatusDao.SUCCESS, RANDOM_NUMBER_OF_SUCCESSFUL_OPERATIONS);

		recordActivity(GuardedIndexBuildStatusDao.ERROR, GuardedIndexBuildStatusDao.ERRORS_TO_CONSIDER_AS_FAILING);
		// Assure Updater is in Degraded Mode
		assertTrue(circuitBreaker.isInDegradedMode());

		recordActivity(GuardedIndexBuildStatusDao.SUCCESS, GuardedIndexBuildStatusDao.SUCCESS_TO_CONSIDER_AS_RECOVERING - 1);
		// Assure Updater is still in Degraded Mode
		assertFalse(circuitBreaker.isInDegradedMode());

		recordActivity(GuardedIndexBuildStatusDao.SUCCESS, 1);
		// Assure Updater is now out of Degraded Mode
		assertFalse(circuitBreaker.isInDegradedMode());
	}

	protected void recordActivity(final boolean operationType, final int quantity) {
		int count = 0;
		if (operationType == GuardedIndexBuildStatusDao.SUCCESS) {
			while (count <= quantity) {
				circuitBreaker.record(operationType);
				count++;
			}
		} else if (operationType == GuardedIndexBuildStatusDao.ERROR) {
			while (count <= quantity) {
				circuitBreaker.record(operationType);
				count++;
			}
		}
	}

	@After
	public void tearDown() {
		indexBuildStatusUpdater.shutdown();
	}
	
	@Test
	public void testIndexBuildStatusUpdater() throws InterruptedException {

		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.submit(simulateIndexerThread(i));
		}
		
		Thread.sleep(TWO_SECONDS);
		
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.submit(simulateIndexerThread(i));
		}
		
		threadPool.shutdown();
		//give the IndexStatusUpdater a chance to run for a while.
		//if we dont do this the test ends and all the threads go away.
		threadPool.awaitTermination(TEN_SECONDS, TimeUnit.SECONDS);
	}
	
	@Test
	public void testIndexBuildStatusUpdaterWithExceptionThrown() throws InterruptedException {
		context.checking(new Expectations() {
			{
				allowing(indexBuildStatusThrowsException).getIndexType();
				will(returnValue(IndexType.CATEGORY));

				allowing(indexBuildStatusThrowsException).getUidPk();
				will(returnValue(Long.MIN_VALUE));

				allowing(mockIndexBuildStatusDao).saveOrUpdate(indexBuildStatusThrowsException);
				will(throwException(new EpServiceException("epServiceException")));
			}
		});

		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		
		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.submit(simulateIndexerThread(i));
		}
		
		Thread.sleep(TWO_SECONDS);
		
		threadPool.submit(simulateIndexerThreadThrowsException());

		for (int i = 0; i < NUM_OF_THREADS; i++) {
			threadPool.submit(simulateIndexerThread(i));
		}
		
		threadPool.shutdown();
		//give the IndexStatusUpdater a chance to run for a while. 
		//if we dont do this the test ends and all the threads go away.
		threadPool.awaitTermination(TEN_SECONDS, TimeUnit.SECONDS);
	}
	
	private Runnable simulateIndexerThread(final int simpleGuid) {
		return new Runnable() {
			@Override
			public void run() {

				IndexBuildStatus status = new IndexBuildStatusImpl();
				status.setIndexType(IndexType.PRODUCT);
				status.setUidPk(simpleGuid);
				
				indexBuildStatusUpdater.enqueue(status);
			}
		};
	}
	
	private Runnable simulateIndexerThreadThrowsException() {
		return new Runnable() {
			@Override
			public void run() {
				indexBuildStatusUpdater.enqueue(indexBuildStatusThrowsException);
			}
		};
	}

	@Test
	public void matchesPatternTest() {
		boolean[] pattern = new boolean[OperationHistory.MAX_NUMBER_OF_OPERATIONS_TO_TRACK];
		Arrays.fill(pattern, false);

		boolean result = operationHistory.matchesPattern(pattern);
		assertTrue("Successfully Completed!", result);
	}

	@Test(expected = IllegalStateException.class)
	public void matchesPatternThrowsExceptionTest() {
		boolean[] pattern = new boolean[OperationHistory.MAX_NUMBER_OF_OPERATIONS_TO_TRACK + 1];
		Arrays.fill(pattern, false);

		operationHistory.matchesPattern(pattern);
	}

}
