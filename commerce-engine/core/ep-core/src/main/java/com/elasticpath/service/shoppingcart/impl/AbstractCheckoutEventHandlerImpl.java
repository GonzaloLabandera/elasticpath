/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.impl;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.CheckoutEventHandler;

/**
 * Abstract implementation of a <code>CheckoutEventHandler</code>. This serves as a convenience
 * class so that subclasses can only implement the events of interest.
 * 
 */
public abstract class AbstractCheckoutEventHandlerImpl implements CheckoutEventHandler {

	@Override
	public void preCheckout(final ShoppingCart shoppingCart) {
		//Do nothing by default
	}

	@Override
	public void preCheckoutOrderPersist(final ShoppingCart shoppingCart, final Order completedOrder) {
		//Do nothing by default
	}

	@Override
	public void postCheckout(final ShoppingCart shoppingCart, final Order completedOrder) {
		//Do nothing by default
	}
	
}
