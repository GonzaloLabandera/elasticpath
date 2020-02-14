/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.search.solr;

import static com.elasticpath.commons.constants.ContextIdNames.PRICE_LIST_STACK;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.DisplayableFilter;
import com.elasticpath.domain.catalogview.Filter;
import com.elasticpath.domain.misc.SearchConfig;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.pricing.PriceListAssignmentService;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.SearchConfigFactory;
import com.elasticpath.service.search.SpellSuggestionSearchCriteria;
import com.elasticpath.service.search.StoreAwareSearchCriteria;
import com.elasticpath.service.search.index.Analyzer;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.query.SearchHint;
import com.elasticpath.service.search.query.SortBy;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;
import com.elasticpath.service.search.solr.SpellingConstants.SpellingParams;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Factory class to create SOLR queries.
 */
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.GodClass", "PMD.TooManyMethods", "PMD.ExcessiveImports" })
public class SolrQueryFactoryImpl implements SolrQueryFactory {

	private static final String STANDARD_REQUEST_HANDLER = "standard";

	private static final String DISMAX_REQUEST_HANDLER = "dismax";

	private static final String SPELLING_REQUEST_HANDLER = "spellchecker";

	private static final MatchAllDocsQuery ALL_DOCS_QUERY = new MatchAllDocsQuery();

	private static final String ESCAPED_MATCH_ALL_DOCS_QUERY = "*:*";

	private static final Pattern ALL_DOCS_PATTERN = Pattern.compile("\\Q" + ALL_DOCS_QUERY + "\\E(?!\\:)", Pattern.MULTILINE);

	private static final String CONSTITUENT_COUNT_SCALE_FORMULA = "_val_:\"linear(constituentCount, 100, 1)\"";

	private static final int BOOST_SCALE = 4;

	private static final String SOLR_QF_PARAMETER = "qf";

	private IndexUtility indexUtility;

	private SolrFacetAdapter solrFacetAdapter;

	private SearchConfigFactory searchConfigFactory;

	private BeanFactory beanFactory;

	private SettingValueProvider<Boolean> showBundlesFirstProvider;

	private Analyzer analyzer;

	private AttributeService attributeService;

	private FacetService facetService;

	/**
	 * <p>Constructs a SOLR query object of <b>type=STANDARD</b> (to be handled by SOLR's <code>StandardRequestHandler</code>),
	 * corresponding to the given search criteria, taking into account the specified startIndex, maxResults,
	 * and search configuration.</p>
	 *
	 * <p>If requested, the SOLR query will be a fuzzy query. Since SOLR is heavily dependent on Lucene search
	 * technology, a Lucene query is created first with the given Query Composer, and then the Lucene query
	 * is enhanced, resulting in the requested SOLR query.</p>
	 *
	 * @param luceneQueryComposer the Lucene query composer to use
	 * @param searchCriteria the search criteria
	 * @param startIndex the initial index to display results for
	 * @param maxResults the maximum results to return from given start index (maximum per page). If their is a maxResults
	 * specified in the given SearchConfig then this parameter will be ignored
	 * @param searchConfig the search configuration to use
	 * @param fuzzyQuery whether the composed query should be fuzzy
	 * @param filterLookup a map keyed on solr query with a value containing the filter
	 * @return a {@link SolrQuery} specific query
	 */
	@Override
	public SolrQuery composeSpecificQuery(final QueryComposer luceneQueryComposer, final SearchCriteria searchCriteria,
										  final int startIndex, final int maxResults, final SearchConfig searchConfig,
										  final boolean fuzzyQuery, final Map<String, Filter<?>> filterLookup) {

		SolrQuery solrQuery = createSolrQueryFromLuceneQuery(createLuceneQuery(luceneQueryComposer, searchCriteria, searchConfig, fuzzyQuery));

		addFiltersToQuery(solrQuery, searchCriteria); //Explicitly define the restricted set of returned values by adding restriction filters

		if (searchCriteria instanceof ProductCategorySearchCriteria) {
			addFacetsToQuery(solrQuery, (ProductCategorySearchCriteria) searchCriteria, filterLookup);
		}

		addSorting(solrQuery, searchCriteria, luceneQueryComposer, searchConfig);
		addInvariantTerms(solrQuery, startIndex, maxResults, searchConfig);

		return solrQuery;
	}

