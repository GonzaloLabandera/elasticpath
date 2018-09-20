/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.service.shoppingcart.OrderSkuToPricingSnapshotFunction;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingItemSubtotalCalculator;
import com.elasticpath.service.shoppingcart.ShoppingItemToPricingSnapshotFunction;

/**
 * Calculates shopping item subtotal.
 */
public class ShoppingItemSubtotalCalculatorImpl implements ShoppingItemSubtotalCalculator {

	private PricingSnapshotService pricingSnapshotService;

	@Override
	public Money calculate(final Collection<? extends ShoppingItem> shoppingItems, final ShoppingCartPricingSnapshot pricingSnapshot,
							final Currency currency) {

		final Set<? extends ShoppingItem> shoppingItemsSet;

		if (shoppingItems instanceof Set) {
			shoppingItemsSet = (Set<? extends ShoppingItem>) shoppingItems;
		} else {
			shoppingItemsSet = new LinkedHashSet<>(shoppingItems);
		}

		return sumShoppingItems(
			Maps.asMap(shoppingItemsSet, new ShoppingItemToPricingSnapshotFunction(pricingSnapshot)), currency);

	}

	@Override
	public Money calculate(final Collection<OrderSku> orderSkus, final Currency currency) {
		final Set<OrderSku> orderSkusSet;

		if (orderSkus instanceof Set) {
			orderSkusSet = (Set<OrderSku>) orderSkus;
		} else {
			orderSkusSet = new LinkedHashSet<>(orderSkus);
		}

		return sumShoppingItems(Maps.asMap(orderSkusSet, new OrderSkuToPricingSnapshotFunction(getPricingSnapshotService())), currency);
	}

	private Money sumShoppingItems(final Map<? extends ShoppingItem, ShoppingItemPricingSnapshot> shoppingItemPricingSnapshotMap,
									final Currency currency) {
		Money subtotal = Money.valueOf(BigDecimal.ZERO, currency);

		for (final Map.Entry<? extends ShoppingItem, ShoppingItemPricingSnapshot> snapshotEntry : shoppingItemPricingSnapshotMap.entrySet()) {
			final ShoppingItemPricingSnapshot itemPricingSnapshot = snapshotEntry.getValue();
			subtotal = subtotal.add(itemPricingSnapshot.getPriceCalc().withCartDiscounts().getMoney());
		}

		return subtotal;
	}

	public void setPricingSnapshotService(final PricingSnapshotService pricingSnapshotService) {
		this.pricingSnapshotService = pricingSnapshotService;
	}

	protected PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}
}
