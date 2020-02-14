/*
 * Copyright (c) Elastic Path Software Inc., 2020
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
import com.elasticpath.provider.payment.service.history.handler.PaymentGroupState;
import com.elasticpath.provider.payment.service.history.handler.ReverseChargePaymentEventHandler;

/**
 * Tests for {@link ReverseChargePaymentEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReverseChargePaymentEventHandlerTest {

	private static final BigDecimal TWENTY = BigDecimal.valueOf(20);

	private final ReverseChargePaymentEventHandler reverseChargePaymentEventHandler = new ReverseChargePaymentEventHandler();

	@Test
	public void handleShouldNotChangeAvailableAndChargedMoneyDtoWhenPaymentEventIsFailed() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO available = mock(MoneyDTO.class);
		final MoneyDTO charged = mock(MoneyDTO.class);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setAvailable(available);
		paymentState.setCharged(charged);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getPaymentStatus()).thenReturn(FAILED);

		reverseChargePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(storedPaymentEvent);
		assertThat(paymentState.getAvailable()).isEqualTo(available);
		assertThat(paymentState.getCharged()).isEqualTo(charged);
	}

	@Test
	public void chargedAmountShouldBeZeroAndReverseChargedShouldBeTwentyWhenPaymentEventIsApproved() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO charged = mock(MoneyDTO.class);
		final MoneyDTO reverseCharged = mock(MoneyDTO.class);

		when(charged.getAmount()).thenReturn(TWENTY);
		when(reverseCharged.getAmount()).thenReturn(ZERO);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setCharged(charged);
		paymentState.setReverseCharged(reverseCharged);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getAmount()).thenReturn(charged);
		when(paymentEvent.getPaymentStatus()).thenReturn(APPROVED);

		reverseChargePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(storedPaymentEvent);
		assertThat(paymentState.getCharged().getAmount()).isEqualTo(TWENTY);
		assertThat(paymentState.getReverseCharged().getAmount()).isEqualTo(TWENTY);
	}

	@Test
	public void chargedAmountShouldBeZeroAndReverseChargedAmountShouldBeZeroWhenPaymentEventIsSkipped() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO charged = mock(MoneyDTO.class);
		final MoneyDTO reverseCharge = mock(MoneyDTO.class);

		when(charged.getAmount()).thenReturn(TWENTY);
		when(reverseCharge.getAmount()).thenReturn(ZERO);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setCharged(charged);
		paymentState.setReverseCharged(reverseCharge);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getAmount()).thenReturn(charged);
		when(paymentEvent.getPaymentStatus()).thenReturn(SKIPPED);

		reverseChargePaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(storedPaymentEvent);
		assertThat(paymentState.getCharged().getAmount()).isEqualTo(TWENTY);
		assertThat(paymentState.getReverseCharged().getAmount()).isEqualTo(TWENTY);
	}

	@Test
	public void handleShouldThrowIllegalStateExceptionWhenChargedIsZero() {
		final MoneyDTO charged = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(ZERO);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(mock(PaymentEvent.class));
		paymentState.setCharged(charged);

		final PaymentEvent newPaymentEvent = mock(PaymentEvent.class);
		when(newPaymentEvent.getPaymentStatus()).thenReturn(APPROVED);

		assertThatThrownBy(() -> reverseChargePaymentEventHandler
				.accumulatePaymentEventInPaymentGroupState(paymentState, newPaymentEvent))
				.isInstanceOf(IllegalStateException.class);
	}

}