	/**
	 * Adds configured facets to the given query. This implementation
	 * will only add facets if the search criteria is for a Product, and uses the
	 * {@link SolrFacetAdapter} to convert the search criteria's filters into
	 * SOLR facets.
	 *
	 * @param query the query to which facets should be added
	 * @param searchCriteria the search criteria containing the facets and the store to which the facets apply
	 * @param filterLookup a map keyed on solr query with a value containing the filter
	 */
	void addFacetsToQuery(final SolrQuery query, final ProductCategorySearchCriteria searchCriteria, final Map<String, Filter<?>> filterLookup) {
		if (searchCriteria.isFacetingEnabled() && IndexType.PRODUCT.equals(searchCriteria.getIndexType())) {
			Map<String, String> queryLookup = new HashMap<>();
			solrFacetAdapter.addFacets(query, searchCriteria, queryLookup, filterLookup);
			Map<String, String> appliedFacets = searchCriteria.getAppliedFacets();
			if (appliedFacets != null && !appliedFacets.isEmpty()) {
				addAppliedFacetFilters(query, appliedFacets, queryLookup);
			}
		}
	}

	/**
	 * Adds "filter queries" to the given SolrQuery, as specified in the given search criteria.
	 * A "filter query" is a Lucene {@link org.apache.lucene.search.Query} that is injected
	 * into a SOLR query to filter results without affecting scoring.
	 * If no filters are defined, none will be added. This basically adds facets for filtered navigation.
	 * Calls {@link #addFiltersToQuery(SolrQuery, List, String)}
	 *
	 * @param query the solr query to which filter queries should be added
	 * @param searchCriteria the search criteria potentially containing filters
	 */
	void addFiltersToQuery(final SolrQuery query, final SearchCriteria searchCriteria) {
		if (searchCriteria.getFilters() != null) {
			addFiltersToQuery(query, searchCriteria.getFilters(), searchCriteria);
		}
	}

	/**
	 * Adds the given filters to the given SolrQuery as "filter queries".
	 * A "filter query" is a Lucene {@link org.apache.lucene.search.Query} that is injected
	 * into a SOLR query to filter results without affecting scoring.
	 *
	 * @param query the Solr Query to which the filters should be added
	 * @param filters the filters to convert to facets and add to the query
	 * @param searchCriteria the search criteria
	 * Can be null if the query is not store-specific
	 */
	void addFiltersToQuery(final SolrQuery query, final List<Filter<?>> filters, final SearchCriteria searchCriteria) {
		for (Filter<?> filter : filters) {
			String filterQuery = solrFacetAdapter.constructFilterQuery(filter, searchCriteria);
			if (!StringUtils.isBlank(filterQuery)) {
				query.addFilterQuery(escapeMatchAllQuery(filterQuery));
			}
		}
	}

	@Override
	public String constructSolrQuery(final Filter<?> filter, final SearchCriteria searchCriteria) {
		return escapeMatchAllQuery(solrFacetAdapter.constructFilterQuery(filter, searchCriteria));
	}

	/**
	 * Creates a new non-fuzzy query with the given inputs using the given query composer.
	 *
	 * @param luceneQueryComposer the query composer that will create the query
	 * @param searchCriteria the search criteria for parameters to the query
	 * @param searchConfig the search configuration
	 * @param fuzzyQuery true if the created query should be a Fuzzy query, false if not
	 * @return a non-fuzzy query based on the given criteria and configuration
	 */
	Query createLuceneQuery(final QueryComposer luceneQueryComposer,
							final SearchCriteria searchCriteria, final SearchConfig searchConfig, final boolean fuzzyQuery) {
		if (fuzzyQuery) {
			return luceneQueryComposer.composeFuzzyQuery(searchCriteria, searchConfig);
		}
		return luceneQueryComposer.composeQuery(searchCriteria, searchConfig);
	}

	/**
	 * Creates a new SolrQuery from the given lucene query, and adds SOLR-specific terms.
	 * @param luceneQuery the lucene query from which to create the Solr query
	 * @return a new solr query based on the lucene query, with standard type
	 */
	SolrQuery createSolrQueryFromLuceneQuery(final Query luceneQuery) {
		final SolrQuery query = new SolrQuery();
		query.setRequestHandler(STANDARD_REQUEST_HANDLER);
		query.setQuery(escapeMatchAllQuery(luceneQuery.toString()));
		return query;
	}

