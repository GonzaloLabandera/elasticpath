/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.shoppingcart.actions.CheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * CheckoutAction to capture order payments.
 */
public class CapturePaymentsCheckoutAction implements ReversibleCheckoutAction {

	private OrderPaymentApiService orderPaymentApiService;
	private OrderService orderService;
	private OrderPaymentService orderPaymentService;

	@Override
	public void execute(final CheckoutActionContext context) throws EpSystemException {
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
	public void rollback(final CheckoutActionContext context) throws EpSystemException {
		final Order order = context.getOrder();
		final List<PaymentEvent> chargedEvents = orderPaymentService.findByOrder(order).stream()
                .filter(orderPayment -> orderPayment.getTransactionType().equals(TransactionType.CHARGE))
                .filter(orderPayment -> orderPayment.getOrderPaymentStatus().equals(OrderPaymentStatus.APPROVED))
                .map(orderPayment -> orderPaymentApiService.buildPaymentEvent(orderPayment, order))
				.collect(Collectors.toList());
		if (order != null) {
			order.getAllShipments()
					.stream()
					.filter(orderShipment -> !(orderShipment instanceof PhysicalOrderShipment))
					.findAny().ifPresent(orderShipment -> orderPaymentApiService.rollbackShipmentCompleted(orderShipment, chargedEvents));
		}
	}

	public void setOrderPaymentApiService(final OrderPaymentApiService orderPaymentApiService) {
		this.orderPaymentApiService = orderPaymentApiService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setOrderPaymentService(final OrderPaymentService orderPaymentService) {
		this.orderPaymentService = orderPaymentService;
	}
}
