/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.order.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_HOLD;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderService;

/**
 * Implementation of the OrderHoldService for interacting with OrderHold entities.
 */
public class OrderHoldServiceImpl extends AbstractEpPersistenceServiceImpl implements OrderHoldService {

	private static final Logger LOG = Logger.getLogger(OrderHoldServiceImpl.class);

	private static final String CM_USERNAME = "cm_username";
	private static final String ORDERHOLD_GUID = "orderhold_guid";
	private static final String ORDER_HOLD_COMMENT = "comment";
	private static final String ORDER_HOLD_STATUS = "status";

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private OrderService orderService;

	private OrderEventHelper orderEventHelper;

	@Override
	public boolean isAllHoldsResolvedForOrder(final long orderUid) {
		final List<Long> results = getPersistenceEngine().retrieveByNamedQuery("COUNT_UNRESOLVED_ORDER_HOLDS_BY_ORDER_UID", orderUid);
		return results.get(0) == 0;
	}

	@Override
	public List<OrderHold> findOrderHoldsByOrderUid(final long orderUid) {
		return getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_HOLDS_BY_ORDER_UID", orderUid);
	}

	@Override
	public void addHoldsToOrder(final Order order, final Set<OrderHold> orderHolds) {
		orderHolds.forEach(orderHold -> {
			orderHold.setOrderUid(order.getUidPk());
			getPersistenceEngine().save(orderHold);
		});
	}

	@Override
	public Order holdOrder(final Order order, final Set<OrderHold> orderHolds) {
		if (!order.isHoldable()) {
			throw new EpServiceException("Order [" + order.getUidPk() + "] is not holdable.");
		}

		order.holdOrder();
		final Order updatedOrder = orderService.update(order);
		addHoldsToOrder(updatedOrder, orderHolds);

		updatedOrder.setModifiedBy(order.getModifiedBy());
		getOrderEventHelper().logOrderOnHold(updatedOrder);

		sendEvent(OrderEventType.ORDER_HELD, updatedOrder.getOrderNumber(), null);

		return updatedOrder;
	}

	@Override
	public void markHoldUnresolvable(final Order order, final OrderHold orderHold, final String cmUserName, final String comment) {
		if (orderContainsHold(order, orderHold)) {
			sendEvent(OrderEventType.ORDER_HOLDS_RESOLVED,
					order.getOrderNumber(),
					ImmutableMap.of(
							ORDERHOLD_GUID, orderHold.getGuid(),
							CM_USERNAME, cmUserName,
							ORDER_HOLD_COMMENT, comment,
							ORDER_HOLD_STATUS, OrderHoldStatus.UNRESOLVABLE.getName()
					)
			);
		} else {
			LOG.error("An attempt was made to reject an order hold that did not belong to the order [" + order.getUidPk() + "]");
			throw new EpServiceException("An attempt was made to reject an order hold that did not belong to the order [" + order.getUidPk() + "]");
		}
	}

	@Override
	public void markHoldResolved(final Order order, final OrderHold orderHold, final String cmUserName, final String comment) {
		if (orderContainsHold(order, orderHold)) {
			sendEvent(OrderEventType.ORDER_HOLDS_RESOLVED,
					order.getOrderNumber(),
					ImmutableMap.of(
							ORDERHOLD_GUID, orderHold.getGuid(),
							CM_USERNAME, cmUserName,
							ORDER_HOLD_COMMENT, comment,
							ORDER_HOLD_STATUS, OrderHoldStatus.RESOLVED.getName()
					)
			);
		} else {
			LOG.error("An attempt was made to approve an order hold that did not belong to the order [" + order.getUidPk() + "]");
			throw new EpServiceException("An attempt was made to approve an order hold that did not belong to the order [" + order.getUidPk() + "]");
		}
	}

	private boolean orderContainsHold(final Order order, final OrderHold orderHold) {
		return order.getUidPk() == orderHold.getOrderUid();
	}

	/**
	 * Publishes an order event.
	 *
	 * @param eventType      the type of Event to trigger
	 * @param orderNumber    the order id associated with the event
	 * @param additionalData additional data to include in the message
	 */
	protected void sendEvent(final EventType eventType, final String orderNumber, final Map<String, Object> additionalData) {
		// Send notification via messaging system
		try {
			final EventMessage eventMessage = getEventMessageFactory().createEventMessage(eventType, orderNumber, additionalData);

			getEventMessagePublisher().publish(eventMessage);

		} catch (final Exception e) {
			throw new EpSystemException("Failed to publish Event Message", e);
		}
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	@Override
	public OrderHold get(final long orderHoldGuid) {
		sanityCheck();
		OrderHold orderHold;
		if (orderHoldGuid <= 0) {
			orderHold = getPrototypeBean(ORDER_HOLD, OrderHold.class);
		} else {
			orderHold = getPersistentBeanFinder().get(ORDER_HOLD, orderHoldGuid);
		}
		return orderHold;
	}

	@Override
	public OrderHold getByGuid(final String orderHoldGuid) {
		sanityCheck();
		OrderHold orderHold = null;
		if (orderHoldGuid == null) {
			orderHold = getPrototypeBean(ORDER_HOLD, OrderHold.class);
		} else {
			List<OrderHold> orderHolds = getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_HOLD_BY_GUID", orderHoldGuid);
			if (!orderHolds.isEmpty()) {
				orderHold = orderHolds.get(0);
			}
		}
		return orderHold;
	}

	@Override
	public OrderHold update(final OrderHold orderHold) {
		sanityCheck();
		return getPersistenceEngine().merge(orderHold);
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return eventMessageFactory;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected OrderEventHelper getOrderEventHelper() {
		return orderEventHelper;
	}

	public void setOrderEventHelper(final OrderEventHelper orderEventHelper) {
		this.orderEventHelper = orderEventHelper;
	}
}
