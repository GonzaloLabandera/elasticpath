/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.search.solr;

import static com.elasticpath.service.search.solr.SolrIndexConstants.BRAND_CODE;
import static com.elasticpath.service.search.solr.SolrIndexConstants.PRODUCT_CATEGORY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.SolrParams;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalogview.AttributeFilter;
import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.BrandFilter;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.Filter;
import com.elasticpath.domain.catalogview.FilterOption;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.SizeRangeFilter;
import com.elasticpath.domain.catalogview.SkuOptionValueFilter;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfigurationLoader;
import com.elasticpath.service.search.StoreAwareSearchCriteria;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.index.QueryComposerFactory;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.query.SolrIndexSearchResult;

/**
 * Data access for the SOLR index.
 */
@SuppressWarnings("PMD.GodClass")
public class SolrIndexSearcherImpl {

	private static final Logger LOG = LogManager.getLogger(SolrIndexSearcherImpl.class);

	private static final Set<String> SIZES = ImmutableSet.of(SolrIndexConstants.WEIGHT, SolrIndexConstants.HEIGHT, SolrIndexConstants.LENGTH,
			SolrIndexConstants.WIDTH);

	private static final Pattern RANGE_REGEX_PATTERN = Pattern
			.compile("[\\[\\{](?:\\w+|\\d+\\.\\d+|\\*)\\s+TO\\s+(?:\\w+|\\d+\\.\\d+|\\*)[\\]\\}]");
	private SolrProvider solrProvider;
	private SolrQueryFactory solrQueryFactory;
	private BeanFactory beanFactory;
	private QueryComposerFactory queryComposerFactory;
	private IndexUtility indexUtility;
	private FilteredNavigationConfigurationLoader filteredNavigationConfigurationLoader;

	/**
	 * Searches an index (determined by the type of the search criteria).
	 * The results are only those on the first page.
	 *
	 * @param searchCriteria the search criteria
	 * @param maxResults     the maximum results to return (maximum per page)
	 * @param searchResult   the search result object to populate
	 */
	public void search(final SearchCriteria searchCriteria, final int maxResults, final SolrIndexSearchResult searchResult) {
		search(searchCriteria, 0, maxResults, searchResult);
	}

	/**
	 * Searches an index (determined by the type of the search criteria)
	 * starting from the specified index. The number of results is limited
	 * by the specified <code>maxResults</code>. The results are only those on the first page.
	 * The given SolrIndexSearchResult is populated with <code>FilterOption</code>s corresponding to
	 * the facets that are returned as part of the SOLR search results (the resultant facets are dependent
	 * upon the filters that are set in the search criteria).
	 *
	 * @param searchCriteria the search criteria
	 * @param startIndex     the initial index to display results for
	 * @param maxResults     the maximum results to return from given start index (maximum per page)
	 * @param searchResult   the search result object to populate
	 */
	public void search(final SearchCriteria searchCriteria, final int startIndex, final int maxResults,
					   final SolrIndexSearchResult searchResult) {
		final SearchConfig searchConfig = solrProvider.getSearchConfig(searchCriteria.getIndexType());

		SolrQuery solrQuery;
		Map<String, Filter<?>> filterLookup = new HashMap<>();
		if (searchCriteria instanceof KeywordSearchCriteria) {
			solrQuery = solrQueryFactory.composeKeywordQuery((KeywordSearchCriteria) searchCriteria, startIndex, maxResults,
					searchConfig, false, filterLookup);
		} else {
			final QueryComposer queryComposer = queryComposerFactory.getComposerForCriteria(searchCriteria);
			solrQuery = solrQueryFactory.composeSpecificQuery(queryComposer, searchCriteria, startIndex, maxResults,
					searchConfig, false, filterLookup);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Generated query: " + solrQuery);
		}

		Map<String, Facet> facetMap = null;
		if (searchCriteria instanceof StoreAwareSearchCriteria) {
			StoreAwareSearchCriteria storeAwareSearchCriteria = (StoreAwareSearchCriteria) searchCriteria;
			FilteredNavigationConfiguration filteredNavigationConfiguration =
					filteredNavigationConfigurationLoader.loadFilteredNavigationConfiguration(storeAwareSearchCriteria.getStoreCode());
			facetMap = filteredNavigationConfiguration.getFacetMap();
		}

		// get the SOLR server and do the search
		final SolrClient client = solrProvider.getServer(searchCriteria.getIndexType());
		doUidSearch(client, solrQuery, searchResult, filterLookup, facetMap);

		// If we don't have any results try again with a 'fuzzy search'
		if (searchResult.getLastNumFound() == 0 && !searchCriteria.isFuzzySearchDisabled()) {
			if (searchCriteria instanceof KeywordSearchCriteria) {
				solrQuery = solrQueryFactory.composeKeywordQuery((KeywordSearchCriteria) searchCriteria, startIndex, maxResults,
						searchConfig, true, filterLookup);
			} else {
				final QueryComposer queryComposer = queryComposerFactory.getComposerForCriteria(searchCriteria);
				solrQuery = solrQueryFactory.composeSpecificQuery(queryComposer, searchCriteria, startIndex, maxResults,
						searchConfig, true, filterLookup);
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Generated fuzzy query: " + solrQuery);
			}
			doUidSearch(client, solrQuery, searchResult, filterLookup, facetMap);
		}

		// make sure the results numFound doesn't exceed the max return number
		if (searchConfig.getMaxReturnNumber() > 0) {
			searchResult.setNumFound(Math.min(searchConfig.getMaxReturnNumber(), searchResult.getLastNumFound()));
		}
	}

