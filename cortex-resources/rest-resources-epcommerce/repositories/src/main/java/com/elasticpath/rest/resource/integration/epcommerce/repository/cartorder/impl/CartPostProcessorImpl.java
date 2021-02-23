/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import javax.inject.Inject;
import javax.inject.Named;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartPostProcessor;
import com.elasticpath.service.cartorder.CartOrderService;

/**
 * Performs post processing on carts. Implements {@link CartPostProcessor}.
 */
@Named("cartPostProcessor")
public class CartPostProcessorImpl implements CartPostProcessor {

	private final CartOrderService cartOrderService;

	/**
	 * Constructor.
	 *
	 * @param cartOrderService the cartOrderService
	 */
	@Inject
	public CartPostProcessorImpl(
			@Named("cartOrderService")
			final CartOrderService cartOrderService) {

		this.cartOrderService = cartOrderService;
	}

	@Override
	public void postProcessCart(final ShoppingCart cart,
			final Shopper shopper,
			final CustomerSession customerSession) {

		if (cart != null) {
			//Need to setup session and cart for refresher to work properly.
			shopper.setCurrentShoppingCart(cart);
			shopper.updateTransientDataWith(customerSession);

			cartOrderService.createOrderIfPossible(cart);
		}
	}

}
