/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;

/**
 * Container class for data required by finalizeCheckoutActions.
 */
public class FinalizeCheckoutActionContextImpl implements FinalizeCheckoutActionContext {

	private final Order order;
	private final ShoppingCart shoppingCart;
	private final CustomerSession customerSession;
	private final boolean orderExchange;
	private boolean emailFailed;

	/**
	 * Constructor for creating a FinalizeCheckoutActionContext object from a CheckoutActionContext object.
	 *
	 * @param checkoutActionContext the checkoutActionContext
	 */
	public FinalizeCheckoutActionContextImpl(final PreCaptureCheckoutActionContext checkoutActionContext) {
		this.order = checkoutActionContext.getOrder();
		this.shoppingCart = checkoutActionContext.getShoppingCart();
		this.orderExchange = checkoutActionContext.isOrderExchange();
		this.customerSession = checkoutActionContext.getCustomerSession();
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	@Override
	public Order getOrder() {
		return order;
	}

	@Override
	public Shopper getShopper() {
		return shoppingCart.getShopper();
	}

	@Override
	public ShoppingCart getShoppingCart() {
		return shoppingCart;
	}

	/**
	 * Gets the is order exchange.
	 *
	 * @return the is order exchange
	 */
	@Override
	public boolean isOrderExchange() {
		return orderExchange;
	}

	/**
	 * Sets email failed.
	 *
	 * @param emailFailed the email failed flag
	 **/
	@Override
	public void setEmailFailed(final boolean emailFailed) {
		this.emailFailed = emailFailed;
	}

	/**
	 * Gets email failed.
	 *
	 * @return is email failed
	 */
	@Override
	public boolean isEmailFailed() {
		return emailFailed;
	}

	@Override
	public CustomerSession getCustomerSession() {
		return customerSession;
	}
}