	/**
	 * Searches the given SOLR server with the given query and parses the response,
	 * using it to populate the given <code>SearchResult</code>. The SearchResult will contain a list of UIDs
	 * matching the given query. If the search is initiated by a store-aware search criteria then the response
	 * document may have a store-specific facet to parse, otherwise any store-specific facets will be assumed not
	 * to exist and will not be parsed.
	 *
	 * @param client       the SOLR server to search
	 * @param query        the query to search with
	 * @param searchResult the search result to modify
	 * @param filterLookup map keyed on solr query filter with a value containing the filter
	 * @param facetMap     the facet map
	 */
	private void doUidSearch(final SolrClient client, final SolrParams query, final SolrIndexSearchResult searchResult,
							 final Map<String, Filter<?>> filterLookup, final Map<String, Facet> facetMap) {
		try {
			final QueryRequest queryRequest = new QueryRequest(query);
			queryRequest.setMethod(METHOD.POST);
			parseResponseDocument(queryRequest.process(client), searchResult, filterLookup, facetMap);
		} catch (final SolrServerException | IOException e) {
			if (client instanceof HttpSolrClient) {
				LOG.error("Error executing search. Solr Manager url : " + ((HttpSolrClient) client).getBaseURL(), e);
			}
			throw new EpPersistenceException("Solr exception executing search", e);
		}
	}

	private void parseResponseDocument(final QueryResponse response, final SolrIndexSearchResult searchResult,
									   final Map<String, Filter<?>> filterLookup, final Map<String, Facet> facetMap) {
		List<Long> objectUidList = Collections.emptyList();
		if (response.getResults() == null) {
			searchResult.setNumFound(0);
			searchResult.setResultUids(objectUidList);
			return;
		}
		searchResult.setNumFound((int) response.getResults().getNumFound());
		objectUidList = new ArrayList<>(response.getResults().size());
		for (final SolrDocument document : response.getResults()) {
			String fieldValue = (String) document.getFieldValue(SolrIndexConstants.OBJECT_UID);
			objectUidList.add(Long.valueOf(fieldValue));
		}
		searchResult.setResultUids(objectUidList);

		if (!searchResult.isRememberOptions()) {
			parseFacetInformation(response, searchResult, filterLookup, facetMap);
		}
	}

	private void parseFacetInformation(final QueryResponse response, final SolrIndexSearchResult searchResult,
									   final Map<String, Filter<?>> filterLookup, final Map<String, Facet> facetMap) {
		final Map<String, Integer> facetQueries = response.getFacetQuery();
		if (facetQueries != null) {
			parseFacetQueries(searchResult, facetQueries, filterLookup, facetMap);
		}
	}

