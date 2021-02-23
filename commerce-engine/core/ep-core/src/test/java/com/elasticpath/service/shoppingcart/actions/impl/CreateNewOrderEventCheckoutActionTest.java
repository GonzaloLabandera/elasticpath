/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.order.Order;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;

/**
 * Test class for {@link CreateNewOrderEventCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNewOrderEventCheckoutActionTest {

	private static final String ORDER_NUMBER = "ORDER123";

	@Mock
	private PreCaptureCheckoutActionContext checkoutContext;

	@Mock
	private EventMessageFactory eventMessageFactory;
	@Mock
	private EventMessagePublisher messagePublisher;
	@Mock
	private Order order;
	@Mock
	private EventMessage eventMessage;

	@InjectMocks
	private CreateNewOrderEventCheckoutAction checkoutAction;

	@Before
	public void setUp() {
        when(checkoutContext.getOrder()).thenReturn(order);
		when(order.getOrderNumber()).thenReturn(ORDER_NUMBER);
	}

	@Test
	public void verifyExecuteThrowsEpSystemExceptionOnPublishingError() {
		when(eventMessageFactory.createEventMessage(any(EventType.class), any(String.class), eq(null)))
				.thenReturn(eventMessage);
		doThrow(new RuntimeException("Boom"))
				.when(messagePublisher).publish(any(EventMessage.class));

		assertThatThrownBy(() -> checkoutAction.execute(checkoutContext))
				.isInstanceOf(EpSystemException.class)
				.hasCauseInstanceOf(RuntimeException.class);
	}

	@Test
	public void verifyExecutePublishesEventMessage() {
		when(eventMessageFactory.createEventMessage(any(EventType.class), any(String.class), eq(null)))
				.thenReturn(eventMessage);

		checkoutAction.execute(checkoutContext);

		verify(messagePublisher).publish(eventMessage);
	}

	@Test
	public void verifyExecutePublishesEventMessageWithGiftCertificateFlag() {

		Map<String, Object> data = new HashMap<>();
		data.put("hasGCs", "true");

		when(eventMessageFactory.createEventMessage(any(EventType.class), any(String.class), eq(data)))
				.thenReturn(eventMessage);
		when(order.hasGiftCertificateShipment())
				.thenReturn(true);

		checkoutAction.execute(checkoutContext);
	}

}
