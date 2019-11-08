/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.shoppingcart;

import java.util.Map;

import com.elasticpath.domain.DatabaseLastModifiedDate;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartStatus;

/**
 * <code>ShoppingCartMemento</code> represents the persistable portion of
 * a <code>Customer</code>'s shopping cart.
 */
public interface ShoppingCartMemento extends ShoppingList, DatabaseLastModifiedDate {

	/**
	 * Gets the uid of the {@link com.elasticpath.domain.shopper.Shopper}.
	 * @return the {@link com.elasticpath.domain.shopper.Shopper} uid
	 */
	long getShopperUid();

	/**
	 * Gets the unique store code for this shopping cart's {@link com.elasticpath.domain.store.Store}.
	 * @return the store's code
	 */
	String getStoreCode();

	/**
	 * Sets the unique store code for this shopping cart's {@link com.elasticpath.domain.store.Store}.
	 *
	 * @param code the store code
	 */
	void setStoreCode(String code);

	/**
	 * Get cart's status.
	 *
	 * @return {@link ShoppingCartStatus}.
	 */
	ShoppingCartStatus getStatus();

	/**
	 * Set cart's status.
	 *
	 * @param status {@link ShoppingCartStatus}.
	 */
	void setStatus(ShoppingCartStatus status);


	/**
	 * Assigns {@code value} to {@code name}. Any previous value is replaced.
	 *
	 * @param name The name of the field to assign.
	 * @param value The value to assign to the field.
	 */
	void setCartDataFieldValue(String name, String value);

	/**
	 * Accesses the field for {@code name} and returns the current value. If the field has not been set
	 * then will return null.
	 *
	 * @param name The name of the field.
	 * @return The current value of the field or null.
	 */
	String getCartDataFieldValue(String name);


	/**
	 * Gets the cart data for the cart.
	 * @return the cart data.
	 */
	Map<String, CartData> getCartData();

	/**
	 * Sets the cart data for the cart.
	 * @param cartData the cart data
	 */
	void setCartData(Map<String, CartData> cartData);

	/**
	 * Whether this cart is the default cart for the shopper.
	 * @return true if this is the default cart. false otherwise.
	 */
	boolean isDefault();

	/**
	 * Sets the defaultCart flag.
	 * @param isDefaultCart the flag for whether this is the default cart.
	 */
	void setDefault(boolean isDefaultCart);

}
