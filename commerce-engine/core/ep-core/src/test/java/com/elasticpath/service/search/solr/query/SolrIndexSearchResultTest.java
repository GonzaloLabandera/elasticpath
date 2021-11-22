/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.search.solr.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.util.StringUtils;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.BrandFilter;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.FilterOption;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.impl.FilterOptionImpl;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.SolrIndexSearcherImpl;

/**
 * Test case for {@link SolrIndexSearchResult}.
 */
public class SolrIndexSearchResultTest {

	private SolrIndexSearchResult solrIndexSearchResult;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private Searcher mockSearcher;

	private TestSolrSearcher testSolrSearcher;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception in case of any errors
	 */
	@Before
	public void setUp() throws Exception {
		solrIndexSearchResult = new SolrIndexSearchResult();
		testSolrSearcher = new TestSolrSearcher(beanFactory);
		solrIndexSearchResult.setIndexSearcher(testSolrSearcher);

		mockSearcher = context.mock(Searcher.class);
		testSolrSearcher.searcher = mockSearcher;
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getResults(int, int)}.
	 */
	@Test
	public void testGetResults() {
		final int startIndex = 0;
		final int maxResults = 10;
		context.checking(new Expectations() {
			{
				oneOf(mockSearcher).search(startIndex, maxResults);
			}
		});
		solrIndexSearchResult.getResults(startIndex, maxResults);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getResults(int, int)} where we fall back to
	 * due to an empty page. This specifically tests where one of the fallback pages will have a
	 * start index that is negative (searches should not have negative start indexes).
	 */
	@Test
	public void testGetResultsFallback() {
		final int startIndex = 6; // needs to be above 5 for testing, but lower than 10
		final int maxResults = 10;
		context.checking(new Expectations() {
			{
				oneOf(mockSearcher).search(startIndex, maxResults);
			}
		});
		solrIndexSearchResult.getResults(startIndex, maxResults);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getNumFound()}.
	 */
	@Test
	public void testNumFound() {
		final int startIndex = 0;
		final int maxResults = 0;
		context.checking(new Expectations() {
			{
				oneOf(mockSearcher).search(startIndex, maxResults);
			}
		});
		solrIndexSearchResult.getNumFound();
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getLastNumFound()}.
	 */
	@Test
	public void testGetLastNumFound() {
		final int startIndex = 0;
		final int maxResults = 0;
		context.checking(new Expectations() {
			{
				oneOf(mockSearcher).search(startIndex, maxResults);
			}
		});
		solrIndexSearchResult.getLastNumFound();

		// a second (or more attempts) should not invoke search (this result is cache as long as
		// at least 1 attempt has been made)
		final int maxTests = 10;
		for (int i = 0; i < maxTests; ++i) {
			solrIndexSearchResult.getLastNumFound();
		}
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getAllResults()}.
	 */
	@Test
	public void testGetAllResults() {
		final int startIndex = 0;
		final int maxResults = 0;
		final int numFound = 30;
		context.checking(new Expectations() {
			{
				oneOf(mockSearcher).search(startIndex, maxResults);
				oneOf(mockSearcher).search(startIndex, numFound);
			}
		});
		testSolrSearcher.setNumFoundOnSearch(numFound);
		solrIndexSearchResult.getAllResults();
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getAttributeRangeFilterOptions()}. This
	 * should never be null, even after a query that returns nothing.
	 */
	@Test
	public void testGetAttributeRangeFilterOptions() {
		assertNotNull(solrIndexSearchResult.getAttributeRangeFilterOptions());

		solrIndexSearchResult.setAttributeRangeFilterOptions(null);
		mockAllowAllSearcher();
		solrIndexSearchResult.getResults(0, 0);
	}

	private void mockAllowAllSearcher() {
		context.checking(new Expectations() {
			{
				allowing(mockSearcher).search(0, 0);
			}
		});
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getAttributeValueFilterOptions()}. This
	 * should never be null, even after a query that returns nothing.
	 */
	@Test
	public void testGetAttributeValueFilterOptions() {
		assertNotNull(solrIndexSearchResult.getAttributeValueFilterOptions());

		solrIndexSearchResult.setAttributeValueFilterOptions(null);
		mockAllowAllSearcher();
		solrIndexSearchResult.getResults(0, 0);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getPriceFilterOptions()}. This should never
	 * be null, even after a query that returns nothing.
	 */
	@Test
	public void testGetPriceFilterOptions() {
		assertNotNull(solrIndexSearchResult.getPriceFilterOptions());

		solrIndexSearchResult.setPriceFilterOptions(null);
		mockAllowAllSearcher();
		solrIndexSearchResult.getResults(0, 0);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getBrandFilterOptions()}. This should never
	 * be null, even after a query that returns nothing.
	 */
	@Test
	public void testGetBrandFilterOptions() {
		assertNotNull(solrIndexSearchResult.getBrandFilterOptions());

		solrIndexSearchResult.setBrandFilterOptions(null);
		mockAllowAllSearcher();
		solrIndexSearchResult.getResults(0, 0);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getCategoryFilterOptions()}. This should never
	 * be null, even after a query that returns nothing.
	 */
	@Test
	public void testGetCategoryFilterOptions() {
		assertNotNull(solrIndexSearchResult.getCategoryFilterOptions());

		solrIndexSearchResult.setCategoryFilterOptions(null);
		mockAllowAllSearcher();
		solrIndexSearchResult.getResults(0, 0);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getResults(int, int)} with negative numbers.
	 * No search should occur.
	 */
	@Test
	public void testWithNegativeNumbers() {
		final int negNum1 = -4;
		final int negNum2 = -535;
		final int negNum3 = -345345;
		final int negNum4 = -3445;

		assertNull(solrIndexSearchResult.getCachedResultUids());
		solrIndexSearchResult.getResults(negNum1, negNum1);
		assertNotNull(solrIndexSearchResult.getCachedResultUids());
		solrIndexSearchResult.getResults(negNum4, negNum2);
		assertNotNull(solrIndexSearchResult.getCachedResultUids());
		solrIndexSearchResult.getResults(negNum4, negNum3);
		assertNotNull(solrIndexSearchResult.getCachedResultUids());
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getResults(int, int)} with rememberOptions = false.
	 * Ensures that filter options are cleared.
	 */
	@Test
	public void testGetResultsWithoutRememberOptions() {
		testGetResults(false);
	}

