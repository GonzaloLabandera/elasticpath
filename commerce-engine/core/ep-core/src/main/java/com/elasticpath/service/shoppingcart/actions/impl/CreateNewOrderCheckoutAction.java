/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.order.Order;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.OrderFactory;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureOrderFailureCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * CheckoutAction to create a new order from the shopping cart.
 */

public class CreateNewOrderCheckoutAction implements ReversibleCheckoutAction, PostCaptureOrderFailureCheckoutAction {

	private static final Logger LOG = LogManager.getLogger(CreateNewOrderCheckoutAction.class);

	private OrderFactory orderFactory;

	private OrderService orderService;
	private EventMessageFactory eventMessageFactory;
	private EventMessagePublisher eventMessagePublisher;

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		final Order newOrder = orderFactory.createAndPersistNewEmptyOrder(
				context.getShopper().getCustomer(),
				context.getCustomerSession(),
				context.getShoppingCart(),
				context.isOrderExchange(),
				context.isAwaitExchangeCompletion());

		// Set the newly created order on the context before calling populateOrder()
		// so that if populateOrder() fails, the order is already on the context and rollback() can call Order#failOrder
		context.setOrder(newOrder);
		populateOrder(context, newOrder);
	}

	/**
	 * Given an empty order, populates the empty order with the appropriate values
	 * from the PreCaptureCheckoutActionContext.
	 *
	 * @param context    the {@link PreCaptureCheckoutActionContext}
	 * @param emptyOrder an empty {@link Order}
	 * @return the populated Order.
	 */
	protected Order populateOrder(final PreCaptureCheckoutActionContext context, final Order emptyOrder) {
		if (context.isOrderExchange()) {
			return orderFactory.fillInNewExchangeOrderFromShoppingCart(
					emptyOrder,
					context.getShopper().getCustomer(),
					context.getCustomerSession(),
					context.getShoppingCart(),
					context.getShoppingCartTaxSnapshot(),
					context.isAwaitExchangeCompletion(),
					context.getExchange());
		}

		return orderFactory.fillInNewOrderFromShoppingCart(
				emptyOrder,
				context.getShopper().getCustomer(),
				context.getCustomerSession(),
				context.getShoppingCart(),
				context.getShoppingCartTaxSnapshot());
	}

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		Order order = context.getOrder();
		if (order == null) {
			LOG.error("Order not found in checkout action context");
		} else {
			try {
				order.failOrder();
				if (order.isPersisted()) {
					order = orderService.update(order);
				} else {
					order = orderService.add(order);
				}
				context.setOrder(order);
				if (LOG.isDebugEnabled()) {
					LOG.debug("failing order: " + order.getOrderNumber());
				}
				publishOrderFailedEvent(order.getOrderNumber());
			} catch (final Exception e) {
				LOG.error("Can't set the order status to failing " + order, e);
			}
		}
	}

	/**
	 * Publishes an ORDER_FAILED event to the order event topic.
	 *
	 * @param orderNumber the order id associated with the event
	 */
	protected void publishOrderFailedEvent(final String orderNumber) {
		try {
			final EventMessage eventMessage = getEventMessageFactory().createEventMessage(OrderEventType.ORDER_FAILED, orderNumber);
			getEventMessagePublisher().publish(eventMessage);
		} catch (final Exception e) {
			LOG.error("Could not publish ORDER_FAILED event.", e);
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	protected OrderFactory getOrderFactory() {
		return orderFactory;
	}

	public void setOrderFactory(final OrderFactory orderFactory) {
		this.orderFactory = orderFactory;
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return this.eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

	@Override
	public void postCaptureRollback(final PostCaptureCheckoutActionContext context, final Exception causeForFailure) {
		publishOrderFailedEvent(context.getOrder().getOrderNumber());
	}
}
