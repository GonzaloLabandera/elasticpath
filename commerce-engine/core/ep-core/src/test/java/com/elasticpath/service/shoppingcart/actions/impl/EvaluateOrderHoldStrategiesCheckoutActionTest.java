/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
import com.elasticpath.service.shoppingcart.actions.OrderHoldStrategy;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;

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

	@InjectMocks
	EvaluateOrderHoldStrategiesCheckoutAction evaluateOrderHoldStrategiesCheckoutAction;

	@Before
	public void setUp() throws Exception {
		orderHoldStrategyList.add(mockOrderHoldStrategy);
	}

	@Test
	public void testStrategyEvaluationWithHold() {
		evaluateOrderHoldStrategiesCheckoutAction.setOrderHoldStrategyList(Collections.singletonList(mockOrderHoldStrategy));

		OrderHold orderHold = mock(OrderHold.class);

		when(mockOrderHoldStrategy.evaluate(mockPreCaptureCheckoutActionContext)).thenReturn(Optional.of(orderHold));
		when(mockPreCaptureCheckoutActionContext.getOrder()).thenReturn(mockOrder);

		evaluateOrderHoldStrategiesCheckoutAction.execute(mockPreCaptureCheckoutActionContext);

		verify(mockOrderHoldStrategy).evaluate(mockPreCaptureCheckoutActionContext);
		verify(mockOrderHoldService).holdOrder(eq(mockOrder), any());
	}

	@Test
	public void testStrategyEvaluationWithoutHold() {
		evaluateOrderHoldStrategiesCheckoutAction.setOrderHoldStrategyList(Collections.singletonList(mockOrderHoldStrategy));

		when(mockOrderHoldStrategy.evaluate(mockPreCaptureCheckoutActionContext)).thenReturn(Optional.empty());
		when(mockPreCaptureCheckoutActionContext.getOrder()).thenReturn(mockOrder);
		when(mockOrderService.triggerPostCaptureCheckout(mockOrder)).thenReturn(mockOrder);

		evaluateOrderHoldStrategiesCheckoutAction.execute(mockPreCaptureCheckoutActionContext);

		verify(mockOrderHoldStrategy).evaluate(mockPreCaptureCheckoutActionContext);
		verify(mockOrderService).triggerPostCaptureCheckout(mockOrder);
	}

}