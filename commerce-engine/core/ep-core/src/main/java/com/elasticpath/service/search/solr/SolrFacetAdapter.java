/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.search.solr;

import static com.elasticpath.commons.constants.ContextIdNames.PRICE_LIST_STACK;
import static com.elasticpath.service.search.solr.FacetConstants.BRAND;
import static com.elasticpath.service.search.solr.FacetConstants.CATEGORY;
import static com.elasticpath.service.search.solr.FacetConstants.PRICE;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.client.solrj.SolrQuery;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalogview.AdvancedSearchFilteredNavSeparatorFilter;
import com.elasticpath.domain.catalogview.AttributeKeywordFilter;
import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.BrandFilter;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.DisplayableFilter;
import com.elasticpath.domain.catalogview.FeaturedProductFilter;
import com.elasticpath.domain.catalogview.Filter;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.RangeFilter;
import com.elasticpath.domain.catalogview.RangeFilterType;
import com.elasticpath.domain.catalogview.SizeRangeFilter;
import com.elasticpath.domain.catalogview.SkuOptionValueFilter;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.service.catalog.CategoryService;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfiguration;
import com.elasticpath.service.catalogview.filterednavigation.FilteredNavigationConfigurationLoader;
import com.elasticpath.service.pricing.PriceListAssignmentService;
import com.elasticpath.service.search.CatalogAwareSearchCriteria;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.service.search.StoreAwareSearchCriteria;
import com.elasticpath.service.search.index.Analyzer;
import com.elasticpath.service.search.query.ProductSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.query.SearchHint;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Converts between EP's filters and Solr's facets and back again.
 */
@SuppressWarnings("PMD.GodClass")
public class SolrFacetAdapter {
	private static final Logger LOG = Logger.getLogger(SolrFacetAdapter.class);

	private static final String EXCLUDE_TAG = "{!ex=%s}%s";

	private static final String COLON_BRACKET = ":[";
	private static final String BETWEEN_TO = " TO ";
	private static final char CURLY_BRACKET = '}';
	private static final String ASTERISK = "*";

	private FilteredNavigationConfiguration config;

	private FilteredNavigationConfigurationLoader fncLoader;

	private IndexUtility indexUtility;

	private Analyzer analyzer;

	private SettingValueProvider<Boolean> attributeFilterEnabledProvider;

	private CategoryService categoryService;

	private FacetService facetService;

	private BeanFactory beanFactory;

	/**
	 * Gets the <code>FilteredNavigationConfiguration</code> for the <code>Store</code>
	 * with the given StoreCode, using this instance's <code>FilteredNavigationConfigurationLoader</code>.
	 * @param storeCode the code the store whose filtered navigation configuration should be retrieved
	 * @return the FilteredNavigationConfiguration object for Store with the given StoreCode
	 */
	FilteredNavigationConfiguration getFilteredNavigationConfiguration(final String storeCode) {
		return getFncLoader().loadFilteredNavigationConfiguration(storeCode);
	}

	/**
	 * Adds the configured facets to the specified query.
	 *
	 * @param query the query to add the faceting information to.
	 * @param searchCriteria the Locale, Currency, and StoreCode are retrieved from here.
	 * @param queryLookup a map keyed on filter id and a value containing the solr query filter.
	 * @param filterLookup a map keyed on solr query with a value containing the filter.
	 */
	public void addFacets(final SolrQuery query, final SearchCriteria searchCriteria, final Map<String, String> queryLookup,
						  final Map<String, Filter<?>> filterLookup) {
		LOG.debug("Adding facets to SOLR query.");
		if (searchCriteria instanceof StoreAwareSearchCriteria) {
			final Locale locale = searchCriteria.getLocale();
			final String storeCode = ((StoreAwareSearchCriteria) searchCriteria).getStoreCode();
			String catalogCode = null;

			if (searchCriteria instanceof CatalogAwareSearchCriteria) {
				catalogCode = ((CatalogAwareSearchCriteria) searchCriteria).getCatalogCode();
			}

			config = this.getFilteredNavigationConfiguration(storeCode);

			addBrandFacets(query, queryLookup, filterLookup);

			if (catalogCode != null) {
				Long categoryUid = searchCriteria instanceof ProductSearchCriteria
						? ((ProductSearchCriteria) searchCriteria).getDirectCategoryUid() : null;
				addCategoryFacets(query, catalogCode, queryLookup, filterLookup, categoryUid);
			}

			addFacetValues(query, locale, queryLookup, filterLookup);

			addPriceFacets(query, catalogCode, searchCriteria.getCurrency(), queryLookup, filterLookup,
					searchCriteria.getSearchHint(PRICE_LIST_STACK));

			addRangeFacets(query, locale, queryLookup, filterLookup);

			addSkuOptionFacets(query, locale, queryLookup, filterLookup);

			addSizeFacets(query, queryLookup, filterLookup);
		}
	}

