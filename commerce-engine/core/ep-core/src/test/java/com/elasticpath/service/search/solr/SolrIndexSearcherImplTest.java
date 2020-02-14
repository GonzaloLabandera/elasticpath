/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.search.solr;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.util.NamedList;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.FilterOption;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.impl.AttributeRangeFilterImpl;
import com.elasticpath.domain.catalogview.impl.AttributeValueFilterImpl;
import com.elasticpath.domain.catalogview.impl.FilterOptionImpl;
import com.elasticpath.domain.catalogview.impl.PriceFilterImpl;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.domain.misc.impl.SearchConfigImpl;
import com.elasticpath.service.catalogview.filterednavigation.impl.FilteredNavigationConfigurationImpl;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.index.QueryComposerFactory;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.query.SolrIndexSearchResult;

/**
 * Test <code>IndexSearcherImpl</code>.
 */
public class SolrIndexSearcherImplTest {

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery() {
		{
			setImposteriser(ClassImposteriser.INSTANCE);
		}
	};

	private final ElasticPath elasticPath = context.mock(ElasticPath.class);

	private SolrProvider solrProvider;

	private SolrClient solrClient;

	private SolrQueryFactory queryFactory;

	private QueryComposerFactory queryComposerFactory;

	private QueryComposer queryComposer;

	private SolrIndexSearcherImpl solrIndexSearcherImpl;

	private static final int DUMMY_MAX_RETURN_NUM = 100;

	/**
	 * Prepare for test.
	 *
	 * @throws Exception in case of error
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		solrClient = context.mock(SolrClient.class);
		solrProvider = context.mock(SolrProvider.class);
		queryFactory = context.mock(SolrQueryFactory.class);
		queryComposer = context.mock(QueryComposer.class);
		queryComposerFactory = context.mock(QueryComposerFactory.class);

		context.checking(new Expectations() {
			{
				allowing(solrProvider).getServer(with(any(IndexType.class))); will(returnValue(solrClient));
				allowing(solrProvider).getSearchConfig(with(any(IndexType.class))); will(returnValue(new SearchConfigImpl()));

				allowing(queryFactory).composeSpecificQuery(with(queryComposer), with(any(SearchCriteria.class)), with(any(Integer.class)),
						with(any(Integer.class)), with(any(SearchConfig.class)), with(any(Boolean.class)), with(any(Map.class)));
				will(returnValue(new SolrQuery()));

				allowing(queryFactory).composeKeywordQuery(with(any(KeywordSearchCriteria.class)), with(any(Integer.class)),
						with(any(Integer.class)), with(any(SearchConfig.class)), with(any(Boolean.class)), with(any(Map.class)));
				will(returnValue(new SolrQuery()));
			}
		});

		solrIndexSearcherImpl = new SolrIndexSearcherImpl();
		solrIndexSearcherImpl.setSolrProvider(solrProvider);
		solrIndexSearcherImpl.setSolrQueryFactory(queryFactory);
		solrIndexSearcherImpl.setQueryComposerFactory(queryComposerFactory);
	}

	/**
	 * Non fuzzy search test method for
	 * {@link SolrIndexSearcherImpl#search(SearchCriteria, int, SolrIndexSearchResult)}.
	 * @throws IOException in case of error during solr request
	 * @throws SolrServerException in case of error during solr request
	 */
	@Test
	public void testNonFuzzySearch() throws SolrServerException, IOException {

		// test fuzzy search
		final ProductSearchCriteria searchCriteria = new ProductSearchCriteria();
		searchCriteria.setFuzzySearchDisabled(true);

		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();

		context.checking(new Expectations() {
			{
				allowing(queryComposerFactory).getComposerForCriteria(with(aNonNull(ProductSearchCriteria.class)));
				will(returnValue(queryComposer));

				oneOf(solrClient).request(with(aNonNull(QueryRequest.class)), with(aNull(String.class)));
				will(returnValue(new NamedList<>()));
			}
		});
		solrIndexSearcherImpl.search(searchCriteria, DUMMY_MAX_RETURN_NUM, searchResult);
	}

