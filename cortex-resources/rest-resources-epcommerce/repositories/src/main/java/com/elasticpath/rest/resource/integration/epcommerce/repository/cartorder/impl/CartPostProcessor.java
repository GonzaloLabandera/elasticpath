/*
 * Copyright © 2014 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.shoppingcart.ShoppingCartRefresher;

/**
 * Performs post processing on carts.
 */
@Named("cartPostProcessor")
public class CartPostProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(CartPostProcessor.class);

	private final CartOrderService cartOrderService;
	private final ShoppingCartRefresher shoppingCartRefresher;

	/**
	 * Constructor.
	 *
	 * @param cartOrderService      the cartOrderService
	 * @param shoppingCartRefresher the shopping cart refresher.
	 */
	@Inject
	public CartPostProcessor(
			@Named("cartOrderService")
			final CartOrderService cartOrderService,
			@Named("shoppingCartRefresher")
			final ShoppingCartRefresher shoppingCartRefresher) {

		this.cartOrderService = cartOrderService;

		this.shoppingCartRefresher = shoppingCartRefresher;
	}

	/**
	 * Sets the customer session on the cart, and does the after retrieval updates on the cart according to
	 * {@link com.elasticpath.service.shoppingcart.impl.ShoppingCartServiceImpl#findByGuid(String)}.
	 *
	 * @param cart            the cart
	 * @param shopper         the shopper
	 * @param customerSession the customer session
	 */
	void postProcessCart(final ShoppingCart cart,
						final Shopper shopper,
						final CustomerSession customerSession) {

		if (cart != null) {
			//Need to setup session and cart for refresher to work properly.
			shopper.setCurrentShoppingCart(cart);
			shopper.updateTransientDataWith(customerSession);

			shoppingCartRefresher.refresh(cart);

			//Required by cortex
			try {
				cartOrderService.createOrderIfPossible(cart);
			} catch (DataIntegrityViolationException dive) {
				LOG.warn("Cart order already created by another thread for the same shopping cart with id: {} and shopper: {}",
					cart.getGuid(), shopper.getGuid());
			}
		}
	}

}
