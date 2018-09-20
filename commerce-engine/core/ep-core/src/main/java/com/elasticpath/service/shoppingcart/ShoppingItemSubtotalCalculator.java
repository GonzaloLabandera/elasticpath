/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart;

import java.util.Collection;
import java.util.Currency;

import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.money.Money;

/**
 * Calculates the subtotal of items.
 * Excludes tax for tax-exclusive regions. Includes tax in tax-inclusive regions.
 */
public interface ShoppingItemSubtotalCalculator {

	/**
	 * Calculates the subtotal of the shopping items, including item discounts, if present.
	 *
	 * @param shoppingItems the shopping items to evaluate
	 * @param pricingSnapshot the pricing snapshot of the shopping cart containing the shopping items
	 * @param currency the currency
	 * @return the subtotal
	 */
	Money calculate(
		Collection<? extends ShoppingItem> shoppingItems, ShoppingCartPricingSnapshot pricingSnapshot, Currency currency);

	/**
	 * Calculates the subtotal of the order SKUs, including item discounts, if present.
	 *
	 * @param orderSkus the order skus to evaluate
	 * @param currency the currency
	 * @return the subtotal
	 */
	Money calculate(Collection<OrderSku> orderSkus, Currency currency);

}
