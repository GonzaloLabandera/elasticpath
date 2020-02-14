/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * CheckoutAction to authorize order payments (pre-authorizations for all shipments).
 */
public class AuthorizePaymentsCheckoutAction implements ReversibleCheckoutAction {

	private static final Logger LOG = Logger.getLogger(AuthorizePaymentsCheckoutAction.class);

	private OrderPaymentApiService orderPaymentApiService;

	@Override
	public void execute(final CheckoutActionContext context) throws EpSystemException {
		final Order order = context.getOrder();
		if (order == null) {
			LOG.error("Order not found in checkout action context");
		} else {
			orderPaymentApiService.orderCreated(context.getOrder());
		}
	}

	@Override
	public void rollback(final CheckoutActionContext context) throws EpSystemException {
		final Order order = context.getOrder();
		if (order == null) {
			LOG.error("Order not found in checkout action context");
		} else {
			orderPaymentApiService.rollbackOrderCreated(order);
		}
	}

	public void setOrderPaymentApiService(final OrderPaymentApiService orderPaymentApiService) {
		this.orderPaymentApiService = orderPaymentApiService;
	}
}