	/**
	 * {@link MatchAllDocsQuery#toString()} returns something that is not parsable from the query parser.
	 * Need to escape (replace) it with something that is.
	 *
	 * @param luceneQuery the query to process
	 * @return a string that represents the query
	 */
	private String escapeMatchAllQuery(final String luceneQuery) {
		return ALL_DOCS_PATTERN.matcher(luceneQuery).replaceAll(ESCAPED_MATCH_ALL_DOCS_QUERY);
	}

	/**
	 * <p>Constructs a SOLR query object of <b>type=DISMAX</b> (to be handled by SOLR's <code>DisMaxRequestHandler</code>),
	 * corresponding to the given search criteria, taking into account the specified startIndex, maxResults,
	 * and search configuration.</p>
	 *
	 * <p>If requested, the SOLR query will be a fuzzy query. Since SOLR is heavily dependent on Lucene search
	 * technology, a Lucene query is created first with the given Query Composer, and then the Lucene query
	 * is enhanced, resulting in the requested SOLR query.</p>
	 *
	 * @param searchCriteria the search criteria
	 * @param startIndex the initial index to display results for
	 * @param maxResults the maximum results to return from given start index (maximum per page)
	 * @param searchConfig the search configuration to use
	 * @param fuzzyQuery whether the composed query should be fuzzy
	 * @return a {@link SolrQuery} keyword query
	 */
	@Override
	public SolrQuery composeKeywordQuery(final KeywordSearchCriteria searchCriteria, final int startIndex,
										 final int maxResults, final SearchConfig searchConfig, final boolean fuzzyQuery,
										 final Map<String, Filter<?>> filterLookup) {
		final SolrQuery query = new SolrQuery();

		query.setRequestHandler(DISMAX_REQUEST_HANDLER);
		String keyword = StringEscapeUtils.unescapeJava(searchCriteria.getKeyword());
		query.setQuery(keyword);
		addQueryOptions(query, searchCriteria);

		if (searchCriteria.isOfferSearch()) {
			String searchableAttributes = getSearchableAttributes(searchConfig, searchCriteria);
			if (StringUtils.isEmpty(searchableAttributes)) {
				searchableAttributes = getSearchableAttributesFromFilterAttributes(searchCriteria, searchConfig);
			}
			query.set(SOLR_QF_PARAMETER, searchableAttributes);
		} else {
			query.set(SOLR_QF_PARAMETER, this.filterAttributes(searchCriteria, searchConfig));
		}

		query.set(DisMaxConstants.FUZZY, String.valueOf(fuzzyQuery));
		if (fuzzyQuery) {
			query.set(DisMaxConstants.MINIMUM_LENGTH, searchConfig.getPrefixLength());
			query.set(DisMaxConstants.MINIMUM_SIMILARITY, String.valueOf(searchConfig.getMinimumSimilarity()));
		}

		if (searchCriteria.getCatalogCode() != null) {
			query.addFilterQuery(new TermQuery(
					new Term(SolrIndexConstants.CATALOG_CODE,
							searchCriteria.getCatalogCode())).toString());
		}

		List<Filter<?>> filterQueries = new ArrayList<>(searchCriteria.getFilters().size() + 2);
		filterQueries.addAll(searchCriteria.getFilters());
		addCategoryFilter(filterQueries, searchCriteria);
		addDisplayableOnlyFilter(query, filterQueries, searchCriteria);

		addFiltersToQuery(query, filterQueries, searchCriteria);

		addFacetsToQuery(query, searchCriteria, filterLookup);

		addDefaultSorting(query, searchCriteria);
		addInvariantTerms(query, startIndex, maxResults, getSearchConfigFactory().getSearchConfig(IndexType.PRODUCT.getIndexName()));

		return query;
	}

	@Override
	public String getSearchableAttributesFromFilterAttributes(final KeywordSearchCriteria searchCriteria, final SearchConfig searchConfig) {
		return this.filterAttributes(searchCriteria, searchConfig);
	}

	private void addAppliedFacetFilters(final SolrQuery query, final Map<String, String> appliedFacets, final Map<String, String> filterMap) {
		for (Entry<String, String> entry : appliedFacets.entrySet()) {
			String guid = entry.getKey();
			if (!guid.isEmpty() && !entry.getValue().isEmpty()) {
				StringBuilder filterQueryString = new StringBuilder();
				filterQueryString.append("{!tag=").append(guid).append('}');
				for (String facetValue : appliedFacets.get(guid).split(FacetConstants.APPLIED_FACETS_SEPARATOR)) {
					filterQueryString.append(' ').append(filterMap.get(facetValue));
				}
				query.addFilterQuery(filterQueryString.toString());
			}
		}
	}

