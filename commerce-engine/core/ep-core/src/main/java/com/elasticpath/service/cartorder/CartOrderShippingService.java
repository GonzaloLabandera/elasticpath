/*
 * Copyright (c) Elastic Path Software Inc., 2011.
 */
package com.elasticpath.service.cartorder;

import java.util.List;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;

/**
 * This Class can perform services for CartOrders related to shipping, 
 * CartOrder should not be used in versions of EP prior to 6.4.
 */
public interface CartOrderShippingService {

	/**
	 * Update the shipping address on the cart order. This may also update the shipping service level on the cart order.
	 *
	 * @param shippingAddressGuid shipping address guid to update on cart
	 * @param cartOrder cart order to update
	 * @param storeCode store code used to look up shipping service levels.
	 * @return true if cart order shipping address is updated, false otherwise.
	 */
	Boolean updateCartOrderShippingAddress(String shippingAddressGuid, CartOrder cartOrder, String storeCode);
	
	/**
	 * Populates the transient fields on the shopping cart using the information in the cart order.
	 *
	 * @param shoppingCart the shopping cart
	 * @param cartOrder the cart order
	 * @return the same shopping cart, now populated with the transient fields.
	 * @deprecated use populateAddressAndShippingFields instead as this method will call ShoppingCart.calculateShoppingCartTaxAndBeforeTaxPrices()
	 */
	@Deprecated
	ShoppingCart populateShoppingCartTransientFields(ShoppingCart shoppingCart, CartOrder cartOrder);

	/**
	 * Populates the transient address and shipping fields on the shopping cart using the information in the cart order.
	 * <p/>
	 * Does not call ShoppingCart.calculateShoppingCartTaxAndBeforeTaxPrices()
	 *
	 * @param shoppingCart the shopping cart
	 * @param cartOrder the cart order
	 * @return the same shopping cart, now populated with the transient fields.
	 */
	ShoppingCart populateAddressAndShippingFields(ShoppingCart shoppingCart, CartOrder cartOrder);

	/**
	 * Find shipping service levels for given store code and shipping address.
	 *
	 * @param storeCode the store code
	 * @param shippingAddress the shipping address
	 * @return list of found shipping service levels or empty list
	 */
	List<ShippingServiceLevel> findShippingServiceLevels(String storeCode, Address shippingAddress);

}