	/**
	 * Test method for {@link SolrIndexSearchResult#getResults(int, int)} with rememberOptions = true.
	 * Ensures that filter options are NOT cleared.
	 */
	@Test
	public void testGetResultsWithRememberOptions() {
		testGetResults(true);
	}

	private void testGetResults(final boolean rememberOptions) {
		populateSearchResult();

		solrIndexSearchResult.setRememberOptions(rememberOptions);

		final int startIndex = 0;
		final int maxResults = 1;
		context.checking(new Expectations() {
			{
				oneOf(mockSearcher).search(startIndex, maxResults);
			}
		});

		solrIndexSearchResult.getResults(startIndex, maxResults); // calls clearAllFilters()

		assertTrue(solrIndexSearchResult.getCachedResultUids().isEmpty());
		assertEquals(solrIndexSearchResult.getCategoryFilterOptions().isEmpty(), !rememberOptions);
		assertEquals(solrIndexSearchResult.getBrandFilterOptions().isEmpty(), !rememberOptions);
		assertEquals(solrIndexSearchResult.getPriceFilterOptions().isEmpty(), !rememberOptions);
		assertEquals(solrIndexSearchResult.getAttributeValueFilterOptions().isEmpty(), !rememberOptions);
		assertEquals(solrIndexSearchResult.getAttributeRangeFilterOptions().isEmpty(), !rememberOptions);
	}

	private void populateSearchResult() {
		final List<Long> results = new ArrayList<>();
		results.add(1L);
		final Map<String, List<FilterOption<AttributeRangeFilter>>> attributeRangeFilterOptions =
			new HashMap<>();
		attributeRangeFilterOptions.put(StringUtils.EMPTY, null);
		final List<FilterOption<CategoryFilter>> categoryFilterOptions = new ArrayList<>();
		categoryFilterOptions.add(new FilterOptionImpl<>());
		final List<FilterOption<BrandFilter>> brandFilterOptions = new ArrayList<>();
		brandFilterOptions.add(new FilterOptionImpl<>());
		final List<FilterOption<PriceFilter>> priceFilterOptions = new ArrayList<>();
		priceFilterOptions.add(new FilterOptionImpl<>());
		final Map<String, List<FilterOption<AttributeValueFilter>>> attributeFilterOptions =
			new HashMap<>();
		attributeFilterOptions.put(StringUtils.EMPTY, null);

		solrIndexSearchResult.setResultUids(results);
		solrIndexSearchResult.setCategoryFilterOptions(categoryFilterOptions);
		solrIndexSearchResult.setBrandFilterOptions(brandFilterOptions);
		solrIndexSearchResult.setPriceFilterOptions(priceFilterOptions);
		solrIndexSearchResult.setAttributeValueFilterOptions(attributeFilterOptions);
		solrIndexSearchResult.setAttributeRangeFilterOptions(attributeRangeFilterOptions);
	}

	/**
	 * Test getter and setter for rememberOptions flag.
	 */
	@Test
	public void testGetSetRememberOptions() {
		assertFalse("rememberOptions is not set yet, should be false", solrIndexSearchResult.isRememberOptions());
		solrIndexSearchResult.setRememberOptions(true);
		assertTrue("rememberOptions should be true", solrIndexSearchResult.isRememberOptions());
	}

	/**
	 * Stub class for testing purposes.
	 */
	private class TestSolrSearcher extends SolrIndexSearcherImpl {

		private Searcher searcher;

		private int numFound;

		TestSolrSearcher(final BeanFactory beanFactory) {
			super();
			setBeanFactory(beanFactory);
		}

		@Override
		public void search(final SearchCriteria searchCriteria, final int startIndex, final int maxResults,
				final SolrIndexSearchResult searchResult) {
			final int five = 5;
			if (startIndex <= five) {
				searchResult.setNumFound(-1);
			}
			searcher.search(startIndex, maxResults);
			searchResult.setResultUids(new ArrayList<>());
			searchResult.setNumFound(numFound);
		}

		public void setNumFoundOnSearch(final int numFound) {
			this.numFound = numFound;
		}
	}

	/**
	 * Stub interface for testing purposes.
	 */
	private interface Searcher {
		void search(int startIndex, int maxResults);
	}
}