	@Override
	public String getSearchableAttributes(final SearchConfig searchConfig, final KeywordSearchCriteria searchCriteria) {

		Map<String, Float> searchKeys = new HashMap<>();
		Locale locale = searchCriteria.getLocale();
		for (Facet facet : facetService.findAllSearchableFacets(searchCriteria.getStoreCode())) {
			int facetGroup = facet.getFacetGroup();
			if (facetGroup == FacetGroup.FIELD.getOrdinal()) {
				addFieldAttributes(searchConfig, searchKeys, locale, facet);
			} else if (facetGroup == FacetGroup.SKU_OPTION.getOrdinal()) {
				addSkuOptions(searchConfig, searchKeys, locale, facet);
			} else {
				addSkuAndProductAttributes(searchConfig, searchKeys, locale, facet);
			}
		}

		return buildQfParameter(searchKeys);
	}

	private String buildQfParameter(final Map<String, Float> searchKeys) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Entry<String, Float> entry : searchKeys.entrySet()) {
			String key = entry.getKey();
			stringBuilder.append(key);
			final BigDecimal boostValue = BigDecimal.valueOf(entry.getValue()).setScale(BOOST_SCALE, BigDecimal.ROUND_DOWN);
			if (boostValue.compareTo(BigDecimal.ONE) != 0) {
				stringBuilder.append('^').append(boostValue);
			}
			stringBuilder.append(' ');
		}
		stringBuilder.setLength(Math.max(stringBuilder.length() - 1, 0));
		return stringBuilder.toString();
	}

	private void addSkuAndProductAttributes(final SearchConfig searchConfig,
											final Map<String, Float> searchKeys, final Locale locale, final Facet facet) {
		String attributeKey = facet.getBusinessObjectId();
		Attribute attribute = getAttributeService().findByKey(attributeKey);
		String fieldName = indexUtility.createAttributeFieldName(attribute, locale, true, false);
		float boost;
		if (attribute.isLocaleDependant()) {
			boost = indexUtility.getAttributeBoostWithFallback(searchConfig, attribute, locale);
		} else {
			boost = indexUtility.getAttributeBoost(searchConfig, attribute);
		}
		searchKeys.put(fieldName, boost);
	}

	private void addSkuOptions(final SearchConfig searchConfig, final Map<String, Float> searchKeys, final Locale locale, final Facet facet) {
		String optionKey = facet.getBusinessObjectId();
		String fieldName = indexUtility.createSkuOptionFieldName(locale, facet.getBusinessObjectId());
		searchKeys.put(fieldName, indexUtility.getLocaleBoostWithFallback(searchConfig, optionKey, locale));
	}

	private void addFieldAttributes(final SearchConfig searchConfig, final Map<String, Float> searchKeys, final Locale locale, final Facet facet) {
		String fieldKey = facet.getFacetName();
		if (fieldKey.equals(FacetConstants.PRODUCT_NAME)) {
			searchKeys.put(indexUtility.createLocaleFieldName(SolrIndexConstants.PRODUCT_NAME, locale),
					indexUtility.getLocaleBoostWithFallback(searchConfig, SolrIndexConstants.PRODUCT_NAME, locale));
		} else if (fieldKey.equals(FacetConstants.PRODUCT_SKU_CODE)) {
			searchKeys.put(SolrIndexConstants.PRODUCT_SKU_CODE, searchConfig.getBoostValue(SolrIndexConstants.PRODUCT_SKU_CODE));
		} else if (fieldKey.equals(FacetConstants.BRAND)) {
			searchKeys.put(SolrIndexConstants.BRAND_NAME, searchConfig.getBoostValue(SolrIndexConstants.BRAND_NAME));
		} else if (FacetConstants.SIZE_ATTRIBUTES.contains(fieldKey)) {
			String solrAttribute = fieldKey.toLowerCase(Locale.ENGLISH);
			searchKeys.put(solrAttribute, searchConfig.getBoostValue(solrAttribute));
		} else if (FacetConstants.CATEGORY.equals(fieldKey)) {
			searchKeys.put(indexUtility.createLocaleFieldName(SolrIndexConstants.CATEGORY_NAME, locale),
					indexUtility.getLocaleBoostWithFallback(searchConfig, SolrIndexConstants.CATEGORY_NAME, locale));
		}
	}

	/**
	 * @param query solr query
	 * @param criteria search criteria
	 */
	protected void addQueryOptions(final SolrQuery query, final SearchCriteria criteria) {
		String storeContext = "";
		if (criteria instanceof StoreAwareSearchCriteria) {
			storeContext = ((StoreAwareSearchCriteria) criteria).getStoreCode();
		}
		if (getShowBundlesFirstProvider().get(storeContext)) {
			query.set("bq", CONSTITUENT_COUNT_SCALE_FORMULA);
		}
	}

	/**
	 * Adds a filter to the given list of filter queries that will specify the category on which to filter
	 * search results, if any. If no categories are specified, nothing will be added to the filter.
	 * @param filterQueries the list of filters to which the newly created filter should be added
	 * @param searchCriteria the search criteria containing the category on which to filter
	 */
	protected void addCategoryFilter(final List<Filter<?>> filterQueries, final KeywordSearchCriteria searchCriteria) {
		if (searchCriteria.getCategoryUid() != null && searchCriteria.getCategoryUid() > 0) {
			CategoryFilter catFilter = getBeanFactory().getPrototypeBean(ContextIdNames.CATEGORY_FILTER, CategoryFilter.class);
			// Create a dummy category here so that we don't need a DB call to get the actual
			// category (we don't use it)
			Category dummyCategory = getBeanFactory().getPrototypeBean(ContextIdNames.CATEGORY, Category.class);
			dummyCategory.setUidPk(searchCriteria.getCategoryUid());
			catFilter.setCategory(dummyCategory);
			filterQueries.add(catFilter);
		}
	}

	/**
	 * Adds a filter to the given list of filter queries that will specify the store on which to filter search results,
	 * if the search criteria include showing only displayable products and categories. If not, then nothing will be added
	 * to the list of queries.
	 * Calls {@link #createTermsForStartEndDateRange(Date)}.
	 * @param query the solr query to add the filter queries to
	 * @param filterQueries the list of filters to which the newly created filter will be added
	 * @param searchCriteria the search criteria containing the displayable boolean
	 */
	protected void addDisplayableOnlyFilter(final SolrQuery query, final List<Filter<?>> filterQueries,
											final KeywordSearchCriteria searchCriteria) {
		if (searchCriteria.isDisplayableOnly()) {
			DisplayableFilter displayableFilter = getBeanFactory().getPrototypeBean(ContextIdNames.DISPLAYABLE_FILTER, DisplayableFilter.class);
			displayableFilter.setStoreCode(searchCriteria.getStoreCode());
			filterQueries.add(displayableFilter);

			Date roundedDate = roundDateUpToMinute(new Date());
			final String now = getAnalyzer().analyze(roundedDate);

			// filter by start date less than or equal to now and end date greater than now
			final String starToNow = ":[* TO " + now + "] ";
			query.addFilterQuery("+" + SolrIndexConstants.START_DATE + starToNow + "-" + SolrIndexConstants.END_DATE + starToNow);
		}
	}

	/**
	 * Gets the catalog service.
	 * @return the catalog service
	 */
	protected CatalogService getCatalogService() {
		return getBeanFactory().getSingletonBean(ContextIdNames.CATALOG_SERVICE, CatalogService.class);
	}

	/**
	 * <p>Constructs a new SOLR query object of <b>type=SPELLCHECKER</b> (to be handled by SOLR's Spellchecker query handler),
	 * corresponding to the given search criteria and search configuration.</p>
	 *
	 * @param searchCriteria the search criteria
	 * @param config the search config to use for this suggestion query
	 * @return a {@link SolrQuery} spell suggestion query
	 */
	@Override
	public SolrQuery composeSpellingQuery(final SpellSuggestionSearchCriteria searchCriteria, final SearchConfig config) {
		final SolrQuery query = new SolrQuery();

		for (String keyword : searchCriteria.getPotentialMisspelledStrings()) {
			query.add(SpellingParams.QUERY, keyword);
		}

		query.setRequestHandler(SPELLING_REQUEST_HANDLER);
		query.set(SpellingParams.ACCURACY, String.valueOf(config.getAccuracy()));
		query.set(SpellingParams.NUM_SUGGESTIONS, String.valueOf(config.getMaximumSuggestionsPerWord()));
		query.set(SpellingParams.LOCALE, searchCriteria.getLocale().toString());

		return query;
	}

	/**
	 * Adds invariant terms to the given solr query.
	 * @param query the query to which invariant terms should be added
	 * @param startIndex the start inded
	 * @param maxResults the max results
	 * @param searchConfig the search configuration
	 */
	void addInvariantTerms(final SolrQuery query, final int startIndex, final int maxResults,
						   final SearchConfig searchConfig) {
		query.setStart(startIndex);

		int maxReturnNum = maxResults;
		if (searchConfig.getMaxReturnNumber() > 0) {
			maxReturnNum = Math.max(0, Math.min(maxResults, searchConfig.getMaxReturnNumber() - startIndex));
		}
		query.setRows(maxReturnNum);
		query.setFields(SolrIndexConstants.OBJECT_UID);
	}


	private void addDefaultSorting(final SolrQuery query, final SearchCriteria searchCriteria) {
		addSorting(query, searchCriteria, null, null);
	}

	private void addSorting(final SolrQuery query, final SearchCriteria searchCriteria,
							final QueryComposer luceneQueryComposer, final SearchConfig searchConfig) {
		if (searchCriteria.getSortingType() == null || searchCriteria.getSortingOrder() == null || sortingRedundant(searchCriteria)) {
			return;
		}

		final String legacySortField = getSortTypeField(searchCriteria);

		Map<String, SortOrder> sortFields = null;

		if (legacySortField == null) {
			if (luceneQueryComposer == null) {
				sortFields = new LinkedHashMap<>();
			} else {
				sortFields = luceneQueryComposer.resolveSortField(searchCriteria, searchConfig);
			}
		} else {
			sortFields = new LinkedHashMap<>();
			sortFields.put(legacySortField, searchCriteria.getSortingOrder());
		}

		if (MapUtils.isEmpty(sortFields)) {
			throw new EpSystemException(String.format("Sort field %1$S unimplemented.", searchCriteria.getSortingType()));
		}

		for (Entry<String, SortOrder> sortField : sortFields.entrySet()) {
			query.addSort(sortField.getKey(), resolveSortingOrder(sortField.getValue()));
		}

		handleFeaturedCategorySpecialCase(query, searchCriteria);
	}

	private boolean sortingRedundant(final SearchCriteria searchCriteria) {
		// this is the default sorting for SOLR, don't need to do anything
		return StandardSortBy.RELEVANCE.equals(searchCriteria.getSortingType()) && searchCriteria.getSortingOrder() == SortOrder.DESCENDING;
	}

	/**
	 * Special case for featured in category so that featured products not in the category will still be ranked higher than products that aren't
	 * featured at all.
	 */
	private void handleFeaturedCategorySpecialCase(final SolrQuery query, final SearchCriteria searchCriteria) {
		final ORDER sortOrder = resolveSortingOrder(searchCriteria.getSortingOrder());
		if (StandardSortBy.FEATURED_CATEGORY.equals(searchCriteria.getSortingType())) {
			SortBy tmpType = searchCriteria.getSortingType();

			searchCriteria.setSortingType(StandardSortBy.FEATURED_ANYWHERE);
			query.addSort(getSortTypeField(searchCriteria), sortOrder);

			searchCriteria.setSortingType(tmpType);
		}
	}

	private ORDER resolveSortingOrder(final SortOrder sortingOrder) {
		ORDER resultSortOrder = ORDER.desc;
		if (sortingOrder == SortOrder.ASCENDING) {
			resultSortOrder = ORDER.asc;
		}
		return resultSortOrder;
	}

	/**
	 * Provides default sorting field detection. Null value returned indicates that the sorting field can not be handled by the default
	 * implementation. This means in turn that particular composer may attempt to resolve the sorting field.
	 *
	 * @param searchCriteria search criteria to derive sorting field from
	 * @return Solr field to sort against to null indicating that further resolving is required
	 */
	private String getSortTypeField(final SearchCriteria searchCriteria) {
		switch (searchCriteria.getSortingType().getOrdinal()) {
			case StandardSortBy.PRICE_ORDINAL:
				return getSortQueryForPrice(searchCriteria);
			case StandardSortBy.PRODUCT_NAME_ORDINAL:
				// need exact name here, otherwise we sort on the lowest or highest value of the
				// tokens
				return indexUtility.createLocaleFieldName(SolrIndexConstants.SORT_PRODUCT_NAME_EXACT, searchCriteria.getLocale());
			case StandardSortBy.RELEVANCE_ORDINAL:
				return SolrIndexConstants.SCORE;
			case StandardSortBy.TOP_SELLER_ORDINAL:
				return SolrIndexConstants.SALES_COUNT;
			case StandardSortBy.FEATURED_ANYWHERE_ORDINAL:
				return SolrIndexConstants.FEATURED;
			case StandardSortBy.FEATURED_CATEGORY_ORDINAL:
				// this sorting ordering should only happen for instances of ProductCategorySearchCriteria
				ProductCategorySearchCriteria prodCatSearchCriteria = (ProductCategorySearchCriteria) searchCriteria;
				if (prodCatSearchCriteria.getCategoryUid() == null || prodCatSearchCriteria.getCategoryUid() <= 0) {
					// assume that if we aren't in a category, we want them all
					return SolrIndexConstants.FEATURED;
				}
				return indexUtility.createFeaturedField(prodCatSearchCriteria.getCategoryUid());
			case StandardSortBy.ATTRIBUTE_ORDINAL:
				return getSortQueryForAttribute(searchCriteria);
			default:
				return null;
		}
	}

	private String getSortQueryForPrice(final SearchCriteria searchCriteria) {
		if (!(searchCriteria instanceof ProductCategorySearchCriteria)) {
			throw new EpServiceException("Search criteria must be of type ProductCategorySearchCriteria to sort on price");
		}

		SearchHint<PriceListStack> priceListStackHint = searchCriteria.getSearchHint(PRICE_LIST_STACK);
		String catalogCode = ((ProductCategorySearchCriteria) searchCriteria).getCatalogCode();
		List<String> priceListStack;

		if (priceListStackHint == null) {
			PriceListAssignmentService priceListAssignmentService = beanFactory.getSingletonBean(ContextIdNames.PRICE_LIST_ASSIGNMENT_SERVICE,
					PriceListAssignmentService.class);
			Currency currency = searchCriteria.getCurrency();
			priceListStack = priceListAssignmentService.listByCatalogAndCurrencyCode(catalogCode, currency.getCurrencyCode())
					.stream().map(priceListAssignment -> priceListAssignment.getPriceListDescriptor().getGuid())
					.collect(Collectors.toList());
		} else {
			priceListStack = priceListStackHint.getValue().getPriceListStack();
		}

		return indexUtility.createPriceSortFieldName(SolrIndexConstants.PRICE_SORT, catalogCode, priceListStack);
	}

	private String getSortQueryForAttribute(final SearchCriteria searchCriteria) {
		String sortString = searchCriteria.getSortingType().getSortString();

		Attribute attribute = attributeService.findByKey(sortString);
		if (attribute.isMultiValueEnabled()) {
			throw new EpServiceException("Cannot sort on a multi-valued attribute.");
		}

		if (attribute.getAttributeUsage().getValue() != AttributeUsage.PRODUCT) {
			throw new EpServiceException("Only product attributes can be sorted.");
		}

		return indexUtility.createAttributeDocValues(attribute, searchCriteria.getLocale());
	}

	/**
	 * Generates a SOLR date range query to only selected active products, thus having the current date fall
	 * between its start and end dates. Products may or may not have an end date.
	 *
	 * <p>Example SOLR query: startDate:[* TO NOW] OR -endDate:[* TO NOW]
	 *
	 * @param date The date the start and end date of the product must fall in.
	 * @return the query for the start and date range based on the given date
	 */
	@Override
	public BooleanQuery createTermsForStartEndDateRange(final Date date) {
		final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

		// round to the nearest minute to improve query caching
		// to the second granularity is an overkill
		Date roundedDate = roundDateUpToMinute(date);
		final String now = getAnalyzer().analyze(roundedDate);

		// start date is in the past
		booleanQueryBuilder.add(TermRangeQuery.newStringRange(SolrIndexConstants.START_DATE, null, now, true, true), Occur.MUST);
		// AND end date is NOT in the past - correctly evaluates possible null end dates
		booleanQueryBuilder.add(TermRangeQuery.newStringRange(SolrIndexConstants.END_DATE, null, now, true, true), Occur.MUST_NOT);

		return booleanQueryBuilder.build();
	}

	/**
	 * Round date up to minute.
	 *
	 * @param date the date to round.
	 * @return the date rounded up to the next minute
	 */
	protected Date roundDateUpToMinute(final Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.MINUTE, 1);
		return calendar.getTime();
	}

	/**
	 * Sets the {@link IndexUtility} instance to use.
	 *
	 * @param indexUtility the {@link IndexUtility} instance to use
	 */
	public void setIndexUtility(final IndexUtility indexUtility) {
		this.indexUtility = indexUtility;
	}

	/**
	 * Sets the {@link SolrFacetAdapter} instance to use.
	 *
	 * @param solrFacetAdapter the {@link SolrFacetAdapter} instance to use
	 */
	public void setSolrFacetAdapter(final SolrFacetAdapter solrFacetAdapter) {
		this.solrFacetAdapter = solrFacetAdapter;
	}

	protected SettingValueProvider<Boolean> getShowBundlesFirstProvider() {
		return showBundlesFirstProvider;
	}

	public void setShowBundlesFirstProvider(final SettingValueProvider<Boolean> showBundlesFirstProvider) {
		this.showBundlesFirstProvider = showBundlesFirstProvider;
	}

	/**
	 * Get the search config factory used to get a search configuration.
	 *
	 * @return the <code>SearchConfigFactory</code>
	 */
	protected SearchConfigFactory getSearchConfigFactory() {
		return searchConfigFactory;
	}

	/**
	 * Set the search config factory used to get a search configuration.
	 *
	 * @param searchConfigFactory the <code>SearchConfigFactory</code> to set
	 */
	public void setSearchConfigFactory(final SearchConfigFactory searchConfigFactory) {
		this.searchConfigFactory = searchConfigFactory;
	}

	/**
	 * Get the bean factory.
	 *
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}


	/**
	 * Set the bean factory.
	 *
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * @return the analyzer
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * @param analyzer the analyzer to set
	 */
	public void setAnalyzer(final Analyzer analyzer) {
		this.analyzer = analyzer;
	}


	private String filterAttributes(final KeywordSearchCriteria searchCriteria, final SearchConfig searchConfig) {
		final List<Attribute> attributeList = getAttributeService().getProductAttributes();
		final List<String> searchKeys = new ArrayList<>(attributeList.size());
		final Set<String> excludedItems = searchConfig.getExclusiveAttributes();
		final List<Float> boostValues = new ArrayList<>(attributeList.size());

		for (Attribute attribute : attributeList) {
			if (excludedItems.contains(attribute.getKey())) {
				continue;
			}

			final String fieldName = indexUtility
					.createAttributeFieldName(attribute, searchCriteria.getLocale(), true, false);
			searchKeys.add(fieldName);
			if (attribute.isLocaleDependant()) {
				boostValues.add(indexUtility.getAttributeBoostWithFallback(searchConfig, attribute, searchCriteria.getLocale()));
			} else {
				boostValues.add(indexUtility.getAttributeBoost(searchConfig, attribute));
			}
		}

		searchKeys.add(indexUtility.createLocaleFieldName(SolrIndexConstants.PRODUCT_NAME, searchCriteria.getLocale()));
		boostValues.add(indexUtility.getLocaleBoostWithFallback(searchConfig, SolrIndexConstants.PRODUCT_NAME, searchCriteria
				.getLocale()));
		searchKeys.add(SolrIndexConstants.PRODUCT_SKU_CODE);
		boostValues.add(searchConfig.getBoostValue(SolrIndexConstants.PRODUCT_SKU_CODE));
		searchKeys.add(SolrIndexConstants.BRAND_CODE_FOR_DISMAX);
		boostValues.add(searchConfig.getBoostValue(SolrIndexConstants.BRAND_CODE_FOR_DISMAX));
		searchKeys.add(SolrIndexConstants.BRAND_NAME);
		boostValues.add(searchConfig.getBoostValue(SolrIndexConstants.BRAND_NAME));

		final StringBuilder builder = new StringBuilder();
		Iterator<String> fieldIter = searchKeys.iterator();
		Iterator<Float> boostIter = boostValues.iterator();

		while (fieldIter.hasNext()) {
			builder.append(fieldIter.next());
			final BigDecimal boostValue = BigDecimal.valueOf(boostIter.next()).setScale(BOOST_SCALE, BigDecimal.ROUND_DOWN);
			if (boostValue.compareTo(BigDecimal.ONE) != 0) {
				builder.append('^').append(boostValue);
			}
			builder.append(' ');
		}
		builder.deleteCharAt(builder.length() - 1);

		return builder.toString();
	}

	private AttributeService getAttributeService() {
		return this.attributeService;
	}

	public void setAttributeService(final AttributeService attributeService) {
		this.attributeService = attributeService;
	}

	public void setFacetService(final FacetService facetService) {
		this.facetService = facetService;
	}
}
