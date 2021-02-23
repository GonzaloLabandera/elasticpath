/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversiblePostCaptureCheckoutAction;

/**
 * CheckoutAction to capture order payments.
 */
public class CapturePaymentsCheckoutAction implements ReversiblePostCaptureCheckoutAction {

	private OrderService orderService;

	@Override
	public void execute(final PostCaptureCheckoutActionContext context) throws EpSystemException {
		final Order order = context.getOrder();

		if (OrderStatus.ONHOLD.equals(order.getStatus())) {
			return;
		}

		orderService.captureImmediatelyShippableShipments(order);

		final Order updatedOrder = orderService.update(order);
		updatedOrder.setModifiedBy(order.getModifiedBy());

		context.setOrder(updatedOrder);
	}


	@Override
	public void rollback(final PostCaptureCheckoutActionContext context) throws EpSystemException {
		//NO-OP - rollbacks are handled by AuthorizePaymentsCheckoutAction
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

}
