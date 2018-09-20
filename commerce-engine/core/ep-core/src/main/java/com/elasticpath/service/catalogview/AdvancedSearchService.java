/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.catalogview;

import com.elasticpath.domain.catalogview.search.AdvancedSearchRequest;
import com.elasticpath.domain.catalogview.search.SearchResult;
import com.elasticpath.domain.shoppingcart.ShoppingCart;

/**
 * This interface provides means of performing an advanced search on products. An advanced search helps to search products using filters such
 * as prices, attributes, brands, etc.
 *
 */
public interface AdvancedSearchService {

	/**
	 * Searches and finds the products matching the advanced search request and returns a search result based on the page number being requested.
	 *
	 * @param advancedSearchRequest the advanced search request
	 * @param shoppingCart the shopping cart
	 * @param pageNumber the page number for the search results
	 *
	 * @return the search result
	 */
	SearchResult search(AdvancedSearchRequest advancedSearchRequest, ShoppingCart shoppingCart, int pageNumber);

}
