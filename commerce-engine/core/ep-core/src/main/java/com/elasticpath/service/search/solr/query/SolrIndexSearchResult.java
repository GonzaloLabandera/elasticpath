/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.solr.query;

import static com.elasticpath.service.search.solr.FacetConstants.ATTRIBUTE;
import static com.elasticpath.service.search.solr.FacetConstants.BRAND;
import static com.elasticpath.service.search.solr.FacetConstants.CATEGORY;
import static com.elasticpath.service.search.solr.FacetConstants.PRICE;
import static com.elasticpath.service.search.solr.FacetConstants.RANGED_ATTRIBUTE;
import static com.elasticpath.service.search.solr.FacetConstants.SIZE;
import static com.elasticpath.service.search.solr.FacetConstants.SKU_OPTION;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;

import com.elasticpath.domain.catalogview.AttributeRangeFilter;
import com.elasticpath.domain.catalogview.AttributeValueFilter;
import com.elasticpath.domain.catalogview.BrandFilter;
import com.elasticpath.domain.catalogview.CategoryFilter;
import com.elasticpath.domain.catalogview.FilterOption;
import com.elasticpath.domain.catalogview.PriceFilter;
import com.elasticpath.domain.catalogview.SizeRangeFilter;
import com.elasticpath.domain.catalogview.SkuOptionValueFilter;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.FacetGroup;
import com.elasticpath.service.search.index.IndexSearchResult;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.search.solr.FacetConstants;
import com.elasticpath.service.search.solr.FacetValue;
import com.elasticpath.service.search.solr.SolrIndexSearcherImpl;

/**
 * A SOLR implementation of <code>IndexSearchResult</code>.
 */
@SuppressWarnings("PMD.GodClass")
public class SolrIndexSearchResult implements IndexSearchResult, Serializable {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Sets whether to try and find a non-empty page when searching. This can happen when you
	 * search for one of the later pages and find no results (i.e. the results were deleted just
	 * prior to your search).
	 */
	private static final boolean PREFER_NON_EMPTY_PAGE = true;

	private static final Set<Integer> RANGE_ATTRIBUTE_FIELD_KEY_TYPES = Sets.newHashSet(1, 3);

	private SearchCriteria searchCriteria;

	private transient SolrIndexSearcherImpl indexSearcher;

	private int numFound = -1;

	private int startIndex = -1;

	private List<Long> results;

	private List<Long> pageResults;

	private List<FilterOption<CategoryFilter>> categoryFilterOptions;

	private List<FilterOption<BrandFilter>> brandFilterOptions;

	private List<FilterOption<PriceFilter>> priceFilterOptions;

	private Map<String, List<FilterOption<SkuOptionValueFilter>>> skuOptionValueFilterOptions;

	private Map<String, List<FilterOption<AttributeValueFilter>>> attributeValueFilterOptions;

	private Map<String, List<FilterOption<AttributeRangeFilter>>> attributeRangeFilterOptions;

	private boolean rememberOptions;

	private Map<String, List<FilterOption<SizeRangeFilter>>> sizeRangeFilterOptions;

	private Map<String, Facet> facetMap;

	private static FacetValue buildFacetValue(final Locale locale, final FilterOption<?> filter) {
		return new FacetValue(filter.getDisplayName(locale), filter.getFilter().getId(), String.valueOf(filter.getHitsNumber()));
	}

	/**
	 * Gets the list of results with an initial index to search from. Attempts to always return a
	 * non-empty list (when the <code>startIndex</code> is out of bounds). If there truly is
	 * nothing, will return an empty list.
	 *
	 * @param startIndex the index to start searching from
	 * @param maxResults the maximum results to return from given start index (maximum per page)
	 * @return a list of results
	 */
	@Override
	public List<Long> getResults(final int startIndex, final int maxResults) {
		this.startIndex = startIndex;
		findNonEmptyPage(maxResults);
		return results;
	}

	/**
	 * Set the result uid list.  Note: this is not
	 * part of the IndexSearchResult interface and not intended for external
	 * use.
	 *
	 * @param results the list of uids that that should be used
	 */
	public void setResultUids(final List<Long> results) {
		this.results = results;
	}

	/**
	 * Return the current result uid list.  Note: this is not part of the
	 * IndexSearchResult interface and not intended for external use.
	 *
	 * @return the current result uid list.
	 */
	public List<Long> getResultUids() {
		return results;
	}

