/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.shoppingcart.impl;

import java.util.Collection;
import java.util.Currency;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.ShippableItemsSubtotalCalculator;
import com.elasticpath.service.shoppingcart.ShoppingItemSubtotalCalculator;

/**
 * Default implementation of {@link ShippableItemsSubtotalCalculator}.
 */
public class ShippableItemsSubtotalCalculatorImpl implements ShippableItemsSubtotalCalculator {

	private ProductSkuLookup productSkuLookup;
	private ShoppingItemSubtotalCalculator shoppingItemSubtotalCalculator;

	@Override
	public Money calculateSubtotalOfShippableItems(final Collection<? extends ShoppingItem> shoppingItems,
													final ShoppingCartPricingSnapshot cartPricingSnapshot, final Currency currency) {
		final ShippableItemPredicate<ShoppingItem> predicate = new ShippableItemPredicate<>(getProductSkuLookup());
		final Collection<? extends ShoppingItem> shippableItems = Collections2.filter(shoppingItems, predicate);
		return getShoppingItemSubtotalCalculator().calculate(shippableItems, cartPricingSnapshot, currency);
	}

	@Override
	public Money calculateSubtotalOfShippableItems(final Collection<OrderSku> orderSkus, final Currency currency) {
		final ShippableItemPredicate<ShoppingItem> predicate = new ShippableItemPredicate<>(getProductSkuLookup());
		final Collection<OrderSku> shippableItems = Collections2.filter(orderSkus, predicate);
		return getShoppingItemSubtotalCalculator().calculate(shippableItems, currency);
	}

	/**
	 * Predicate used for identifying shippable items.
	 * @param <T> ShoppingItem or subclass thereof
	 */
	private static final class ShippableItemPredicate<T extends ShoppingItem> implements Predicate<T> {

		private final ProductSkuLookup productSkuLookup;

		/**
		 * Constructor.
		 * @param productSkuLookup the {@link ProductSkuLookup} used for determining isShippable.
		 */
		ShippableItemPredicate(final ProductSkuLookup productSkuLookup) {
			this.productSkuLookup = productSkuLookup;
		}

		@Override
		public boolean apply(final T shoppingItem) {
			return shoppingItem.isShippable(productSkuLookup);
		}
	}

	public void setProductSkuLookup(final ProductSkuLookup productSkuLookup) {
		this.productSkuLookup = productSkuLookup;
	}

	protected ProductSkuLookup getProductSkuLookup() {
		return productSkuLookup;
	}

	public void setShoppingItemSubtotalCalculator(final ShoppingItemSubtotalCalculator shoppingItemSubtotalCalculator) {
		this.shoppingItemSubtotalCalculator = shoppingItemSubtotalCalculator;
	}

	public ShoppingItemSubtotalCalculator getShoppingItemSubtotalCalculator() {
		return shoppingItemSubtotalCalculator;
	}
}
