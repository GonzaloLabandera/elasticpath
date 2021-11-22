/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.shopper.impl;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.service.customer.CustomerSessionShopperUpdateHandler;
import com.elasticpath.service.shoppingcart.ShoppingCartMerger;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Handles what happens to the {@link ShoppingCart} when a {@link Shopper} is switched out.
 */
public final class ShoppingCartMergerForShopperUpdates implements CustomerSessionShopperUpdateHandler {

	private final ShoppingCartService shoppingCartService;
	private final ShoppingCartMerger shoppingCartMerger;

	/**
	 * @param shoppingCartService shopping cart service
	 * @param shoppingCartMerger the shopping cart merger routines
	 */
	public ShoppingCartMergerForShopperUpdates(final ShoppingCartService shoppingCartService,
			final ShoppingCartMerger shoppingCartMerger) {
		this.shoppingCartService = shoppingCartService;
		this.shoppingCartMerger = shoppingCartMerger;
	}

	@Override
	public void invalidateShopper(final Shopper invalidShopper, final Shopper newShopper) {
		if (newShopper.equals(invalidShopper)) {
			copyTransientData(newShopper, invalidShopper);
			return;
		}

		final ShoppingCart invalidShoppingCart = shoppingCartService.findOrCreateDefaultCartByShopper(invalidShopper);
		final ShoppingCart newShoppingCart = shoppingCartService.findOrCreateDefaultCartByShopper(newShopper);
		if (!((ShoppingCartMementoHolder) newShoppingCart).getShoppingCartMemento().isPersisted()) {
			shoppingCartService.saveOrUpdate(newShoppingCart);
		}

		final ShoppingCart mergedShoppingCart = shoppingCartMerger.merge(newShoppingCart, invalidShoppingCart);

		attachCartToShopperAndPersist(newShopper, mergedShoppingCart);
	}

	private void copyTransientData(final Shopper currentShopper, final Shopper invalidShopper) {
		final ShoppingCart shoppingCart = invalidShopper.getCurrentShoppingCart();
		currentShopper.setCurrentShoppingCart(shoppingCart);
		if (shoppingCart != null) {
			shoppingCart.setShopper(currentShopper);
		}
	}

	private void attachCartToShopperAndPersist(final Shopper shopper, final ShoppingCart shoppingCart) {
		shoppingCart.setShopper(shopper);
		shoppingCartService.saveOrUpdate(shoppingCart);

		shopper.setCurrentShoppingCart(shoppingCart);
	}
}

