/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shopper.impl;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.customer.CustomerSessionShopperUpdateHandler;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Handles what happens to the shopping cart when a {@link Shopper} is switched out.
 */
public final class ShoppingCartsRemoverForShopperUpdates implements CustomerSessionShopperUpdateHandler {

	private final ShoppingCartService shoppingCartService;

	/**
	 * Alternate constructor.
	 * @param shoppingCartService shopping cart service
	 */
	public ShoppingCartsRemoverForShopperUpdates(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	@Override
	public void invalidateShopper(final Shopper oldShopper, final Shopper newShopper) {
		if (newShopper.equals(oldShopper)) {
			return;
		}
		shoppingCartService.deleteAllShoppingCartsByShopperUid(oldShopper.getUidPk());
	}
}

