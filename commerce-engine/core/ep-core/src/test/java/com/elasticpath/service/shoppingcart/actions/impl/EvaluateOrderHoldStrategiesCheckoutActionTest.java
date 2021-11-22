/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHold;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.xpf.bridges.OrderHoldStrategyXPFBridge;
import com.elasticpath.xpf.connectivity.extensionpoint.OrderHoldStrategy;

@RunWith(MockitoJUnitRunner.class)
public class EvaluateOrderHoldStrategiesCheckoutActionTest {


	List<OrderHoldStrategy> orderHoldStrategyList = new ArrayList<>();

	@Mock
	OrderHoldStrategy mockOrderHoldStrategy;

	@Mock
	OrderHoldService mockOrderHoldService;

	@Mock
	OrderService mockOrderService;

	@Mock
	Order mockOrder;

	@Mock
	PreCaptureCheckoutActionContext mockPreCaptureCheckoutActionContext;

	@Mock
	private OrderHoldStrategyXPFBridge orderHoldStrategyXPFBridge;

	@InjectMocks
	EvaluateOrderHoldStrategiesCheckoutAction evaluateOrderHoldStrategiesCheckoutAction;

	@Before
	public void setUp() throws Exception {
		orderHoldStrategyList.add(mockOrderHoldStrategy);
	}

	@Test
	public void testStrategyEvaluationWithHold() {
		OrderHold orderHold = mock(OrderHold.class);

		when(orderHoldStrategyXPFBridge.evaluateOrderHolds(mockPreCaptureCheckoutActionContext)).thenReturn(Collections.singletonList(orderHold));
		when(mockPreCaptureCheckoutActionContext.getOrder()).thenReturn(mockOrder);

		evaluateOrderHoldStrategiesCheckoutAction.execute(mockPreCaptureCheckoutActionContext);

		verify(orderHoldStrategyXPFBridge).evaluateOrderHolds(mockPreCaptureCheckoutActionContext);
		verify(mockOrderHoldService).holdOrder(eq(mockOrder), any());
	}

	@Test
	public void testStrategyEvaluationWithoutHold() {
		when(orderHoldStrategyXPFBridge.evaluateOrderHolds(mockPreCaptureCheckoutActionContext)).thenReturn(Collections.emptyList());

		when(mockPreCaptureCheckoutActionContext.getOrder()).thenReturn(mockOrder);
		when(mockOrderService.triggerPostCaptureCheckout(mockOrder)).thenReturn(mockOrder);

		evaluateOrderHoldStrategiesCheckoutAction.execute(mockPreCaptureCheckoutActionContext);

		verify(orderHoldStrategyXPFBridge).evaluateOrderHolds(mockPreCaptureCheckoutActionContext);
		verify(mockOrderService).triggerPostCaptureCheckout(mockOrder);
		verify(mockOrderHoldService, never()).holdOrder(any(), any());

	}

}