	/**
	 *
	 * @param searchCriteria the search criteria.
	 * @return THe list of price list stacks.
	 */
	private List<String> getPriceListStackFromSearchCriteria(final SearchCriteria searchCriteria) {
		SearchHint<PriceListStack> priceListStackHint = searchCriteria.getSearchHint("priceListStack");
		if (priceListStackHint != null) {
			return priceListStackHint.getValue().getPriceListStack();
		}
		return Collections.emptyList();
	}

	/**
	 * Constructs a Lucene {@link Query} from the given {@link Filter}.
	 *
	 * @param filter the filter
	 * @param searchCriteria the search criteria
	 * if the filter being constructed is not for a store-specific search field (e.g. Price).
	 * @return a {@link Query}
	 */
	public String constructFilterQuery(final Filter<?> filter, final SearchCriteria searchCriteria) {
		String queries = null;

		if (filter instanceof PriceFilter) {
			final String catalogCode = getCatalogCodeFromSearchCriteria(searchCriteria);
			return getPriceQueryForStack((PriceFilter) filter, catalogCode, getPriceListStackFromSearchCriteria(searchCriteria));

		} else if (filter instanceof AttributeRangeFilter) {
			queries = constructAttributeRangeFilterQuery((AttributeRangeFilter) filter);

		} else if (filter instanceof BrandFilter) {
			queries = constructBrandFilterQuery((BrandFilter) filter).toString();

		} else if (filter instanceof AttributeValueFilter) {
			queries = constructAttributeValueFilterQuery((AttributeValueFilter) filter, true);

		} else if (filter instanceof AttributeKeywordFilter) {
			queries = constructAttributeKeywordFilterQuery((AttributeKeywordFilter) filter, searchCriteria.getLocale(), true).toString();

		} else if (filter instanceof CategoryFilter) {
			final String catalogCode = getCatalogCodeFromSearchCriteria(searchCriteria);
			queries = constructCategoryFilterQuery((CategoryFilter) filter, catalogCode).toString();

		} else if (filter instanceof FeaturedProductFilter) {
			final String catalogCode = getCatalogCodeFromSearchCriteria(searchCriteria);
			queries = constructFeaturedProductFilterQuery((FeaturedProductFilter) filter, catalogCode).toString();

		} else if (filter instanceof DisplayableFilter) {
			queries = constructDisplayableOnlyFilterQuery((DisplayableFilter) filter).toString();

		} else if (filter instanceof AdvancedSearchFilteredNavSeparatorFilter) {
			queries = "";
		} else if (filter instanceof SkuOptionValueFilter) {
			queries = constructSkuOptionValueFilterQuery((SkuOptionValueFilter) filter, searchCriteria.getLocale());
		}

		if (queries == null) {
			throw new EpSystemException("Unimplemented filter type: " + filter.getClass());
		}

		return queries;
	}

	/**
	 * Get the catalog code from the given search criteria in case it is of type
	 * {@link CatalogAwareSearchCriteria}.
	 *
	 * @param searchCriteria the search criteria
	 * @return the catalog code or null if criteria does not support catalog codes
	 */
	String getCatalogCodeFromSearchCriteria(final SearchCriteria searchCriteria) {
		if (searchCriteria instanceof CatalogAwareSearchCriteria) {
			return ((CatalogAwareSearchCriteria) searchCriteria).getCatalogCode();
		}
		return null;
	}

	/**
	 * Gets a price query for the given price list stack.
	 * @param priceFilter the filter to use
	 * @param catalogCode the catalog code
	 * @param stackGuids the guids of the price lists on the stack
	 * @return a facet query string
	 */
	protected String getPriceQueryForStack(final PriceFilter priceFilter, final String catalogCode, final List<String> stackGuids) {
		if (stackGuids == null || stackGuids.isEmpty()) {
			return EMPTY;
		}
		String priceFieldPrefix = getIndexUtility().createPriceFieldName(SolrIndexConstants.PRICE, catalogCode, EMPTY);
		return stackGuids.stream().map(guid -> buildRangeFacetQuery(priceFilter, priceFieldPrefix + guid))
				.collect(Collectors.joining(" "));
	}

