/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.CheckoutEventHandler;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureOrderFailureCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * CheckoutAction to persist (update) the order after checkout processing.
 */
public class UpdateOrderCheckoutAction implements ReversibleCheckoutAction, PostCaptureOrderFailureCheckoutAction {

	private static final Logger LOG = LogManager.getLogger(UpdateOrderCheckoutAction.class);

	private CheckoutEventHandler checkoutEventHandler;

	private OrderService orderService;

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		// Notify the checkout event handler before the order is persisted
		// No part of the order should be saved until after this point so that
		// the checkoutEventHandler can reject an order by throwing an exception
		checkoutEventHandler.preCheckoutOrderPersist(context.getShoppingCart(),	context.getOrder());

		//process and update order - should limit our updates to once
		final Order updatedOrder = orderService.processOrderOnCheckout(context.getOrder(),
				context.getShoppingCart().isExchangeOrderShoppingCart());
		context.setOrder(updatedOrder);
	}

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context)
			throws EpSystemException {
		// NO OP
	}

	protected CheckoutEventHandler getCheckoutEventHandler() {
		return checkoutEventHandler;
	}

	public void setCheckoutEventHandler(final CheckoutEventHandler checkoutEventHandler) {
		this.checkoutEventHandler = checkoutEventHandler;
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	@Override
	public void postCaptureRollback(final PostCaptureCheckoutActionContext context, final Exception causeForFailure) {
		final Order order = context.getOrder();
		if (order == null) {
			LOG.error("Order not found in post capture checkout action context");
		} else {
			orderService.update(order);
		}
	}
}
