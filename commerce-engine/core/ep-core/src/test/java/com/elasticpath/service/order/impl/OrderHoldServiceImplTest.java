/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.order.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.core.messaging.order.OrderEventType;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.order.OrderService;

@RunWith(MockitoJUnitRunner.class)
public class OrderHoldServiceImplTest {

	private static final String ORDER_NUMBER = "order_1";
	private static final long ORDER_UID = 1;
	private static final String ORDER_HOLD_UNRESOLVABLE_COMMENT = "ORDER HOLD UNRESOLVABLE COMMENT";
	private static final String ORDER_HOLD_RESOLVED_COMMENT = "ORDER HOLD RESOLVED COMMENT";
	private static final String ORDER_HOLD_GUID = "ORDER_HOLD_GUID";
	private static final String CMUSER_NAME = "CMUSER_NAME";
	private static final long WRONG_ORDER_UID = 1000;

	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessagePublisher eventMessagePublisher;
	@Mock
	private Order order;
	@Mock
	private OrderHold orderHold;
	@Mock
	private OrderService orderService;
	@Mock
	private OrderEventHelper orderEventHelper;
	@Mock
	private PersistenceEngine persistenceEngine;

	@InjectMocks
	private OrderHoldServiceImpl orderHoldService;

	@Test
	public void holdOrderSuccess() {
		EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessageFactory.createEventMessage(OrderEventType.ORDER_HELD, ORDER_NUMBER, null)).thenReturn(eventMessage);
		when(order.isHoldable()).thenReturn(Boolean.TRUE);
		when(order.getOrderNumber()).thenReturn(ORDER_NUMBER);
		when(order.getUidPk()).thenReturn(ORDER_UID);
		when(orderService.update(order)).thenReturn(order);

		Set<OrderHold> orderHoldSet = Collections.singleton(orderHold);
		orderHoldService.holdOrder(order, orderHoldSet);

		verify(order).holdOrder();
		verify(persistenceEngine).save(orderHold);
		verify(orderService).update(order);
		verify(orderHold).setOrderUid(ORDER_UID);
		verify(orderEventHelper).logOrderOnHold(order);
		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void holdOrderNotHoldable() {
		when(order.isHoldable()).thenReturn(Boolean.FALSE);

		Set<OrderHold> orderHoldSet = Collections.singleton(orderHold);

		try {
			orderHoldService.holdOrder(order, orderHoldSet);
			fail("Order that is not holdable should not be held");
		} catch (EpSystemException e) {
			//order is not holdable as the isHoldable check failed -  this is expected
		}
	}

	@Test
	public void markHoldUnresolvable() {
		EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessageFactory.createEventMessage(eq(OrderEventType.ORDER_HOLDS_RESOLVED), eq(ORDER_NUMBER), any())).thenReturn(eventMessage);

		when(order.getOrderNumber()).thenReturn(ORDER_NUMBER);
		when(order.getUidPk()).thenReturn(ORDER_UID);
		when(orderHold.getOrderUid()).thenReturn(ORDER_UID);
		when(orderHold.getGuid()).thenReturn(ORDER_HOLD_GUID);

		orderHoldService.markHoldUnresolvable(order, orderHold, CMUSER_NAME, ORDER_HOLD_UNRESOLVABLE_COMMENT);

		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void markInvalidHoldUnresolvable() {
		when(order.getUidPk()).thenReturn(WRONG_ORDER_UID);
		when(orderHold.getOrderUid()).thenReturn(ORDER_UID);
		try {
			orderHoldService.markHoldUnresolvable(order, orderHold, CMUSER_NAME, ORDER_HOLD_UNRESOLVABLE_COMMENT);
			fail("Order should not be rejectable with a hold that it does not own.");
		} catch (EpServiceException e) {
			//order should not be rejected as it does not own the hold - this is expected
		}

	}