	/**
	 * Fuzzy search test method for
	 * {@link SolrIndexSearcherImpl#search(SearchCriteria, int, SolrIndexSearchResult)}.
	 * @throws IOException in case of error during solr request
	 * @throws SolrServerException in case of error during solr request
	 */
	@Test
	public void testFuzzySearch() throws SolrServerException, IOException {

		// test non fuzzy search
		final ProductSearchCriteria searchCriteria = new ProductSearchCriteria();

		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();

		context.checking(new Expectations() {
			{
				allowing(queryComposerFactory).getComposerForCriteria(with(aNonNull(ProductSearchCriteria.class)));
				will(returnValue(queryComposer));

				exactly(2).of(solrClient).request(with(aNonNull(SolrRequest.class)), with(aNull(String.class)));
				will(returnValue(new NamedList<>()));
			}
		});

		searchCriteria.setFuzzySearchDisabled(false);
		solrIndexSearcherImpl.search(searchCriteria, DUMMY_MAX_RETURN_NUM, searchResult);
	}

	/**
	 * Fuzzy search with keyword test method for
	 * {@link SolrIndexSearcherImpl#search(SearchCriteria, int, SolrIndexSearchResult)}. This
	 * should <i>not</i> invoke the {@link QueryComposerFactory}.
	 * @throws IOException in case of error during solr request
	 * @throws SolrServerException in case of error during solr request
	 */
	@Test
	public void testFuzzySearchWithKeyword() throws SolrServerException, IOException {
		final SearchCriteria searchCriteria = new KeywordSearchCriteria();
		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();

		context.checking(new Expectations() {
			{
				allowing(queryComposerFactory).getComposerForCriteria(with(aNonNull(ProductSearchCriteria.class)));
				will(returnValue(queryComposer));

				exactly(2).of(solrClient).request(with(aNonNull(QueryRequest.class)), with(aNull(String.class)));
				will(returnValue(new NamedList<>()));
			}
		});


		// test fuzzy search enabled
		searchCriteria.setFuzzySearchDisabled(false);
		solrIndexSearcherImpl.search(searchCriteria, DUMMY_MAX_RETURN_NUM, searchResult);
	}

	/**
	 * Non fuzzy search with keyword test method for
	 * {@link SolrIndexSearcherImpl#search(SearchCriteria, int, SolrIndexSearchResult)}. This
	 * should <i>not</i> invoke the {@link QueryComposerFactory}.
	 * @throws IOException in case of error during solr request
	 * @throws SolrServerException in case of error during solr request
	 */
	@Test
	public void testNonFuzzySearchWithKeyword() throws SolrServerException, IOException {
		final SearchCriteria searchCriteria = new KeywordSearchCriteria();
		searchCriteria.setFuzzySearchDisabled(true);
		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();

		context.checking(new Expectations() {
			{
				allowing(queryComposerFactory).getComposerForCriteria(with(aNonNull(ProductSearchCriteria.class)));
				will(returnValue(queryComposer));

				oneOf(solrClient).request(with(aNonNull(QueryRequest.class)), with(aNull(String.class)));
				will(returnValue(new NamedList<>()));
			}
		});

		solrIndexSearcherImpl.search(searchCriteria, DUMMY_MAX_RETURN_NUM, searchResult);

	}

	/**
	 * Tests that if a query starts with a bracket '(' (generated by a boolean query)
	 * it still is verified properly that it starts with the given prefix.
	 */
	@Test
	public void testQueryStartsWith() {
		final String fieldPrefix = "pre";
		String queryString = "((pre_testPre_pre(())";
		assertTrue("The query is expected to start with the term 'pre'", solrIndexSearcherImpl.queryStartsWith(queryString, fieldPrefix));

		queryString = "preTest";
		assertTrue("The query is expected to start with the term 'pre'", solrIndexSearcherImpl.queryStartsWith(queryString, fieldPrefix));

		queryString = "trepre";
		assertFalse("The query is expected not to start with the term 'pre'", solrIndexSearcherImpl.queryStartsWith(queryString, fieldPrefix));
	}

	/**
	 * Tests that facet queries with attribute range are parsed correctly.
	 */
	@Test
	public void testParseFacetQueriesWithAttributeRange() {
		final String attributeRangeKey = "attribute.A02638_sf:[6.0 TO *]";

		final FilterOption<AttributeRangeFilter> filterOption = new FilterOptionImpl<>();
		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();
		final SolrFacetAdapter facetAdapter = new SolrFacetAdapter();
		facetAdapter.setConfig(new FilteredNavigationConfigurationImpl());
		final AttributeRangeFilter attributeRangeFilter = new AttributeRangeFilterImpl();
		attributeRangeFilter.setAttribute(new AttributeImpl());
		final Map<String, Integer> facetQueries = new HashMap<>();
		facetQueries.put(attributeRangeKey, 2);

		context.checking(new Expectations() { {
			allowing(elasticPath).getPrototypeBean(ContextIdNames.FILTER_OPTION, FilterOption.class); will(returnValue(filterOption));
		} });

		solrIndexSearcherImpl.setSolrFacetAdapter(facetAdapter);
		solrIndexSearcherImpl.setElasticPath(elasticPath);
		solrIndexSearcherImpl.parseFacetQueries(searchResult, facetQueries, ImmutableMap.of(attributeRangeKey, attributeRangeFilter));

		assertFalse(searchResult.getAttributeRangeFilterOptions().isEmpty());
		assertTrue(searchResult.getAttributeValueFilterOptions().isEmpty());
		assertTrue(searchResult.getPriceFilterOptions().isEmpty());
	}

