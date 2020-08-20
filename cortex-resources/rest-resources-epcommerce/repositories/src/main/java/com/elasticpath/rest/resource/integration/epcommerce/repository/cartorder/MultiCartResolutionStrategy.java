/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.identity.Subject;

/**
 * Strategies for resolving multi-carts.
 */
public interface MultiCartResolutionStrategy {


	/**
	 * Is the strategy applicable for the subject.
	 * @param subject the subject.
	 * @return whether to use the subject.
	 */
	boolean isApplicable(Subject subject);

	/**
	 * Find all cart guids for the customer.
	 * @param customerGuid the customer guid.
	 * @param accountSharedId the account shared ID.
	 * @param storeCode the store code.
	 * @param subject the subject.
	 * @return the list of carts.
	 */
	Observable<String> findAllCarts(String customerGuid, String accountSharedId, String storeCode, Subject subject);


	/**
	 * Get shopping cart.
	 * @param cartGuid the cart guid.
	 * @return the shopping cart.
	 */
	Single<ShoppingCart> getShoppingCartSingle(String cartGuid);

	/**
	 * Does the strategy support creating new carts.
	 * @param subject the subject.
	 * @param shopper the shopper.
	 * @param storeCode the store code.
	 * @return true if the strategy supports creating new carts.
	 */
	boolean supportsCreate(Subject subject, Shopper shopper, String storeCode);

	/**
	 * Does the strategy support creating a cart with the given identifiers.
	 * @param shoppingCart the shopper.
	 *
	 */
	void validateCreate(ShoppingCart shoppingCart);

	/**
	 * Get the modifier fields.
	 * @param storeCode the store code.
	 * @return the modifier fields.
	 */
	List<ModifierField> getModifierFields(String storeCode);

	/**
	 * Gets the default shopping cart guid.
	 * @return the default guid.
	 */
	Single<String> getDefaultShoppingCartGuid();

	/**
	 * Gets the default shopping cart.
	 * @return the default shopping cart.
	 */
	Single<ShoppingCart> getDefaultShoppingCart();

	/**
	 * Gets the default shopping cart for the customer session.
	 * @param customerSession the customer session.
	 * @return the default cart for the customer session.
	 */
	Single<ShoppingCart> getDefaultCart(CustomerSession customerSession);

	/**
	 * Creates a new non-default cart.
	 * @param descriptors the descriptors.
	 * @param scope the scope.
	 * @return a new cart, wrapped in a single.
	 */
	Single<ShoppingCart> createCart(Map<String, String> descriptors, String scope);

	/**
	 * Checks whether the given store supports multicart.
	 * @param storeCode the store code.
	 * @return true if  multicart enabled for store, false otherwise.
	 */
	boolean hasMulticartEnabled(String storeCode);
}
