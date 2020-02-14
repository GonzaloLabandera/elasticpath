/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.test.integration.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.search.query.SearchTerms;
import com.elasticpath.domain.search.query.SearchTermsActivity;
import com.elasticpath.domain.search.query.SearchTermsMemento.SearchTermsId;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.search.query.SearchTermsActivityStrategy;
import com.elasticpath.service.search.query.impl.SearchTermsServiceImpl;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration tests for {@link com.elasticpath.service.search.query.impl.SearchTermsServiceImpl}.
 */
public class SearchTermsServiceAuditImplTest extends BasicSpringContextTest {
	private static final String KEYWORDS1 = "keyword1 keyword2";

	@Autowired
	private SearchTermsServiceImpl searchTermsService;
	
	@Autowired
	@Qualifier("loggingSearchTermsActivityStrategy")
	private SearchTermsActivityStrategy searchTermsActivityStrategy;

	@Autowired
	private PersistenceEngine persistenceEngine;

	/**
	 * Sets up each test.
	 */
	@Before
	public void setUp() {
		searchTermsService.setSearchTermsActivityStrategy(searchTermsActivityStrategy);
	}

	private SearchTerms createSearchTerms(final String keywords) {
		SearchTerms searchTerms = getBeanFactory().getPrototypeBean(ContextIdNames.SEARCH_TERMS, SearchTerms.class);
		searchTerms.setKeywords(keywords);
		return searchTerms;
	}

	/**
	 * Activity should be logged when saving.
	 */
	@DirtiesDatabase
	@Test
	public void testActivityLoggedSave() {
		SearchTerms searchTerms1 = createSearchTerms(KEYWORDS1);
		SearchTermsId id1 = searchTermsService.saveIfNotExists(searchTerms1);

		List<SearchTermsActivity> searchActivity = getSearchTermsActivity(id1);
		assertThat(searchActivity)
			.as("Activity should have a single log entry")
			.hasSize(1);
	}

	private List<SearchTermsActivity> getSearchTermsActivity(final SearchTermsId identifier) {
		return persistenceEngine.retrieve("SELECT s FROM SearchTermsActivityImpl s WHERE s.searchTerms.guid = ?1", identifier.getValue());
	}

	/**
	 * Activity should be logged even if a memento already exists for a search term.
	 */
	@DirtiesDatabase
	@Test
	public void testActivityLoggedSaveTwice() {
		SearchTerms searchTerms1 = createSearchTerms(KEYWORDS1);
		SearchTermsId id1 = searchTermsService.saveIfNotExists(searchTerms1);
		SearchTerms searchTerms2 = createSearchTerms(KEYWORDS1);
		SearchTermsId id2 = searchTermsService.saveIfNotExists(searchTerms2);
		assertThat(id2)
			.as("Equal keywords for search terms should give back the same ID")
			.isEqualTo(id1);

		List<SearchTermsActivity> searchActivity = getSearchTermsActivity(id1);
		assertThat(searchActivity)
			.as("2 saves should have 2 log entries")
			.hasSize(2);
	}

	/**
	 * Activity should be logged when a load occurs.
	 */
	@DirtiesDatabase
	@Test
	public void testActivityLoggedLoad() {
		SearchTerms searchTerms1 = createSearchTerms(KEYWORDS1);
		SearchTermsId id1 = searchTermsService.saveIfNotExists(searchTerms1);

		List<SearchTermsActivity> searchActivity = getSearchTermsActivity(id1);
		assertThat(searchActivity)
			.as("1 save should only have a single log entry")
			.hasSize(1);

		searchTermsService.load(id1);
		searchActivity = getSearchTermsActivity(id1);
		assertThat(searchActivity)
			.as("Load didn't trigger a log entry")
			.hasSize(2);
	}
}
