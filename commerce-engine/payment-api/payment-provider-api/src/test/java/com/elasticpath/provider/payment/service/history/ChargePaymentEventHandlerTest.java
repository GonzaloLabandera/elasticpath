/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.handler.ChargePaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.PaymentGroupState;

/**
 * Tests for {@link ChargePaymentEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChargePaymentEventHandlerTest {

	private final ChargePaymentEventHandler chargePaymentEventHandler = new ChargePaymentEventHandler();

	private static final BigDecimal EIGHTY = BigDecimal.valueOf(80);
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

	@Test
	public void availableAmountShouldBeZeroWhenPaymentEventIsFailed() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO available = mock(MoneyDTO.class);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setAvailable(available);

		final PaymentEvent newPaymentEvent = mock(PaymentEvent.class);
		when(newPaymentEvent.getPaymentStatus()).thenReturn(FAILED);

		chargePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, newPaymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(newPaymentEvent);
		assertThat(paymentState.getAvailable().getAmount()).isEqualTo(ZERO);
	}

	@Test
	public void availableAmountShouldBeZeroAndChargedAmountShouldBeSameAsInPaymentEventWhenPaymentEventIsApproved() {
		final MoneyDTO available = mock(MoneyDTO.class);
		when(available.getAmount()).thenReturn(ONE_HUNDRED);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setAvailable(available);

		final MoneyDTO charged = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(EIGHTY);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(APPROVED);
		when(paymentEvent.getAmount()).thenReturn(charged);

		chargePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(paymentEvent);
		assertThat(paymentState.getAvailable().getAmount()).isEqualTo(ZERO);
		assertThat(paymentState.getCharged()).isEqualTo(charged);
	}

	@Test
	public void availableAmountShouldBeZeroAndChargedAmountShouldBeSameAsInPaymentEventWhenPaymentEventIsSkipped() {
		final MoneyDTO available = mock(MoneyDTO.class);
		when(available.getAmount()).thenReturn(ONE_HUNDRED);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setAvailable(available);

		final MoneyDTO charged = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(EIGHTY);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(SKIPPED);
		when(paymentEvent.getAmount()).thenReturn(charged);

		chargePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(paymentEvent);
		assertThat(paymentState.getAvailable().getAmount()).isEqualTo(ZERO);
		assertThat(paymentState.getCharged()).isEqualTo(charged);
	}

	@Test
	public void handleShouldThrowIllegalStateExceptionWhenAvailableIsLessThenCharged() {
		final MoneyDTO available = mock(MoneyDTO.class);
		when(available.getAmount()).thenReturn(EIGHTY);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setAvailable(available);

		final MoneyDTO charged = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(ONE_HUNDRED);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(APPROVED);
		when(paymentEvent.getAmount()).thenReturn(charged);

		assertThatThrownBy(() -> chargePaymentEventHandler
				.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent))
				.isInstanceOf(IllegalStateException.class);
	}

}