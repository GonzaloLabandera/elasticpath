/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;

/**
 * Unit test for {@link CapturePaymentsCheckoutAction}.
 */

@RunWith(MockitoJUnitRunner.class)
public class CapturePaymentsCheckoutActionTest {

	@Mock
	private OrderService orderService;

	@InjectMocks
	private CapturePaymentsCheckoutAction checkoutAction;


	@Test
	public void verifyCaptureNotTriggeredWhenOnHold() {
		final Order order = mock(Order.class);
		final PostCaptureCheckoutActionContext checkoutActionContext = mock(PostCaptureCheckoutActionContext.class);
		given(checkoutActionContext.getOrder()).willReturn(order);

		when(order.getStatus()).thenReturn(OrderStatus.ONHOLD);

		checkoutAction.execute(checkoutActionContext);

		verify(orderService, never()).captureImmediatelyShippableShipments(any());
	}

	@Test
	public void verifyCaptureTriggeredWhenCreated() {
		final Order order = mock(Order.class);
		final Order updatedOrder = mock(Order.class);
		final PostCaptureCheckoutActionContext checkoutActionContext = mock(PostCaptureCheckoutActionContext.class);
		given(checkoutActionContext.getOrder()).willReturn(order);

		when(order.getStatus()).thenReturn(OrderStatus.CREATED);
		when(orderService.update(order)).thenReturn(updatedOrder);

		checkoutAction.execute(checkoutActionContext);

		verify(orderService).captureImmediatelyShippableShipments(order);
	}

}