	/**
	 * Retrieves all of the results without pagination.
	 *
	 * @return a list of all results without pagination
	 */
	@Override
	public List<Long> getAllResults() {
		// Use a two stage search here to get the number of results found and then use that as
		// the limit. This is due to Solr requiring a limit. Just using a large number may put
		// restrictions on the requirements of the JVM (and takes longer to compile search
		// from a query).
		final int numFound = getNumFound();
		return getResults(0, numFound);
	}

	/**
	 * Gets the last page of results. The last page will contain the maximum number of allowable
	 * results unless there are less than that many.
	 *
	 * @param maxResults the maximum results to return (maximum per page)
	 * @return a list of results
	 */
	@Override
	public List<Long> getLastPage(final int maxResults) {
		startIndex = getLastNumFound() - maxResults;
		findNonEmptyPage(maxResults);
		return results;
	}

	private void findNonEmptyPage(final int maxResults) {
		clearResults();
		clearAllFilterOptions();

		if (startIndex < 0) {
			results = Collections.emptyList();
			return;
		}
		indexSearcher.search(searchCriteria, startIndex, maxResults, this);
		if (results.isEmpty()) {
			startIndex -= maxResults;
		}
		while (PREFER_NON_EMPTY_PAGE && results.isEmpty() && startIndex > 0) {
			indexSearcher.search(searchCriteria, startIndex, maxResults, this);
			startIndex -= maxResults;
		}
	}

	/**
	 * Sets the <code>SearchCriteria</code> instance.
	 *
	 * @param searchCriteria the <code>SearchCriteria</code> instance
	 */
	public void setSearchCriteria(final SearchCriteria searchCriteria) {
		this.searchCriteria = searchCriteria;
		searchCriteria.optimize();
	}

	/**
	 * Sets the {@link SolrIndexSearcherImpl} instance.
	 *
	 * @param indexSearcher the {@link SolrIndexSearcherImpl} instance
	 */
	public void setIndexSearcher(final SolrIndexSearcherImpl indexSearcher) {
		this.indexSearcher = indexSearcher;
	}

	public void setNumFound(final int numFound) {
		this.numFound = numFound;
	}

	/**
	 * Gets the number of results found from the last search. If no search has taken place, a
	 * search will be requested initially to get an accurate number of results.
	 *
	 * @return the number of results
	 */
	@Override
	public int getLastNumFound() {
		// always make sure this number is valid, trigger a search if invalid
		if (numFound < 0) {
			getResults(0, 0);
		}
		return numFound;
	}

	/**
	 * Gets the number of results found. This does not necessarily reflect the number of results
	 * found in the last search. This overrides the cached number of results found.
	 *
	 * @return the number of results found
	 */
	@Override
	public int getNumFound() {
		getResults(0, 0);
		return numFound;
	}

	/**
	 * Returns the start index of the previous search result. If no search has taken place, a
	 * negative number will be returned.
	 *
	 * @return the start index of the previous search result
	 */
	@Override
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * Gets the list of category {@link FilterOption}s for the previous search. This is only
	 * valid if faceting is enabled for the search.
	 *
	 * @return the list of category {@link FilterOption}s for the previous search
	 */
	@Override
	public List<FilterOption<CategoryFilter>> getCategoryFilterOptions() {
		return ObjectUtils.defaultIfNull(categoryFilterOptions, Collections.emptyList());
	}

	/**
	 * Sets the list of category {@link FilterOption}s for the previous search. This is only
	 * valid if faceting is enabled for the search.
	 *
	 * @param categoryFilterOptions the list of category {@link FilterOption}s for the previous
	 *            search
	 */
	public void setCategoryFilterOptions(final List<FilterOption<CategoryFilter>> categoryFilterOptions) {
		this.categoryFilterOptions = categoryFilterOptions;
	}

	/**
	 * Gets the list of brand {@link FilterOption}s for the previous search. This is only valid
	 * if faceting is enabled for the search.
	 *
	 * @return the list of brand {@link FilterOption}s for the previous search
	 */
	@Override
	public List<FilterOption<BrandFilter>> getBrandFilterOptions() {
		return ObjectUtils.defaultIfNull(brandFilterOptions, Collections.emptyList());
	}

	/**
	 * Sets the list of brand {@link FilterOption}s for the previous search. This is only valid
	 * if faceting is enabled for the search.
	 *
	 * @param brandFilterOptions the list of brand {@link FilterOption}s for the previous search
	 */
	public void setBrandFilterOptions(final List<FilterOption<BrandFilter>> brandFilterOptions) {
		this.brandFilterOptions = brandFilterOptions;
	}