	/**
	 * Parses facet queries for attribute values, attribute ranges or price values.
	 *
	 * @param searchResult search result
	 * @param facetQueries facet queries
	 * @param filterLookup map keyed on solr query filter with a value containing the filter
	 * @param facetMap     the facet map
	 */
	void parseFacetQueries(final SolrIndexSearchResult searchResult, final Map<String, Integer> facetQueries,
						   final Map<String, Filter<?>> filterLookup, final Map<String, Facet> facetMap) {
		final List<FilterOption<PriceFilter>> priceFilterOptions = new ArrayList<>(facetQueries.size());
		final List<FilterOption<AttributeValueFilter>> attributeValueFilterOptions = new ArrayList<>(facetQueries.size());
		final List<FilterOption<AttributeRangeFilter>> attributeRangeFilterOptions = new ArrayList<>();
		final List<FilterOption<SkuOptionValueFilter>> skuOptionValueFilterOptions = new ArrayList<>();
		final List<FilterOption<SizeRangeFilter>> sizeFilterOptions = new ArrayList<>();
		final List<FilterOption<BrandFilter>> brandFilters = new ArrayList<>();
		final List<FilterOption<CategoryFilter>> categoryFilters = new ArrayList<>();

		for (final Entry<String, Integer> entry : facetQueries.entrySet()) {
			// don't care about entries with a zero count
			if (entry.getValue() == 0) {
				continue;
			}
			String query = entry.getKey();
			String queryString = query.replaceFirst("(\\{[\\w-!= ]+})", "");
			final String filterTag = Optional.ofNullable(StringUtils.substringBetween(query, "{", "}"))
					.map(string -> string.replace("!ex=", "")).orElse(null);
			if (isPriceKey(queryString)) {
				addPriceFilter(filterLookup, priceFilterOptions, entry, query, queryString, filterTag);
			} else if (isAttributeRangeKey(queryString)) {
				addAttributeRangeFilter(filterLookup, attributeRangeFilterOptions, entry, query, queryString, filterTag);
			} else if (isAttributeKey(queryString)) {
				addAttributeValueFilter(filterLookup, attributeValueFilterOptions, entry, query, queryString, filterTag);
			} else if (isSkuOptionKey(queryString)) {
				addSkuOptionValueFilter(filterLookup, skuOptionValueFilterOptions, entry, query, queryString, filterTag);
			} else if (isSizeFilter(queryString)) {
				addSizeRangeFilter(filterLookup, sizeFilterOptions, entry, query, queryString, filterTag);
			} else if (isBrandFilter(queryString)) {
				addBrandFilter(filterLookup, brandFilters, entry, query, queryString, filterTag);
			} else if (isCategoryFilter(queryString)) {
				addCategoryFilter(filterLookup, categoryFilters, entry, query, queryString, filterTag);
			}
		}
		searchResult.setPriceFilterOptions(priceFilterOptions);
		searchResult.setSizeRangeFilterOptions(createSizeRangeFilterMap(sizeFilterOptions));
		searchResult.setSkuOptionValueFilterOptions(createSkuOptionValueFilterMap(skuOptionValueFilterOptions));
		searchResult.setAttributeValueFilterOptions(createAttributeFilterMap(attributeValueFilterOptions));
		searchResult.setAttributeRangeFilterOptions(createAttributeFilterMap(attributeRangeFilterOptions));
		searchResult.setCategoryFilterOptions(categoryFilters);
		searchResult.setBrandFilterOptions(brandFilters);
		searchResult.setFacetMap(facetMap);
	}