	@Test
	public void resolveHold() {
		EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessageFactory.createEventMessage(eq(OrderEventType.ORDER_HOLDS_RESOLVED), eq(ORDER_NUMBER), any())).thenReturn(eventMessage);

		when(order.getOrderNumber()).thenReturn(ORDER_NUMBER);
		when(order.getUidPk()).thenReturn(ORDER_UID);
		when(orderHold.getGuid()).thenReturn(ORDER_HOLD_GUID);
		when(orderHold.getOrderUid()).thenReturn(ORDER_UID);

		orderHoldService.markHoldResolved(order, orderHold, CMUSER_NAME, ORDER_HOLD_RESOLVED_COMMENT);

		verify(eventMessagePublisher).publish(eventMessage);
	}

	@Test
	public void resolveInvalidHold() {
		when(order.getUidPk()).thenReturn(WRONG_ORDER_UID);
		when(orderHold.getOrderUid()).thenReturn(ORDER_UID);

		try {
			orderHoldService.markHoldResolved(order, orderHold, CMUSER_NAME, ORDER_HOLD_RESOLVED_COMMENT);
			fail("Order should not be resolved with a hold that it does not own.");
		} catch (EpServiceException e) {
			//order should not be resolved as it does not own the hold - this is expected
		}
	}

	@Test
	public void testAddHoldsToOrder() {
		when(order.getUidPk()).thenReturn(ORDER_UID);

		Set<OrderHold> orderHoldSet = Collections.singleton(orderHold);
		orderHoldService.addHoldsToOrder(order, orderHoldSet);

		verify(orderHold).setOrderUid(ORDER_UID);
		verify(persistenceEngine).save(orderHold);
	}

	@Test
	public void testAddEmptyHoldsToOrder() {
		orderHoldService.addHoldsToOrder(order, Collections.emptySet());

		verify(persistenceEngine, never()).merge(any());
	}

	@Test
	public void testFindOrderHoldsByOrderUid() {
		when(persistenceEngine.retrieveByNamedQuery(any(), eq(ORDER_UID))).thenReturn(Collections.singletonList(orderHold));

		final List<OrderHold> orderHolds = orderHoldService.findOrderHoldsByOrderUid(ORDER_UID);

		assertTrue("The expected Order Hold was not returned.", orderHolds.contains(orderHold));
	}

	@Test
	public void testFindOrderHoldsWithInvalidOrderUid() {
		final List<OrderHold> orderHolds = orderHoldService.findOrderHoldsByOrderUid(-1);

		assertTrue("An empty collection should be returned for an invalid order uid.", orderHolds.isEmpty());
	}

	@Test
	public void testIsAllHoldsResolvedForOrderWithMultipleResolvedHolds() {
		final long unresolvedHoldsCount = 0L;
		when(persistenceEngine.retrieveByNamedQuery(any(), eq(ORDER_UID))).thenReturn(Collections.singletonList(unresolvedHoldsCount));

		boolean isResolved = orderHoldService.isAllHoldsResolvedForOrder(ORDER_UID);

		assertTrue("Order with all holds in RESOLVED status must report as resolved.", isResolved);
	}

	@Test
	public void testIsAllHoldsResolvedForOrderWithOneUnresolvedHold() {
		final long unresolvedHoldsCount = 1L;
		when(persistenceEngine.retrieveByNamedQuery(any(), eq(ORDER_UID))).thenReturn(Collections.singletonList(unresolvedHoldsCount));

		boolean isResolved = orderHoldService.isAllHoldsResolvedForOrder(ORDER_UID);

		assertFalse("Order with a hold in a status other than RESOLVED must not report as resolved.", isResolved);
	}

	@Test
	public void testOrderHoldUpdate() {
		when(persistenceEngine.merge(orderHold)).thenReturn(orderHold);

		final OrderHold updatedOrderHold = orderHoldService.update(orderHold);

		verify(persistenceEngine).merge(orderHold);
		assertSame(updatedOrderHold, orderHold);
	}

}