/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.service.shoppingcart.CheckoutEventHandler;
import com.elasticpath.service.shoppingcart.actions.CheckoutAction;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;

/**
 * CheckoutAction to trigger preCheckout events registered through checkoutEventHandler.
 */
public class PreCheckoutCheckoutAction implements CheckoutAction {
	private CheckoutEventHandler checkoutEventHandler;

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) {
		checkoutEventHandler.preCheckout(context.getShoppingCart());
	}

	protected CheckoutEventHandler getCheckoutEventHandler() {
		return checkoutEventHandler;
	}

	public void setCheckoutEventHandler(final CheckoutEventHandler checkoutEventHandler) {
		this.checkoutEventHandler = checkoutEventHandler;
	}
}
