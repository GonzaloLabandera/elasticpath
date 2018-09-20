/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.domain.shipping.impl;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Currency;

import com.elasticpath.domain.shipping.ShippingCostCalculationMethod;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Test assertions for shipping cost calculations. 
 */
public final class ShippingAsserts {

	private ShippingAsserts() {
		// Prevent external instantiations
	}
	
	/**
	 * Assert that the shipping cost is as expected.
	 * @param expectedCost the expected shipping cost.
	 * @param method the method to ask to calculate the shipping cost.
	 * @param shoppingCart the cart to calculate the shipping cost for.
	 * @param shippableItemsSubtotal the subtotal of the shippable shopping items
	 * @param currency the currency
	 * @param productSkuLookup a sku lookup
	 */
	public static void assertShippingCost(
			final String expectedCost, final ShippingCostCalculationMethod method,
			final ShoppingCart shoppingCart, final Money shippableItemsSubtotal, final Currency currency, final ProductSkuLookup productSkuLookup) {
		assertEquals(
				new BigDecimal(expectedCost), 
				method.calculateShippingCost(shoppingCart.getAllItems(), shippableItemsSubtotal, currency, productSkuLookup).getAmount());
	}
	
}
