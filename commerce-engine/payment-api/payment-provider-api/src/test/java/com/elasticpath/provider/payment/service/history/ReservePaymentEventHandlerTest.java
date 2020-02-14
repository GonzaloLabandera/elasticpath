/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.handler.PaymentGroupState;
import com.elasticpath.provider.payment.service.history.handler.ReservePaymentEventHandler;

/**
 * Tests for {@link ReservePaymentEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReservePaymentEventHandlerTest {

	private final ReservePaymentEventHandler reservePaymentEventHandler = new ReservePaymentEventHandler();

	@Test
	public void availableAndChargedMoneyDtoShouldResetWhenPaymentEventIsFailed() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO available = mock(MoneyDTO.class);
		final MoneyDTO charged = mock(MoneyDTO.class);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setAvailable(available);
		paymentState.setCharged(charged);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(FAILED);

		reservePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(paymentEvent);
		assertThat(paymentState.getAvailable().getAmount()).isEqualTo(ZERO);
		assertThat(paymentState.getCharged().getAmount()).isEqualTo(ZERO);
	}

	@Test
	public void availableAmountShouldBeSameAsInPaymentEventWhenPaymentEventIsApproved() {
		final PaymentGroupState paymentState = new PaymentGroupState();

		final MoneyDTO moneyDTO = mock(MoneyDTO.class);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(APPROVED);
		when(paymentEvent.getAmount()).thenReturn(moneyDTO);

		reservePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(paymentEvent);
		assertThat(paymentState.getAvailable()).isEqualTo(moneyDTO);
	}

	@Test
	public void availableAmountShouldBeSameAsInPaymentEventWhenPaymentEventIsSkipped() {
		final PaymentGroupState paymentState = new PaymentGroupState();

		final MoneyDTO moneyDTO = mock(MoneyDTO.class);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(SKIPPED);
		when(paymentEvent.getAmount()).thenReturn(moneyDTO);

		reservePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(paymentEvent);
		assertThat(paymentState.getAvailable()).isEqualTo(moneyDTO);
	}

}