	private void addLuceneQuery(final SolrQuery query, final Filter<?> filter, final String luceneQuery, final String guid,
								final Map<String, String> queryLookup, final Map<String, Filter<?>> filterLookup) {
		String tagQuery = String.format(EXCLUDE_TAG, guid, luceneQuery);
		filterLookup.put(tagQuery, filter);
		queryLookup.put(filter.getId(), luceneQuery);
		query.addFacetQuery(tagQuery);
	}

	private void addSizeFacets(final SolrQuery query, final Map<String, String> queryLookup, final Map<String, Filter<?>> filterLookup) {
		Collection<SizeRangeFilter> sizeRangeFilters = config.getAllSizeRangeFilters().values();

		sizeRangeFilters.forEach(sizeRangeFilter -> {
			String guid = config.getOthersGuidMap().get(sizeRangeFilter.getSizeType().getLabel());
			addLuceneQuery(query, sizeRangeFilter, constructSizeFilterQuery(sizeRangeFilter), guid, queryLookup, filterLookup);
		});
	}

	private void addSkuOptionFacets(final SolrQuery query, final Locale locale, final Map<String, String> queryLookup,
									final Map<String, Filter<?>> filterLookup) {
		Collection<SkuOptionValueFilter> skuOptionValueFilters = config.getAllSkuOptionValueFilters().values();
		skuOptionValueFilters.forEach(skuOptionValueFilter -> {
			String guid = config.getSkuOptionGuidMap().get(skuOptionValueFilter.getSkuOptionValue().getSkuOption().getOptionKey());
			addLuceneQuery(query, skuOptionValueFilter, constructSkuOptionValueFilterQuery(skuOptionValueFilter, locale), guid,
					queryLookup, filterLookup);
		});
	}

	private void addRangeFacets(final SolrQuery query, final Locale locale, final Map<String, String> queryLookup,
								final Map<String, Filter<?>> filterLookup) {
		Collection<AttributeRangeFilter> attrRangeFilters = config.getAllAttributeRanges().values();
		// don't want filters for locales we aren't searching for
		// remove filters that do nothing (filter then filters nothing)
		attrRangeFilters.stream()
				.filter(attrRangeFilter -> !attrRangeFilter.getAttribute().isLocaleDependant() || Objects.equals(attrRangeFilter.getLocale(),
						locale))
				.filter(attrRangeFilter -> attrRangeFilter.getRangeType() != RangeFilterType.ALL)
				.forEach(attrRangeFilter -> {
					String guid = config.getAttributeGuidMap().get(attrRangeFilter.getAttributeKey());
					addLuceneQuery(query, attrRangeFilter, constructAttributeRangeFilterQuery(attrRangeFilter), guid, queryLookup, filterLookup);
				});
	}

	private void addPriceFacets(final SolrQuery query, final String catalogCode, final Currency currency, final Map<String, String> queryLookup,
								final Map<String, Filter<?>> filterLookup, final SearchHint<PriceListStack> priceListStackHint) {
		if (catalogCode == null || currency == null) {
			return;
		}

		List<String> priceListGuids;

		if (priceListStackHint == null) {
			PriceListAssignmentService priceListAssignmentService = beanFactory.getSingletonBean(ContextIdNames.PRICE_LIST_ASSIGNMENT_SERVICE,
					PriceListAssignmentService.class);
			priceListGuids = priceListAssignmentService.listByCatalogAndCurrencyCode(catalogCode, currency.getCurrencyCode())
					.stream().map(priceListAssignment -> priceListAssignment.getPriceListDescriptor().getGuid())
					.collect(Collectors.toList());
		} else {
			priceListGuids = priceListStackHint.getValue().getPriceListStack();
		}

		Collection<PriceFilter> priceFilters = config.getAllPriceRanges().values();
		// don't want filters for currencies we aren't searching for
		// remove filters that do nothing (filter then filters nothing)
		priceFilters.stream()
				.filter(priceFilter -> priceFilter.getCurrency().equals(currency))
				.filter(priceFilter -> priceFilter.getRangeType() != RangeFilterType.ALL)
				.forEach(priceFilter -> {
					String guid = config.getOthersGuidMap().get(PRICE);
					addLuceneQuery(query, priceFilter, getPriceQueryForStack(priceFilter, catalogCode, priceListGuids), guid,
							queryLookup, filterLookup);
				});
	}

