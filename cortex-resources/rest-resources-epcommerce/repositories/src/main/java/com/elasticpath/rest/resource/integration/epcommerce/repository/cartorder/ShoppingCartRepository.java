/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.service.shoppingcart.impl.AddToWishlistResult;

/**
 * The facade for operations with the cart.
 */
public interface ShoppingCartRepository {

	/**
	 * Gets the default shopping cart.
	 *
	 * @return Single with the default shopping cart
	 */
	Single<ShoppingCart> getDefaultShoppingCart();

	/**
	 * Gets the default shopping cart GUID.
	 * @return Single with the cart guid.
	 */
	Single<String> getDefaultShoppingCartGuid();

	/**
	 * Gets the descriptors for a given cart.
	 * @param cartGuid the cart.
	 * @return the map of cartData descriptors. map.
	 */
	Map<String, CartData> getCartDescriptors(String cartGuid);

	/**
	 * Gets the default shopping cart for the customer.
	 *
	 * @param customerGuid  The customer.
	 * @return the ShoppingCart
	 */
	Single<ShoppingCart> getShoppingCartForCustomer(String customerGuid);

	/**
	 * Gets the shopping cart with given GUID.
	 *
	 * @param cartGuid the cart guid
	 * @return the ShoppingCart
	 */
	Single<ShoppingCart> getShoppingCart(String cartGuid);

	/**
	 * Check if the shopping cart with the specified guid is valid for the current scope.
	 *
	 * @param cartGuid  shoppingCart id.
	 * @param storeCode storeCode.
	 * @return boolean true if the cart exists.
	 */
	Single<Boolean> verifyShoppingCartExistsForStore(String cartGuid, String storeCode);

	/**
	 * Since you inevitably run fireRules() and blow away the record
	 * of the applied catalog promotions, this method will re-price
	 * all your items and re-record the catalog promotions for you.
	 *
	 * @param cart The cart.
	 */
	void reApplyCatalogPromotions(ShoppingCart cart);

	/**
	 * Adds an item to the cart.
	 *
	 * @param cart     the shopping cart
	 * @param skuCode  sku code
	 * @param quantity new item count
	 * @param fields   the shopping item fields
	 * @return the new ShoppingItem
	 */
	Single<ShoppingItem> addItemToCart(ShoppingCart cart, String skuCode, int quantity, Map<String, String> fields);

	/**
	 * Get the shopping item dto given the sku code, quantity and configurable fields.
	 *
	 * @param skuCode  skuCode
	 * @param quantity quantity
	 * @param fields   fields
	 * @return shopping item dto
	 */
	ShoppingItemDto getShoppingItemDto(String skuCode, int quantity, Map<String, String> fields);

	/**
	 * Add a list of items to the cart.
	 *
	 * @param cart    the shopping cart
	 * @param dtoList dtoList
	 * @return the updated shopping cart
	 */
	Single<ShoppingCart> addItemsToCart(ShoppingCart cart, List<ShoppingItemDto> dtoList);

	/**
	 * Moves an item to the cart from a wishlist.
	 *
	 * @param cart                 the shopping cart
	 * @param wishlistLineItemGuid the line item guid
	 * @param skuCode              sku code
	 * @param quantity             new item count
	 * @param fields               configuration
	 * @return the new ShoppingItem
	 */
	Single<ShoppingItem> moveItemToCart(ShoppingCart cart, String wishlistLineItemGuid, String skuCode, int quantity, Map<String, String> fields);

	/**
	 * Moves an item to a wishlist from a cart.
	 *
	 * @param cart             the shopping cart
	 * @param cartLineItemGuid the line item guid
	 * @return the new ShoppingItem
	 */
	Single<AddToWishlistResult> moveItemToWishlist(ShoppingCart cart, String cartLineItemGuid);

	/**
	 * Get the product sku in the shopping cart.
	 *
	 *
	 * @param cartId the shopping cart identifer.
	 * @param lineItemId the line item id
	 * @return product sku
	 */
	Single<ProductSku> getProductSku(String cartId, String lineItemId);

	/**
	 * Find the shopping item for the lineItemId.
	 *
	 * @param lineItemId lineItemId
	 * @param cart       cart
	 * @return shopping item
	 */
	Single<ShoppingItem> getShoppingItem(String lineItemId, ShoppingCart cart);

	/**
	 * Updates an existing item to the cart.
	 *
	 * @param cart            the shopping cart
	 * @param shoppingItem    the shopping item that should be updated
	 * @param skuCode         sku code
	 * @param quantity        new item count
	 * @return the updated ShoppingItem and the updated ShoppingCart
	 */
	Completable updateCartItem(ShoppingCart cart, ShoppingItem shoppingItem, String skuCode, int quantity);

	/**
	 * Removes an existing item from the cart.
	 *
	 * @param cart            the shopping cart
	 * @param shoppingItemGuid the id of the shopping item that should be removed
	 * @return the updated ShoppingCart
	 */
	Completable removeItemFromCart(ShoppingCart cart, String shoppingItemGuid);

	/**
	 * Removes all existing items from the cart.
	 *
	 * @return the whether the operation was successful.
	 */
	Completable removeAllItemsFromDefaultCart();


	/**
	 * Removes all existing items from the cart.
	 * @param  cart the cart.
	 * @return the whether the operation was successful.
	 */
	Completable removeAllItemsFromCart(ShoppingCart cart);

	/**
	 * Find All of the shopping carts for this customer.
	 *
	 * @param customerGuid the customer GUID
	 * @param accountSharedId the account shared ID
	 * @param storeCode valid store code
	 * @return Collection of cart GUID's
	 */
	Observable<String> findAllCarts(String customerGuid, String accountSharedId, String storeCode);

	/**
	 * Finds the storecode for the given cart guid.
	 * @param cartGuid the cart guid.
	 * @return the storecode.
	 */
	Single<String> findStoreForCartGuid(String cartGuid);

	/**
	 * Create a cart for the given cart identifier.
	 * @param identifier the identifiers.
	 * @param scope the scope.
	 * @return the shopping cart.
	 */
	Single<ShoppingCart> createCart(Map<String, String> identifier, String scope);

	/**
	 * Delete named shopping cart.
	 *
	 * @param shoppingCartGuid shopping cart guid
	 * @return if removal was successful
	 */
	Completable removeCart(String shoppingCartGuid);

	/**
	 * Checks whether a given storecode supports creating a cart.
	 * @param storeCode the storeCode.
	 * @return true if carts can be created for the store.
	 */
	boolean canCreateCart(String storeCode);
}
