/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shopper.impl;

import java.util.Collections;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.customer.CustomerSessionShopperUpdateHandler;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Handles what happens to the shopping cart when a {@link Shopper} is switched out.
 */
public final class InactiveShoppingCartsRemoverForShopperUpdates implements CustomerSessionShopperUpdateHandler {

	private final ShoppingCartService shoppingCartService;

	/**
	 * Alternate constructor.
	 * @param shoppingCartService shopping cart service
	 */
	public InactiveShoppingCartsRemoverForShopperUpdates(final ShoppingCartService shoppingCartService) {
		this.shoppingCartService = shoppingCartService;
	}

	@Override
	public void invalidateShopper(final CustomerSession customerSession, final Shopper invalidShopper) {
		shoppingCartService.deleteAllInactiveShoppingCartsByShopperUids(Collections.singletonList(invalidShopper.getUidPk()));
	}
}

