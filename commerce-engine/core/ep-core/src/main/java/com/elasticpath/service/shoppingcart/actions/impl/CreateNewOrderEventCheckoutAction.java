/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.order.Order;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureOrderFailureCheckoutAction;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * Checkout action to create and publish an {@link OrderEventType} {@link EventMessage}.
 */
public class CreateNewOrderEventCheckoutAction implements ReversibleCheckoutAction, PostCaptureOrderFailureCheckoutAction {

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private static final Logger LOG = LogManager.getLogger(CreateNewOrderEventCheckoutAction.class);

	@Override
	public void execute(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		try {
			Map<String, Object> data = null;
			if (context.getOrder().hasGiftCertificateShipment()) {
				data = new HashMap<>();
				data.put("hasGCs", "true");
			}

			final EventMessage orderCreatedEventMessage = getEventMessageFactory().createEventMessage(OrderEventType.ORDER_CREATED,
					context.getOrder().getOrderNumber(), data);


			getEventMessagePublisher().publish(orderCreatedEventMessage);
		} catch (Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	@Override
	public void rollback(final PreCaptureCheckoutActionContext context) throws EpSystemException {
		// NO OP
	}

	@Override
	public void postCaptureRollback(final PostCaptureCheckoutActionContext context, final Exception causeForFailure) {
		final Order order = context.getOrder();

		if (order == null) {
			LOG.error("Order not found in post capture checkout action context");
		} else {
			order.failOrder();
			LOG.error("Order marked as failed " + order, causeForFailure);
		}
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
}
