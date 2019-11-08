/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.batch.jobs.impl;

import static com.elasticpath.batch.jobs.impl.SearchTermsAggregatorJobImplTest.JMS_BROKER_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.query.SearchTerms;
import com.elasticpath.domain.search.query.SearchTermsActivity;
import com.elasticpath.domain.search.query.SearchTermsActivitySummary;
import com.elasticpath.domain.search.query.SearchTermsMemento.SearchTermsId;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.search.query.SearchTermsService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.junit.DatabaseHandlingTestExecutionListener;
import com.elasticpath.test.jta.JmsBrokerConfigurator;
import com.elasticpath.test.jta.XaTransactionTestSupport;
import com.elasticpath.test.persister.TestApplicationContext;
import com.elasticpath.test.support.junit.JmsRegistrationTestExecutionListener;

@ContextConfiguration("/integration-context.xml")
@TestExecutionListeners({
		JmsRegistrationTestExecutionListener.class,
		DatabaseHandlingTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class
})
@JmsBrokerConfigurator(url = JMS_BROKER_URL)
public class SearchTermsAggregatorJobImplTest extends XaTransactionTestSupport {

	public static final String JMS_BROKER_URL = "tcp://localhost:61622";

	private static final String KEYWORDS1 = "keyword1 keyword2";
	private static final String KEYWORDS2 = "keyword3 keyword4";
	private static final long TWO_SECONDS_IN_MILLISECONDS = 2000L;
	private static final int SEARCH_TERMS_COUNT = 10;

	@Autowired
	private TestApplicationContext tac;
	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	@Qualifier("searchTermsService")
	private SearchTermsService searchTermsService;
	@Autowired
	@Qualifier(ContextIdNames.PERSISTENCE_ENGINE)
	private PersistenceEngine persistenceEngine;
	
	private SearchTermsAggregatorJobImpl searchTermsAggregatorJob;

	@Before
	public void setUp() {
		TimeService timeService = beanFactory.getBean("timeService");

		searchTermsAggregatorJob = new SearchTermsAggregatorJobImpl();
		searchTermsAggregatorJob.setPersistenceEngine(persistenceEngine);
		searchTermsAggregatorJob.setTimeService(timeService);
	}

