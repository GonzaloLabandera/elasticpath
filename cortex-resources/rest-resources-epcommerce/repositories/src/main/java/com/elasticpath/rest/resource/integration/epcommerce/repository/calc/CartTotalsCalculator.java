/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.calc;

import io.reactivex.Single;

import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.service.tax.TaxCalculationResult;

/**
 * Performs calculations on the shopping cart.
 */
public interface CartTotalsCalculator {

	/**
	 * Calculates the total for the given shopping cart.
	 * @param storeCode the store code.
	 * @param shoppingCartGuid the shopping cart to total
	 * @return the total or an error
	 */
	Single<Money> calculateTotalForShoppingCart(String storeCode, String shoppingCartGuid);

	/**
	 * Calculates the total for the given cart order.
	 * @param storeCode the store code.
	 * @param cartOrderGuid the cart order to total
	 * @return the total or an error
	 */
	Single<Money> calculateTotalForCartOrder(String storeCode, String cartOrderGuid);

	/**
	 * Calculates the total without tax for the given cart order.
	 * @param storeCode the store code.
	 * @param cartOrderGuid the cart order to total
	 * @return the total
	 */
	Single<Money> calculateSubTotalForCartOrder(String storeCode, String cartOrderGuid);

	/**
	 * Calculates the total for a line item.
	 * @param storeCode the store code.
	 * @param shoppingCartGuid the shopping cart guid
	 * @param cartItemGuid the cart item guid
	 * @return the total or an error
	 */
	Single<Money> calculateTotalForShoppingItem(String storeCode, String shoppingCartGuid, String cartItemGuid);

	/**
	 * Retrieves shopping item pricing snapshot for a line item, or an error if the price isn't set for the line item.
	 *
	 * @param storeCode the store code.
	 * @param shoppingCartGuid the shopping cart guid
	 * @param cartItemGuid the cart item guid
	 * @return the total or an error
	 */
	Single<ShoppingItemPricingSnapshot> getShoppingItemPricingSnapshot(String storeCode, String shoppingCartGuid, String cartItemGuid);

	/**
	 * Determine if the passed cart item guid has a price.
	 *
	 * @param storeCode the store code
	 * @param shoppingCartGuid the shopping cart guid
	 * @param cartItemGuid the cart item guid
	 * @return true if the cart item has a price
	 */
	boolean shoppingItemHasPrice(String storeCode, String shoppingCartGuid, String cartItemGuid);

	/**
	 * Calculates the tax for the given cart order.
	 * @param storeCode The store code.
	 * @param cartOrderGuid the cart order to tax
	 * @return the tax calculation result
	 */
	Single<TaxCalculationResult> calculateTax(String storeCode, String cartOrderGuid);
}
