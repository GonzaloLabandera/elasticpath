/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureOrderFailureCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * CheckoutAction to authorize order payments (pre-authorizations for all shipments).
 */
public class AuthorizePaymentsCheckoutAction implements ReversibleCheckoutAction, PostCaptureOrderFailureCheckoutAction {

	private static final Logger LOG = Logger.getLogger(AuthorizePaymentsCheckoutAction.class);

	private OrderPaymentApiService orderPaymentApiService;

	private OrderPaymentService orderPaymentService;

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		final Order order = context.getOrder();
		if (order == null) {
			LOG.error("Order not found in checkout action context");
		} else {
			orderPaymentApiService.orderCreated(context.getOrder());
		}
	}

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context) throws EpSystemException {
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

	public void setOrderPaymentService(final OrderPaymentService orderPaymentService) {
		this.orderPaymentService = orderPaymentService;
	}

	@Override
	public void postCaptureRollback(final PostCaptureCheckoutActionContext context, final Exception causeForFailure) {
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
			orderPaymentApiService.rollbackOrderCreated(order);
		}
	}
}