	/**
	 * Tests that facet queries with attribute value are parsed correctly.
	 */
	@Test
	public void testParseFacetQueriesWithAttributeValue() {
		final String attributeValueKey = "attribute.A02638_sf:value";

		final FilterOption<AttributeValueFilter> filterOption = new FilterOptionImpl<>();
		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();
		final SolrFacetAdapter facetAdapter = new SolrFacetAdapter();
		facetAdapter.setConfig(new FilteredNavigationConfigurationImpl());
		final AttributeValueFilter attributeValueFilter = new AttributeValueFilterImpl();
		attributeValueFilter.setAttribute(new AttributeImpl());
		final Map<String, Integer> facetQueries = new HashMap<>();
		facetQueries.put(attributeValueKey, 2);

		context.checking(new Expectations() { {
			allowing(elasticPath).getPrototypeBean(ContextIdNames.FILTER_OPTION, FilterOption.class); will(returnValue(filterOption));
		} });

		solrIndexSearcherImpl.setSolrFacetAdapter(facetAdapter);
		solrIndexSearcherImpl.setElasticPath(elasticPath);
		solrIndexSearcherImpl.parseFacetQueries(searchResult, facetQueries, ImmutableMap.of(attributeValueKey, attributeValueFilter));

		assertTrue(searchResult.getAttributeRangeFilterOptions().isEmpty());
		assertFalse(searchResult.getAttributeValueFilterOptions().isEmpty());
		assertTrue(searchResult.getPriceFilterOptions().isEmpty());
	}

	/**
	 * Tests that facet queries with price are parsed correctly.
	 */
	@Test
	public void testParseFacetQueriesWithPriceValue() {
		final String priceValueKey = "priceA02638_sf:value";

		final FilterOption<PriceFilter> filterOption = new FilterOptionImpl<>();
		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();
		final SolrFacetAdapter facetAdapter = new SolrFacetAdapter();
		final PriceFilter priceFilter = new PriceFilterImpl();
		facetAdapter.setConfig(new FilteredNavigationConfigurationImpl());
		final Map<String, Integer> facetQueries = new HashMap<>();
		facetQueries.put(priceValueKey, 2);

		context.checking(new Expectations() { {
			allowing(elasticPath).getPrototypeBean(ContextIdNames.FILTER_OPTION, FilterOption.class); will(returnValue(filterOption));
		} });

		solrIndexSearcherImpl.setSolrFacetAdapter(facetAdapter);
		solrIndexSearcherImpl.setElasticPath(elasticPath);
		solrIndexSearcherImpl.parseFacetQueries(searchResult, facetQueries, ImmutableMap.of(priceValueKey, priceFilter));

		assertTrue(searchResult.getAttributeRangeFilterOptions().isEmpty());
		assertTrue(searchResult.getAttributeValueFilterOptions().isEmpty());
		assertFalse(searchResult.getPriceFilterOptions().isEmpty());
	}

	/**
	 * Tests that facet queries with random values are handled correctly.
	 */
	@Test
	public void testParseFacetQueriesWithRandomValueOrZeroValue() {
		final String randomValueKey = "random:value";
		final String priceValueKey = "priceA02638_sf:value";

		final SolrIndexSearchResult searchResult = new SolrIndexSearchResult();
		final Map<String, Integer> facetQueries = new HashMap<>();
		facetQueries.put(priceValueKey, 0);
		facetQueries.put(randomValueKey, 2);

		solrIndexSearcherImpl.parseFacetQueries(searchResult, facetQueries, ImmutableMap.of());

		assertTrue(searchResult.getAttributeRangeFilterOptions().isEmpty());
		assertTrue(searchResult.getAttributeValueFilterOptions().isEmpty());
		assertTrue(searchResult.getPriceFilterOptions().isEmpty());
	}
}
