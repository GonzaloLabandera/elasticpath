/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.wishlist;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.rest.definition.wishlists.WishlistLineItemIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;

/**
 * The facade for operations with the wishlist.
 */
public interface WishlistRepository {
	/**
	 * Adds an item to the wishlist.
	 *
	 * @param wishlistId the wishlist id
	 * @param storeCode  the store code
	 * @param sku        the sku to add
	 * @return the result of the add operation
	 */
	Single<AddToWishlistResult> addItemToWishlist(String wishlistId, String storeCode, String sku);

	/**
	 * Removes an existing item from the wishlist.
	 *
	 * @param wishlistId   the wishlist id
	 * @param lineItemGuid the lineItemGuid that should be removed
	 * @return remove completed
	 */
	Completable removeItemFromWishlist(String wishlistId, String lineItemGuid);

	/**
	 * Gets the list of wishlist guids for the customer.
	 *
	 * @param customerGuid The customer.
	 * @param storeCode    The store.
	 * @return a list of wishlist guids for the customer
	 */
	Observable<String> getWishlistIds(String customerGuid, String storeCode);

	/**
	 * Gets the wishlist with the given guid.
	 *
	 * @param guid the guid
	 * @return the wishlist
	 */
	Single<WishList> getWishlist(String guid);

	/**
	 * Removes all items from the wishlist.
	 *
	 * @param wishlistGuid the guid of the wishlist
	 * @return remove completed
	 */
	Completable removeAllItemsFromWishlist(String wishlistGuid);

	/**
	 * Gets the list of wishlist guids for the customer that contain the given item.
	 *
	 * @param itemIdMap the item id map
	 * @param storeCode the store
	 * @return a list of wishlists for the customer that contain the item
	 */
	Maybe<WishList> findWishlistsContainingItem(String storeCode, Map<String, String> itemIdMap);

	/**
	 * Get the default wishlist id.
	 *
	 * @param scope scope
	 * @return the default wishlist id
	 */
	Single<String> getDefaultWishlistId(String scope);

	/**
	 * Get the product sku for the lineitem in the wishlist.
	 *
	 * @param wishlistId   the wishlist id
	 * @param lineItemGuid the lineitem guid
	 * @return the product sku
	 */
	Single<ProductSku> getProductSku(String wishlistId, String lineItemGuid);

	/**
	 * Get the product sku for the lineitem in the wishlist.
	 *
	 * @param wishlist     the wishlist object
	 * @param lineItemGuid the lineitem guid
	 * @return the product sku
	 */
	Single<ProductSku> getProductSku(WishList wishlist, String lineItemGuid);

	/**
	 * Get the shopping item for the lineitem in the wishlist.
	 *
	 * @param wishlist     the wishlist object
	 * @param lineItemGuid the lineitem guid
	 * @return the shopping item
	 */
	Single<ShoppingItem> getShoppingItem(WishList wishlist, String lineItemGuid);

	/**
	 * Construct the submit result.
	 *
	 * @param scope               scope
	 * @param wishlistId          wishlist id
	 * @param addToWishlistResult the result of add item to wishlist
	 * @return the submit result.
	 */
	Single<SubmitResult<WishlistLineItemIdentifier>> buildSubmitResult(String scope, String wishlistId,
																	   AddToWishlistResult addToWishlistResult);
}