	/**
	 * Gets the list of price {@link FilterOption}s for the previous search. This is only valid
	 * if faceting is enabled for the search.
	 *
	 * @return the list of price {@link FilterOption}s for the previous search
	 */
	@Override
	public List<FilterOption<PriceFilter>> getPriceFilterOptions() {
		return ObjectUtils.defaultIfNull(priceFilterOptions, Collections.emptyList());
	}

	/**
	 * Sets the list of price {@link FilterOption}s for the previous search. This is only valid
	 * if faceting is enabled for the search.
	 *
	 * @param priceFilterOptions the list of price {@link FilterOption}s for the previous search
	 */
	public void setPriceFilterOptions(final List<FilterOption<PriceFilter>> priceFilterOptions) {
		this.priceFilterOptions = priceFilterOptions;
	}

	/**
	 * Gets the list of attribute value {@link FilterOption}s for the previous search. This is
	 * only valid if faceting is enabled for the search.
	 *
	 * @return the list of attribute value {@link FilterOption}s for the previous search
	 */
	@Override
	public Map<String, List<FilterOption<AttributeValueFilter>>> getAttributeValueFilterOptions() {
		return ObjectUtils.defaultIfNull(attributeValueFilterOptions, Collections.emptyMap());
	}

	/**
	 * Sets the list of attribute value {@link FilterOption}s for the previous search. This is
	 * only valid if faceting is enabled for the search.
	 *
	 * @param attributeFilterOptions the list of attribute value {@link FilterOption}s for the
	 *            previous search
	 */
	public void setAttributeValueFilterOptions(final Map<String, List<FilterOption<AttributeValueFilter>>> attributeFilterOptions) {
		this.attributeValueFilterOptions = attributeFilterOptions;
	}

	/**
	 * Gets the list of attribute range {@link FilterOption}s for the previous search. This is
	 * only valid if faceting is enabled for the search.
	 *
	 * @return the list of attribute range {@link FilterOption}s for the previous search
	 */
	@Override
	public Map<String, List<FilterOption<AttributeRangeFilter>>> getAttributeRangeFilterOptions() {
		return ObjectUtils.defaultIfNull(attributeRangeFilterOptions, Collections.emptyMap());
	}

	/**
	 * Sets the list of attribute range {@link FilterOption}s for the previous search. This is
	 * only valid if faceting is enabled for the search.
	 *
	 * @param attributeRangeFilterOptions the list of attribute range {@link FilterOption}s for
	 *                                    the previous search
	 */
	public void setAttributeRangeFilterOptions(
			final Map<String, List<FilterOption<AttributeRangeFilter>>> attributeRangeFilterOptions) {
		this.attributeRangeFilterOptions = attributeRangeFilterOptions;
	}

	@Override
	public List<String> getFacetFields(final int maxResults) {
		getResults(0, maxResults);
		List<String> facetInfos = addDefaultFacets();
		facetInfos.addAll(getSizeRangeFilterOptions().keySet());
		facetInfos.addAll(getAttributeRangeFilterOptions().keySet());
		facetInfos.addAll(getAttributeValueFilterOptions().keySet());
		facetInfos.addAll(getSkuOptionValueFilterOptions().keySet());
		return facetInfos;
	}

	private List<String> addDefaultFacets() {
		final List<String> facetInfos = new ArrayList<>();
		if (!getBrandFilterOptions().isEmpty()) {
			facetInfos.add(getFacetInfo(BRAND));
		}
		if (!getPriceFilterOptions().isEmpty()) {
			facetInfos.add(getFacetInfo(PRICE));
		}
		if (!getCategoryFilterOptions().isEmpty()) {
			facetInfos.add(getFacetInfo(CATEGORY));
		}
		return facetInfos;
	}

	private String getFacetInfo(final String facetType) {
		return getFacetMap().values().stream()
				.filter(facet -> facet.getFacetGroup() == FacetGroup.OTHERS.getOrdinal() && facetType.equals(facet.getFacetName()))
				.map(Facet::getFacetGuid).findFirst().orElse(facetType);
	}

