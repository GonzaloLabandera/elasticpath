/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search;

import java.util.Currency;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.OfferSearchData;
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

	Single<PaginatedResult> searchForProductIds(ProductCategorySearchCriteria productSearchCriteria, int startPageNumber,
												int numberOfResultsPerPage);

	/**
	 * Returns a list objects containing the facet field and facet type.
	 *
	 * @param productSearchCriteria  search criteria
	 * @param startPageNumber        starting page number of the request
	 * @param numberOfResultsPerPage the number of results per page.
	 * @return list of facet fields
	 */
	Observable<String> getFacetFields(ProductCategorySearchCriteria productSearchCriteria, int startPageNumber, int numberOfResultsPerPage);

	/**
	 * Returns a list of facet values.
	 *
	 * @param facetGuid             facet guid
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
	 *
	 * @param offerSearchData offer search data containing search
	 * @param locale          locale
	 * @param currency        currency
	 * @return a search criteria
	 */
	Single<ProductCategorySearchCriteria> getSearchCriteria(OfferSearchData offerSearchData, Locale locale, Currency currency);

	/**
	 * Get all sort attribute guids for store and locale.
	 * @param storeCode store code
	 * @param localeCode locale code
	 * @return guids
	 */
	Observable<String> getSortAttributeGuidsForStoreAndLocale(String storeCode, String localeCode);

	/**
	 * Get the sort value of the sort attribute given the guid and locale code.
	 * @param guid guid
	 * @param localCode locale code
	 * @return sort value
	 */
	Single<SortValue> getSortValueByGuidAndLocaleCode(String guid, String localCode);

	/**
	 * Get the default sort for the store.
	 * @param storeCode store code
	 * @return default sort or null if there is no default
	 */
	Maybe<SortAttribute> getDefaultSortAttributeForStore(String storeCode);
}