	private void updateSearchTermsActivity() {
		Uninterruptibles.sleepUninterruptibly(TWO_SECONDS_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
		/*
		 * Since we instantiate searchTermsAggregatorJob (instead of having spring do it), it won't be wrapped in a
		 * transaction proxy --- help it out.
		 */
		tac.getTxTemplate().execute(txStatus -> {
			searchTermsAggregatorJob.updateSearchTermsActivity();
			return null;
		});
	}

	/**
	 * Test aggregation of counts with a single log entry.
	 */
	@Test
	@DirtiesDatabase
	public void singleSearch() {
		SearchTerms searchTerms = createSearchTerms(KEYWORDS1);
		SearchTermsId identifier = searchTermsService.saveIfNotExists(searchTerms);

		List<SearchTermsActivity> searchTermsActivity = getSearchTermsActivity(identifier);
		assertThat(searchTermsActivity)
			.as("Single search log expected")
			.hasSize(1);

		updateSearchTermsActivity();

		SearchTermsActivitySummary summary = getSearchTermsActivitySummary(identifier);
		assertThat(summary)
			.as("Summary not updated!")
			.isNotNull();
		assertThat(summary.getSearchCount())
			.as("Count didn't update")
			.isEqualTo(1);
		assertThat(summary.getLastAccessDate())
			.as("Dates don't match!")
			.isEqualTo(searchTermsActivity.get(0).getLastAccessDate());
	}

	/**
	 * Test aggregation of counts with multiple log entries.
	 */
	@Test
	@DirtiesDatabase
	public void multipleSearchesSingleTerm() {
		SearchTerms searchTerms = createSearchTerms(KEYWORDS1);
		SearchTermsId identifier = searchTermsService.saveIfNotExists(searchTerms);
		searchTermsService.load(identifier);

		List<SearchTermsActivity> searchTermsActivity = getSearchTermsActivity(identifier);
		assertThat(searchTermsActivity)
			.as("2 search logs expected")
			.hasSize(2);

		updateSearchTermsActivity();

		SearchTermsActivitySummary summary = getSearchTermsActivitySummary(identifier);
		assertThat(summary)
			.as("Summary not updated!")
			.isNotNull();
		assertThat(summary.getSearchCount())
			.as("Count didn't update after save then load")
			.isEqualTo(2);
		assertThat(summary.getLastAccessDate())
			.as("Incorrect date updated")
			.isEqualTo(searchTermsActivity.get(1).getLastAccessDate());
	}

	/**
	 * Test aggregation of counts with multiple log entries with more than one search terms.
	 */
	@Test
	@DirtiesDatabase
	public void multipleSearchesMultipleTerms() {
		SearchTerms searchTerms = createSearchTerms(KEYWORDS1);
		SearchTerms searchTerms2 = createSearchTerms(KEYWORDS2);
		SearchTermsId identifier = searchTermsService.saveIfNotExists(searchTerms);
		searchTermsService.saveIfNotExists(searchTerms2);
		searchTermsService.load(identifier);

		List<SearchTermsActivity> searchTermsActivity = getSearchTermsActivity(identifier);
		assertThat(searchTermsActivity)
			.as("2 search logs expected")
			.hasSize(2);

		updateSearchTermsActivity();

		SearchTermsActivitySummary summary = getSearchTermsActivitySummary(identifier);
		assertThat(summary)
			.as("Summary not updated!")
			.isNotNull();
		assertThat(summary.getSearchCount())
			.as("Count didn't update after save then load")
			.isEqualTo(2);
		assertThat(summary.getLastAccessDate())
			.as("Incorrect date updated")
			.isEqualTo(searchTermsActivity.get(1).getLastAccessDate());
	}

	/**
	 * Test aggregation of counts with multiple log entries when a count needs to be updated.
	 */
	@Test
	@DirtiesDatabase
	public void singleMultipleUpdate() {
		SearchTerms searchTerms = createSearchTerms(KEYWORDS1);
		SearchTermsId identifier = searchTermsService.saveIfNotExists(searchTerms);

		List<SearchTermsActivity> searchTermsActivity = getSearchTermsActivity(identifier);
		assertThat(searchTermsActivity)
			.as("Single search log expected")
			.hasSize(1);

		updateSearchTermsActivity();

		searchTermsService.load(identifier);
		searchTermsActivity = getSearchTermsActivity(identifier);
		assertThat(searchTermsActivity)
			.as("One search log expected")
			.hasSize(1);

		updateSearchTermsActivity();

		SearchTermsActivitySummary summary = getSearchTermsActivitySummary(identifier);
		assertThat(summary)
			.as("Summary not updated!")
			.isNotNull();
		assertThat(summary.getSearchCount())
			.as("Count didn't update after save then load")
			.isEqualTo(2);
		assertThat(summary.getLastAccessDate())
			.as("Incorrect date updated")
			.isEqualTo(searchTermsActivity.get(0).getLastAccessDate());
	}

	/**
	 * Test aggregation of counts with multiple log entries when a count needs to be updated when there is more than a
	 * single search term.
	 */
	@Test
	@DirtiesDatabase
	public void singleMultipleUpdateMultipleTerms() {
		SearchTerms searchTerms = createSearchTerms(KEYWORDS1);
		SearchTerms searchTerms2 = createSearchTerms(KEYWORDS2);
		SearchTermsId identifier = searchTermsService.saveIfNotExists(searchTerms);
		SearchTermsId identifier2 = searchTermsService.saveIfNotExists(searchTerms2);

		List<SearchTermsActivity> searchTermsActivity1 = getSearchTermsActivity(identifier);
		List<SearchTermsActivity> searchTermsActivity2 = getSearchTermsActivity(identifier2);

		assertThat(searchTermsActivity1)
			.as("Single search log expected")
			.hasSize(1);

		updateSearchTermsActivity();

		searchTermsService.load(identifier);
		searchTermsActivity1 = getSearchTermsActivity(identifier);
		assertThat(searchTermsActivity1)
			.as("One search log expected")
			.hasSize(1);

		updateSearchTermsActivity();

		SearchTermsActivitySummary summary = getSearchTermsActivitySummary(identifier);
		assertThat(summary)
			.as("Summary not updated!")
			.isNotNull();
		assertThat(summary.getSearchCount())
			.as("Count didn't update after save then load")
			.isEqualTo(2);
		assertThat(summary.getLastAccessDate())
			.as("Incorrect date updated")
			.isEqualTo(searchTermsActivity1.get(0).getLastAccessDate());

		SearchTermsActivitySummary summary2 = getSearchTermsActivitySummary(identifier2);
		assertThat(summary2)
			.as("Summary not updated!")
			.isNotNull();
		assertThat(summary2.getSearchCount())
			.as("Count wasn't accurate")
			.isEqualTo(1);
		assertThat(summary2.getLastAccessDate())
			.as("Incorrect date updated")
			.isEqualTo(searchTermsActivity2.get(0).getLastAccessDate());
	}

	@DirtiesDatabase
	@Test
	public void testCleanup() {
		SearchTerms searchTerms = createSearchTerms(KEYWORDS1);
		SearchTermsId identifier = searchTermsService.saveIfNotExists(searchTerms);
		for (int i = 0; i < SEARCH_TERMS_COUNT; ++i) {
			searchTermsService.load(identifier);
		}

		assertThat(getSearchTermsActivity(identifier).size())
			.as("There should be a bunch of activities.")
			.isGreaterThan(SEARCH_TERMS_COUNT);
		updateSearchTermsActivity();
		assertThat(getSearchTermsActivity(identifier))
			.as("Old activity should be cleaned up.")
			.isEmpty();
	}

	private SearchTermsActivitySummary getSearchTermsActivitySummary(final SearchTermsId identifier) {
		// Need to wrap the call in a transaction as the workaround for TSR-372 to avoid a ClassCastException in the persitence engine evict call
		return tac.getTxTemplate().execute(
				txStatus -> {
					List<SearchTermsActivitySummary> result = persistenceEngine.retrieve(
							"SELECT s FROM SearchTermsActivitySummaryImpl s WHERE s.searchTermsInternal.guid = ?1", identifier.getValue());
					if (result == null || result.isEmpty()) {
						return null;
					}
					return result.get(0);
				});
	}

	private List<SearchTermsActivity> getSearchTermsActivity(final SearchTermsId identifier) {
		// Need to wrap the call in a transaction as the workaround for TSR-372 to avoid a ClassCastException in the persistence engine evict call
		return tac.getTxTemplate().execute(
				txStatus -> persistenceEngine.retrieve(
						"SELECT s FROM SearchTermsActivityImpl s WHERE s.searchTerms.guid = ?1 ORDER BY s.lastAccessDate", identifier.getValue()));
	}

	private SearchTerms createSearchTerms(final String keywords) {
		SearchTerms searchTerms = beanFactory.getBean(ContextIdNames.SEARCH_TERMS);
		searchTerms.setKeywords(keywords);
		return searchTerms;
	}
}