	@Override
	public List<FacetValue> getFacetValues(final String facetGuid, final int maxResults) {
		getResults(0, maxResults);
		Locale locale = searchCriteria.getLocale();
		final Facet facet = getFacetMap().get(facetGuid);
		final Integer facetGroup = facet.getFacetGroup();
		final String type;
		if (facetGroup == FacetGroup.OTHERS.getOrdinal()) {
			String name = facet.getFacetName();
			type = FacetConstants.SIZE_ATTRIBUTES.contains(name) ? SIZE : name;
		} else if (Stream.of(FacetGroup.PRODUCT_ATTRIBUTE, FacetGroup.SKU_ATTRIBUTE).map(FacetGroup::getOrdinal).anyMatch(facetGroup::equals)) {
			type = RANGE_ATTRIBUTE_FIELD_KEY_TYPES.contains(facet.getFieldKeyType()) ? RANGED_ATTRIBUTE : ATTRIBUTE;
		} else {
			type = SKU_OPTION;
		}
		List<FacetValue> facetValues = new ArrayList<>();
		switch (type) {
			case BRAND:
				getBrandFilterOptions().forEach(brandFilter -> facetValues.add(buildFacetValue(locale, brandFilter)));
				break;
			case CATEGORY:
				getCategoryFilterOptions().forEach(catFilter -> facetValues.add(buildFacetValue(locale, catFilter)));
				break;
			case PRICE:
				getPriceFilterOptions().forEach(priceFilter -> facetValues.add(buildFacetValue(locale, priceFilter)));
				break;
			case RANGED_ATTRIBUTE:
				getAttributeRangeFilterOptions().get(facetGuid)
						.forEach(filter -> facetValues.add(buildFacetValue(locale, filter)));
				break;
			case SKU_OPTION:
				getSkuOptionValueFilterOptions().get(facetGuid)
						.forEach(filter -> facetValues.add(buildFacetValue(locale, filter)));
				break;
			case SIZE:
				getSizeRangeFilterOptions().get(facetGuid)
						.forEach(filter -> facetValues.add(buildFacetValue(locale, filter)));
				break;
			default:
				getAttributeValueFilterOptions().get(facetGuid)
						.forEach(filter -> facetValues.add(buildFacetValue(locale, filter)));
				break;
		}
		return facetValues;
	}

	/**
	 * Clears results by setting the result to an empty list.
	 */
	private void clearResults() {
		results = Collections.emptyList();

	}

	/**
	 * Clears all filter options by setting them to empty collections.
	 */
	private void clearAllFilterOptions() {
		if (!rememberOptions) {
			categoryFilterOptions = Collections.emptyList();
			brandFilterOptions = Collections.emptyList();
			priceFilterOptions = Collections.emptyList();
			attributeValueFilterOptions = Collections.emptyMap();
			attributeRangeFilterOptions = Collections.emptyMap();
		}
	}

	/**
	 * filter uids by start index and page size.
	 *
	 * @param startIndex start index
	 * @param pageSize page size
	 */
	@Override
	public void filterUids(final int startIndex, final int pageSize) {
		this.pageResults = getResults(startIndex, pageSize);
	}

	/**
	 * Gets page result.
	 *
	 * @return list of uids within the page
	 */
	@Override
	public List<Long> getPageResults() {
		return this.pageResults;
	}

	/**
	 * Sets the flag to remember existing options.
	 *
	 * @param rememberOptions the flag to remember existing filter options
	 */
	@Override
	public void setRememberOptions(final boolean rememberOptions) {
		this.rememberOptions = rememberOptions;
	}

	/**
	 * Checks if the flag to remember existing filter options is set.  If true,
	 *
	 * @return true if remembering existing filter options.  The implication is that filter options have
	 * already been set and do not need to be retrieved and set again.
	 */
	@Override
	public boolean isRememberOptions() {
		return rememberOptions;
	}

	@Override
	public boolean isEmpty() {
		return results.isEmpty();
	}

	public Map<String, Facet> getFacetMap() {
		return facetMap;
	}

	public void setFacetMap(final Map<String, Facet> facetMap) {
		this.facetMap = facetMap;
	}

	@Override
	public Map<String, List<FilterOption<SkuOptionValueFilter>>> getSkuOptionValueFilterOptions() {
		return ObjectUtils.defaultIfNull(skuOptionValueFilterOptions, Collections.emptyMap());
	}

	@Override
	public void setSkuOptionValueFilterOptions(final Map<String, List<FilterOption<SkuOptionValueFilter>>> skuOptionValueFilterOptions) {
		this.skuOptionValueFilterOptions = skuOptionValueFilterOptions;
	}

	@Override
	public Map<String, List<FilterOption<SizeRangeFilter>>> getSizeRangeFilterOptions() {
		return ObjectUtils.defaultIfNull(sizeRangeFilterOptions, Collections.emptyMap());
	}

	@Override
	public void setSizeRangeFilterOptions(final Map<String, List<FilterOption<SizeRangeFilter>>> sizeRangeFilterOptions) {
		this.sizeRangeFilterOptions = sizeRangeFilterOptions;
	}
}