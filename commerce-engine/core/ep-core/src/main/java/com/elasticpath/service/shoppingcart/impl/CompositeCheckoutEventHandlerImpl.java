/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.impl;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.CheckoutEventHandler;

/**
 * Implementation of {@link CheckoutEventHandler} that utilizes the composite design
 * pattern to allow multiple {@link CheckoutEventHandler} instances to be invoked during
 * the Checkout process.
 */
public class CompositeCheckoutEventHandlerImpl implements CheckoutEventHandler {

	private final List<CheckoutEventHandler> checkoutEventHandlers;

	/**
	 * Constructor.
	 */
	public CompositeCheckoutEventHandlerImpl() {
		checkoutEventHandlers = new ArrayList<>();
	}

	/**
	 * <p>
	 * This event occurs before any action is taken as part of the checkout process.
	 * Iterates through the list of {@link CheckoutEventHandler}s and invokes preCheckout() on each instance.
	 * </p>
	 * <p>
	 * Note that the first exception encountered is immediately thrown upwards to the
	 * caller. No subsequent {@link CheckoutEventHandler} instances will be invoked from
	 * that point onward.
	 * </p>
	 *
	 * @param shoppingCart the {@link ShoppingCart} being checked out
	 */
	@Override
	public void preCheckout(final ShoppingCart shoppingCart) {
		for (final CheckoutEventHandler checkoutEventHandler : getCheckoutEventHandlers()) {
			checkoutEventHandler.preCheckout(shoppingCart);
		}
	}

	/**
	 * <p>
	 * This event occurs after a checkout has been processed but before the order has been persisted, between preCheckout and postCheckout.
	 * Iterates through the list of {@link CheckoutEventHandler}s and invokes preCheckoutOrderPersist() on each instance.
	 * </p>
	 * <p>
	 * Note that the first exception encountered is immediately thrown upwards to the
	 * caller. No subsequent {@link CheckoutEventHandler} instances will be invoked from
	 * that point onward.
	 * </p>
	 *
	 * @param shoppingCart   the {@link ShoppingCart} being checked out
	 * @param completedOrder the order object resulting from the checkout
	 */
	@Override
	public void preCheckoutOrderPersist(final ShoppingCart shoppingCart,
										final Order completedOrder) {
		for (final CheckoutEventHandler checkoutEventHandler : getCheckoutEventHandlers()) {
			checkoutEventHandler.preCheckoutOrderPersist(shoppingCart, completedOrder);
		}
	}

	/**
	 * <p>
	 * This event occurs after a checkout process has completed.
	 * Iterates through the list of {@link CheckoutEventHandler}s and invokes postCheckout() on each instance.
	 * </p>
	 * <p>
	 * Note that the first exception encountered is immediately thrown upwards to the
	 * caller. No subsequent {@link CheckoutEventHandler} instances will be invoked from
	 * that point onward.
	 * </p>
	 *
	 * @param shoppingCart   with the {@link ShoppingCart} being checked out
	 * @param completedOrder the order object resulting from the checkout
	 */
	@Override
	public void postCheckout(final ShoppingCart shoppingCart, final Order completedOrder) {
		for (final CheckoutEventHandler checkoutEventHandler : getCheckoutEventHandlers()) {
			checkoutEventHandler.postCheckout(shoppingCart, completedOrder);
		}
	}

	/**
	 * @param checkoutEventHandlers the checkoutEventHandlers to set
	 */
	public void setCheckoutEventHandlers(final List<CheckoutEventHandler> checkoutEventHandlers) {
		this.checkoutEventHandlers.clear();
		this.checkoutEventHandlers.addAll(checkoutEventHandlers);
	}

	/**
	 * @return the checkoutEventHandlers
	 */
	public List<CheckoutEventHandler> getCheckoutEventHandlers() {
		return new ArrayList<>(checkoutEventHandlers);
	}

}