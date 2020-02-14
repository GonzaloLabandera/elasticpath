/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.handler.CancelReservePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.PaymentGroupState;

/**
 * Tests for {@link CancelReservePaymentEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CancelReservePaymentEventHandlerTest {

	private final CancelReservePaymentEventHandler cancelReservePaymentEventHandler = new CancelReservePaymentEventHandler();

	@Test
	public void availableShouldBeZeroIndependentOfPaymentEventStatus() {
		final PaymentGroupState paymentState = new PaymentGroupState();

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);

		cancelReservePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(paymentEvent);
		assertThat(paymentState.getAvailable().getAmount()).isEqualTo(ZERO);
	}

}