	private void addCategoryFilter(final Map<String, Filter<?>> filterLookup, final List<FilterOption<CategoryFilter>> categoryFilters,
								   final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final CategoryFilter categoryFilter = (CategoryFilter) filterLookup.get(query);
		final FilterOption<CategoryFilter> filterOption = constructFilterOption(entry.getValue(), categoryFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		categoryFilters.add(filterOption);
	}

	private void addBrandFilter(final Map<String, Filter<?>> filterLookup, final List<FilterOption<BrandFilter>> brandFilters,
								final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final BrandFilter brandFilter = (BrandFilter) filterLookup.get(query);
		final FilterOption<BrandFilter> filterOption = constructFilterOption(entry.getValue(), brandFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		brandFilters.add(filterOption);
	}

	private void addSizeRangeFilter(final Map<String, Filter<?>> filterLookup, final List<FilterOption<SizeRangeFilter>> sizeFilterOptions,
									final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final SizeRangeFilter sizeRangeFilter = (SizeRangeFilter) filterLookup.get(query);
		final FilterOption<SizeRangeFilter> filterOption = constructFilterOption(entry.getValue(), sizeRangeFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		sizeFilterOptions.add(filterOption);
	}

	private void addSkuOptionValueFilter(final Map<String, Filter<?>> filterLookup,
										 final List<FilterOption<SkuOptionValueFilter>> skuOptionValueFilterOptions,
										 final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final SkuOptionValueFilter skuOptionValueFilter = (SkuOptionValueFilter) filterLookup.get(query);
		final FilterOption<SkuOptionValueFilter> filterOption = constructFilterOption(entry.getValue(), skuOptionValueFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		skuOptionValueFilterOptions.add(filterOption);
	}

	private void addAttributeValueFilter(final Map<String, Filter<?>> filterLookup,
										 final List<FilterOption<AttributeValueFilter>> attributeValueFilterOptions,
										 final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final AttributeValueFilter valueFilter = (AttributeValueFilter) filterLookup.get(query);
		final FilterOption<AttributeValueFilter> filterOption = constructFilterOption(entry.getValue(), valueFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		attributeValueFilterOptions.add(filterOption);
	}

	private void addAttributeRangeFilter(final Map<String, Filter<?>> filterLookup,
										 final List<FilterOption<AttributeRangeFilter>> attributeRangeFilterOptions,
										 final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final AttributeRangeFilter rangeFilter = (AttributeRangeFilter) filterLookup.get(query);
		final FilterOption<AttributeRangeFilter> filterOption = constructFilterOption(entry.getValue(), rangeFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		attributeRangeFilterOptions.add(filterOption);
	}

	private void addPriceFilter(final Map<String, Filter<?>> filterLookup, final List<FilterOption<PriceFilter>> priceFilterOptions,
								final Entry<String, Integer> entry, final String query, final String queryString, final String filterTag) {
		final PriceFilter priceFilter = (PriceFilter) filterLookup.get(query);
		final FilterOption<PriceFilter> filterOption = constructFilterOption(entry.getValue(), priceFilter);
		enrichFilterOption(queryString, filterTag, filterOption);
		priceFilterOptions.add(filterOption);
	}

	private void enrichFilterOption(final String queryString, final String filterTag, final FilterOption<?> filterOption) {
		filterOption.setQueryString(queryString);
		filterOption.setFilterTag(filterTag);
	}

	private boolean isBrandFilter(final String queryString) {
		return queryString.startsWith(BRAND_CODE);
	}

	private boolean isCategoryFilter(final String queryString) {
		return queryString.startsWith(PRODUCT_CATEGORY);
	}

	private Map<String, List<FilterOption<SizeRangeFilter>>> createSizeRangeFilterMap(final List<FilterOption<SizeRangeFilter>> filterOptions) {
		Map<String, List<FilterOption<SizeRangeFilter>>> sizeRangeFilters = new HashMap<>();
		for (FilterOption<SizeRangeFilter> filterOption : filterOptions) {
			String key = filterOption.getFilterTag();
			if (!sizeRangeFilters.containsKey(key)) {
				sizeRangeFilters.put(key, new ArrayList<>());
			}
			sizeRangeFilters.get(key).add(filterOption);
		}
		return sizeRangeFilters;
	}

	private boolean isSizeFilter(final String key) {
		return SIZES.contains(key.split(":")[0]);
	}

	private Map<String, List<FilterOption<SkuOptionValueFilter>>> createSkuOptionValueFilterMap(
			final List<FilterOption<SkuOptionValueFilter>> filterOptions) {
		Map<String, List<FilterOption<SkuOptionValueFilter>>> skuOptionValueFilters = new HashMap<>();
		for (FilterOption<SkuOptionValueFilter> filterOption : filterOptions) {
			String key = filterOption.getFilterTag();
			if (!skuOptionValueFilters.containsKey(key)) {
				skuOptionValueFilters.put(key, new ArrayList<>());
			}
			skuOptionValueFilters.get(key).add(filterOption);
		}
		return skuOptionValueFilters;
	}

	private boolean isPriceKey(final String key) {
		return queryStartsWith(key, SolrIndexConstants.PRICE);
	}

	private boolean isAttributeKey(final String attributeKey) {
		return attributeKey.startsWith(SolrIndexConstants.ATTRIBUTE_PREFIX);
	}

	private boolean isSkuOptionKey(final String key) {
		return key.startsWith(SolrIndexConstants.SKU_OPTION_PREFIX);
	}

	private boolean isAttributeRangeKey(final String attributeKey) {
		final Matcher matcher = RANGE_REGEX_PATTERN.matcher(attributeKey);
		return matcher.find() && isAttributeKey(attributeKey);
	}

	/**
	 * A query starts with a {@code fieldPrefix } if the prefix is in the begging of the {@code queryString }
	 * or the {@code queryString } starts with a bracket '('.
	 *
	 * @param queryString the query string to check
	 * @param fieldPrefix the field prefix to look for
	 * @return true if the query starts with the given prefix
	 */
	boolean queryStartsWith(final String queryString, final String fieldPrefix) {
		final String queryToCheck = StringUtils.remove(queryString, "(");
		return queryToCheck.startsWith(fieldPrefix);
	}

	private <T extends AttributeFilter<T>> Map<String, List<FilterOption<T>>> createAttributeFilterMap(
			final List<FilterOption<T>> attributeFilterOptions) {
		final Map<String, List<FilterOption<T>>> resultMap = new LinkedHashMap<>(
				attributeFilterOptions.size());
		for (final FilterOption<T> option : attributeFilterOptions) {
			final String guid = option.getFilterTag();
			resultMap.computeIfAbsent(guid, key -> new ArrayList<>());
			resultMap.get(guid).add(option);
		}
		return resultMap;
	}

	/**
	 * Sets the {@link SolrProvider}instance.
	 *
	 * @param solrProvider the {@link SolrProvider} instance
	 */
	public void setSolrProvider(final SolrProvider solrProvider) {
		this.solrProvider = solrProvider;
	}

	private <T extends Filter<T>> FilterOption<T> constructFilterOption(final int hitsNumber, final T filter) {
		@SuppressWarnings("unchecked")
		final FilterOption<T> filterOption = beanFactory.getPrototypeBean(ContextIdNames.FILTER_OPTION, FilterOption.class);
		filterOption.setHitsNumber(hitsNumber);
		filterOption.setFilter(filter);
		return filterOption;
	}

	/**
	 * Sets the {@link SolrQueryFactory} instance to use.
	 *
	 * @param solrQueryFactory the {@link SolrQueryFactory} instance to use
	 */
	public void setSolrQueryFactory(final SolrQueryFactory solrQueryFactory) {
		this.solrQueryFactory = solrQueryFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the {@link QueryComposerFactory} instance to use.
	 *
	 * @param queryComposerFactory the {@link QueryComposerFactory} instance to use
	 */
	public void setQueryComposerFactory(final QueryComposerFactory queryComposerFactory) {
		this.queryComposerFactory = queryComposerFactory;
	}

	/**
	 * Gets the {@link IndexUtility} instance.
	 *
	 * @return the {@link IndexUtility} instance
	 */
	protected IndexUtility getIndexUtility() {
		return indexUtility;
	}

	/**
	 * Sets the index utility instance.
	 *
	 * @param indexUtility the index utility instance
	 */
	public void setIndexUtility(final IndexUtility indexUtility) {
		this.indexUtility = indexUtility;
	}

	protected FilteredNavigationConfigurationLoader getFilteredNavigationConfigurationLoader() {
		return filteredNavigationConfigurationLoader;
	}

	public void setFilteredNavigationConfigurationLoader(final FilteredNavigationConfigurationLoader filteredNavigationConfigurationLoader) {
		this.filteredNavigationConfigurationLoader = filteredNavigationConfigurationLoader;
	}
}