	private void addFacetValues(final SolrQuery query, final Locale locale, final Map<String, String> queryLookup,
								final Map<String, Filter<?>> filterLookup) {
		Collection<AttributeValueFilter> attrValueFilters = config.getAllAttributeSimpleValues().values();
		// remove the filters used as unique keys - they don't have attribute values
		// don't want to filter for locales we aren't searching for
		attrValueFilters.stream()
				.filter(attrValueFilter -> attrValueFilter.getAttributeValue() != null)
				.filter(attrValueFilter -> !attrValueFilter.isLocalized()
				|| attrValueFilter.getLocale().equals(locale))
				.forEach(attrValueFilter -> {
					String guid = config.getAttributeGuidMap().get(attrValueFilter.getAttributeKey());
					addLuceneQuery(query, attrValueFilter, constructAttributeValueFilterQuery(attrValueFilter, true), guid,
							queryLookup, filterLookup);
				});
	}

	private String constructSizeFilterQuery(final SizeRangeFilter sizeRangeFilter) {
		return buildRangeFacetQuery(sizeRangeFilter, sizeRangeFilter.getSizeType().getLabel().toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Adds the Brand facets to the given query.
	 *
	 * @param query        the query
	 * @param queryLookup  map keyed on filter id with a value containing the solr query filter
	 * @param filterLookup map keyed on solr query filter with a value containing the filter
	 */
	void addBrandFacets(final SolrQuery query, final Map<String, String> queryLookup, final Map<String, Filter<?>> filterLookup) {
		String guid = config.getOthersGuidMap().get(BRAND);
		config.getBrandFilters().forEach(brandFilter ->
				addLuceneQuery(query, brandFilter, constructBrandFilterQuery(brandFilter).toString(), guid, queryLookup, filterLookup));
	}

	/**
	 * Adds the Category facets to the given query.
	 *
	 * @param query         the query
	 * @param catalogCode   the catalog code
	 * @param queryLookup   map keyed on filter id with a value containing the solr query filter
	 * @param filterLookup  map keyed on solr query filter with a value containing the filter
	 * @param categoryUid   the category uid used for a product search criteria, null otherwise
	 */
	void addCategoryFacets(final SolrQuery query, final String catalogCode, final Map<String, String> queryLookup,
						   final Map<String, Filter<?>> filterLookup, final Long categoryUid) {
		String guid = config.getOthersGuidMap().get(CATEGORY);
		if (categoryUid == null) {
			config.getCategoryFilters().forEach(categoryFilter ->
					addLuceneQuery(query, categoryFilter,
							constructCategoryFilterQuery(categoryFilter, catalogCode).toString(), guid, queryLookup, filterLookup));
		} else {
			Set<Long> descendants = new HashSet<>(getCategoryService().findDescendantCategoryUids(categoryUid));
			config.getCategoryFilters().stream()
					.filter(categoryFilter -> descendants.contains(categoryFilter.getCategory().getUidPk()))
					.forEach(categoryFilter -> addLuceneQuery(query, categoryFilter,
							constructCategoryFilterQuery(categoryFilter, catalogCode).toString(), guid, queryLookup, filterLookup));
		}
	}

	private String constructAttributeRangeFilterQuery(final AttributeRangeFilter filter) {
		Attribute attribute = filter.getAttribute();
		String fieldName = getIndexUtility().createAttributeFieldName(attribute, filter.getLocale(), false, true);
		return buildRangeFacetQuery(filter, fieldName);
	}

	private String constructSkuOptionValueFilterQuery(final SkuOptionValueFilter filter, final Locale locale) {
		SkuOptionValue skuOptionValue = filter.getSkuOptionValue();
		SkuOption skuOption = skuOptionValue.getSkuOption();
		String optionKey = skuOption.getOptionKey();
		String fieldName = indexUtility.createSkuOptionFieldName(locale, optionKey);
		return new TermQuery(new Term(fieldName, analyzer.analyze(skuOptionValue.getDisplayName(locale, true), true))).toString();
	}

	/**
	 * Construct a range facet solr query.
	 * @param filter the range filter
	 * @param fieldName field name to be prefixed in the query
	 * @return range facet solr query
	 */
	protected String buildRangeFacetQuery(final RangeFilter<?, ?> filter, final String fieldName) {
		switch (filter.getRangeType()) {
			case BETWEEN:
				String lowerValue = filter.getLowerValue().toString();
				String upperValue = filter.getUpperValue().toString();

				return buildRangeQuery(fieldName, lowerValue, upperValue);
			case LESS_THAN:
				upperValue = filter.getUpperValue().toString();
				return buildRangeQuery(fieldName, ASTERISK, upperValue);
			case MORE_THAN:
				lowerValue = filter.getLowerValue().toString();
				return buildRangeQuery(fieldName, lowerValue, ASTERISK);
			default:
				break;
		}

		return null;
	}

	private String buildRangeQuery(final String fieldName, final String lowerValue, final String upperValue) {
		return fieldName + COLON_BRACKET + lowerValue + BETWEEN_TO + upperValue + CURLY_BRACKET;
	}

	/**
	 * Construct a query out of the given brand filter.
	 *
	 * @param filter the brand filter.
	 * @return the query.
	 */
	protected Query constructBrandFilterQuery(final BrandFilter filter) {
		if (filter != null && filter.getBrands() != null && !filter.getBrands().isEmpty()) {
			Iterator<Brand> iterator = filter.getBrands().iterator();
			final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
			while (iterator.hasNext()) {
				Brand brand = iterator.next();
				TermQuery term =
						new TermQuery(new Term(SolrIndexConstants.BRAND_CODE, getAnalyzer().analyze(brand.getCode())));
				queryBuilder.add(term, Occur.SHOULD);
			}
			return queryBuilder.build();
		}
		return null;
	}

	/**
	 * Constructs attribute value query.
	 *
	 * @param filter filter
	 * @param faceting true if faceting is used
	 * @return constructed query
	 */
	String constructAttributeValueFilterQuery(final AttributeValueFilter filter, final boolean faceting) {
		final Attribute attribute = filter.getAttribute();
		return new TermQuery(new Term(indexUtility.createAttributeFieldName(attribute, filter.getLocale(), faceting,
				faceting), analyzer.analyze(filter.getAttributeValue().getStringValue(), true))).toString();
	}

	/**
	 * Constructs attribute keyword query.
	 *
	 * @param filter filter
	 * @param locale locale
	 * @param faceting true if faceting is used
	 * @return constructed query
	 */
	Query constructAttributeKeywordFilterQuery(final AttributeKeywordFilter filter, final Locale locale, final boolean faceting) {

		final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		String fieldName = getIndexUtility().createAttributeFieldName(filter.getAttribute(), locale, faceting, false);

		String keywords = filter.getAttributeValue().getStringValue();
		if (StringUtils.isNotEmpty(keywords)) {
			for (String keyword : StringUtils.split(keywords)) {
				Query keywordQuery = new TermQuery(new Term(fieldName, analyzer.analyze(keyword, true)));

				queryBuilder.add(keywordQuery, Occur.MUST);
			}
		}

		return queryBuilder.build();
	}

	private Query constructCategoryFilterQuery(final CategoryFilter filter, final String catalogCode) {
		if (catalogCode == null) {
			throw new EpServiceException("The catalog code cannot be null to filter on Category");
		}

		final String categoryCode = this.getCategoryCodeFromCategoryUid(filter.getCategory().getUidPk());

		final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		final Query currentCategoryQuery = new TermQuery(
				new Term(getIndexUtility().createProductCategoryFieldName(SolrIndexConstants.PRODUCT_CATEGORY, catalogCode),
						getAnalyzer().analyze(categoryCode)));
		final Query parentCategoryQuery = new TermQuery(new Term(SolrIndexConstants.PARENT_CATEGORY_CODES,
				getAnalyzer().analyze(categoryCode)));

		queryBuilder.add(currentCategoryQuery, Occur.SHOULD);
		queryBuilder.add(parentCategoryQuery, Occur.SHOULD);

		return queryBuilder.build();
	}

	private Query constructFeaturedProductFilterQuery(final FeaturedProductFilter filter, final String catalogCode) {
		if (catalogCode == null) {
			throw new EpServiceException("The store code cannot be null to filter on featured product");
		}

		final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.add(new TermQuery(new Term(SolrIndexConstants.FEATURED, String.valueOf(true))), Occur.MUST);

		if (filter.getCategoryUid() != null && filter.getCategoryUid() > 0) {
			final BooleanQuery.Builder innerQueryBuilder = new BooleanQuery.Builder();

			// case 1 - not featured in this category, but it's featured in a sub-category
			final BooleanQuery.Builder innerSubQueryBuilder = new BooleanQuery.Builder();
			innerSubQueryBuilder.add(new TermQuery(
					new Term(getIndexUtility().createFeaturedField(filter.getCategoryUid()), String.valueOf(0))), Occur.MUST);
			final String categoryCode = getCategoryCodeFromCategoryUid(filter.getCategoryUid());

			innerSubQueryBuilder.add(new TermQuery(new Term(SolrIndexConstants.PARENT_CATEGORY_CODES, categoryCode)), Occur.MUST);

			// case 2 - it is featured in this category and apart of this category
			final BooleanQuery.Builder innerSubQuery2Builder = new BooleanQuery.Builder();
			innerSubQuery2Builder.add(
					new TermQuery(
							new Term(getIndexUtility().createFeaturedField(filter.getCategoryUid()), String.valueOf(0))), Occur.MUST_NOT);
			// have to explicitly say it's in this category or in combination with the other inner
			// query will give invalid results
			innerSubQuery2Builder.add(new TermQuery(
							new Term(getIndexUtility().createProductCategoryFieldName(SolrIndexConstants.PRODUCT_CATEGORY, catalogCode),
									categoryCode)),
					Occur.MUST);

			innerQueryBuilder.add(innerSubQueryBuilder.build(), Occur.SHOULD);
			innerQueryBuilder.add(innerSubQuery2Builder.build(), Occur.SHOULD);
			queryBuilder.add(innerQueryBuilder.build(), Occur.MUST);
		}

		return queryBuilder.build();
	}

	/**
	 * Gets the category code given a category UID.
	 * @param categoryUid the category UID
	 * @return the category code from the category specified in the search criteria
	 */
	private String getCategoryCodeFromCategoryUid(final long categoryUid) {
		return getCategoryService().findCodeByUid(categoryUid);
	}

	private Query constructDisplayableOnlyFilterQuery(final DisplayableFilter filter) {
		BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

		Query termQuery = new TermQuery(
				new Term(getIndexUtility().createDisplayableFieldName(SolrIndexConstants.DISPLAYABLE, filter.getStoreCode()), String.valueOf(true)));

		booleanQueryBuilder.add(termQuery, Occur.MUST);

		return booleanQueryBuilder.build();
	}

	/**
	 * Sets the {@link CategoryService} instance to use.
	 *
	 * @param categoryService the {@link CategoryService} instance to use
	 */
	public void setCategoryService(final CategoryService categoryService) {
		this.categoryService = categoryService;
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
	 * Return the {@link IndexUtility} instance.
	 * @return the index utility instance
	 */
	protected IndexUtility getIndexUtility() {
		return indexUtility;
	}

	/**
	 * Sets the {@link Analyzer} instance to use.
	 *
	 * @param analyzer the {@link Analyzer} instance to use
	 */
	public void setAnalyzer(final Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	/**
	 * Return the {@link Analyzer} instance.
	 * @return the analyzer instance
	 */
	protected Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * @return the fncLoader
	 */
	public FilteredNavigationConfigurationLoader getFncLoader() {
		return fncLoader;
	}

	/**
	 * @param fncLoader the fncLoader to set
	 */
	public void setFncLoader(final FilteredNavigationConfigurationLoader fncLoader) {
		this.fncLoader = fncLoader;
	}

	/**
	 * @return the catagory service
	 */
	protected CategoryService getCategoryService() {
		return categoryService;
	}

	protected SettingValueProvider<Boolean> getAttributeFilterEnabledProvider() {
		return attributeFilterEnabledProvider;
	}

	public void setAttributeFilterEnabledProvider(final SettingValueProvider<Boolean> attributeFilterEnabledProvider) {
		this.attributeFilterEnabledProvider = attributeFilterEnabledProvider;
	}

	public FacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(final FacetService facetService) {
		this.facetService = facetService;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public FilteredNavigationConfiguration getConfig() {
		return config;
	}

	public void setConfig(final FilteredNavigationConfiguration config) {
		this.config = config;
	}
}
