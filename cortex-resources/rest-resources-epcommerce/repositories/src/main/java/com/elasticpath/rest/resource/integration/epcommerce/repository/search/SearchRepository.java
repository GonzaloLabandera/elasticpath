/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.solr.FacetValue;

/**
 * Repository class for general search.
 */
public interface SearchRepository {

	/**
	 * Gets the default page size.
	 *
	 * @param storeCode the store code
	 * @return the default page size
	 */
	Single<Integer> getDefaultPageSize(String storeCode);

	/**
	 * Returns a paginated search result containing the default item ids for products
	 * returned by a search with {@code productSearchCriteria}.
	 *
	 * @param startPageNumber        the starting page to return results from
	 * @param numberOfResultsPerPage the page size to use
	 * @param productSearchCriteria  the search criteria for finding the desired products
	 * @return the paginated item search results
	 */
	Single<PaginatedResult> searchForItemIds(int startPageNumber, int numberOfResultsPerPage,
											 ProductCategorySearchCriteria productSearchCriteria);

	/**
	 * Returns a paginated search result containing the default product ids
	 * returned by a search with {@code productSearchCriteria}.
	 *
	 * @param startPageNumber        the starting page to return results from
	 * @param numberOfResultsPerPage the page size to use
	 * @param productSearchCriteria  the search criteria for finding the desired products
	 * @return the paginated item search results
	 */

	Single<PaginatedResult> searchForProductIds(ProductCategorySearchCriteria productSearchCriteria, int startPageNumber, int numberOfResultsPerPage);

	/**
	 * Returns a list objects containing the facet field and facet type.
	 *
	 * @param productSearchCriteria search criteria
	 * @param startPageNumber starting page number of the request
	 * @param numberOfResultsPerPage the number of results per page.
	 * @return list of facet fields
	 */
	Observable<String> getFacetFields(ProductCategorySearchCriteria productSearchCriteria, int startPageNumber, int numberOfResultsPerPage);

	/**
	 * Returns a list of facet values.
	 *
	 * @param facetGuid facet guid
	 * @param productSearchCriteria search criteria
	 * @return list of facet values
	 */
	Observable<FacetValue> getFacetValues(String facetGuid, ProductCategorySearchCriteria productSearchCriteria);

	/**
	 * Validate search keywords entity.
	 *
	 * @param searchKeywordsEntity SearchKeywordsEntity
	 * @return validation result
	 */
	Completable validate(SearchKeywordsEntity searchKeywordsEntity);

	/**
	 * Get Facet display name by facet guid.
	 *
	 * @param facetGuid facet guid
	 * @return facet display name
	 */
	Single<String> getDisplayNameByGuid(String facetGuid);

	/**
	 * Get the search criteria given the search details.
	 * @param categoryCode the category code if its a navigated search, else null
	 * @param storeCode the store code
	 * @param locale locale
	 * @param currency currency
	 * @param appliedFacets applied facets map
	 * @param keyword the keyword if its an offer search, else null
	 * @return a search criteria
	 */
	Single<ProductCategorySearchCriteria> getSearchCriteria(String categoryCode, String storeCode, Locale locale, Currency currency,
															Map<String, String> appliedFacets, String keyword);
}
