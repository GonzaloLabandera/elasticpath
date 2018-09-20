/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.shoppingcart;

import com.google.common.base.Function;

import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;

/**
 * <p>Guava function to convert from a {@link ShoppingItem} to a {@link ShoppingItemPricingSnapshot}, with use of a corresponding {@link
 * ShoppingCartPricingSnapshot}.</p>
 * <p>This is useful when converting a collection of {@link ShoppingItem}s and a {@link ShoppingCartPricingSnapshot} to a
 * <code>Map&lt;ShoppingItem, ShoppingItemPricingSnapshot&gt;</code>.</p>
 * <p>Sample usage:
 * <pre>
 *     ShoppingCartPricingSnapshot cartPricingSnapshot = ...;
 *     Collection<? extends ShoppingItem> shoppingItems = ...;
 *
 *     Map<? extends ShoppingItem, ShoppingItemPricingSnapshot> shoppingItemPricingSnapshotMap =
 *     		Maps.toMap(shoppingItems, new ShoppingItemToPricingSnapshotFunction(cartPricingSnapshot));
 * </pre>
 * </p>
 */
public class ShoppingItemToPricingSnapshotFunction implements Function<ShoppingItem, ShoppingItemPricingSnapshot> {

	private final ShoppingCartPricingSnapshot cartPricingSnapshot;

	/**
	 * Constructor.
	 *
	 * @param cartPricingSnapshot the cart pricing snapshot corresponding to each shopping item
	 */
	public ShoppingItemToPricingSnapshotFunction(final ShoppingCartPricingSnapshot cartPricingSnapshot) {
		this.cartPricingSnapshot = cartPricingSnapshot;
	}

	@Override
	public ShoppingItemPricingSnapshot apply(final ShoppingItem input) {
		return cartPricingSnapshot.getShoppingItemPricingSnapshot(input);
	}

}
