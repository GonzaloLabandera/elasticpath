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
import com.elasticpath.provider.payment.service.history.handler.CreditPaymentEventHandler;
import com.elasticpath.provider.payment.service.history.handler.PaymentGroupState;

/**
 * Tests for {@link CreditPaymentEventHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreditPaymentEventHandlerTest {

	private final CreditPaymentEventHandler creditPaymentEventHandler = new CreditPaymentEventHandler();

	private static final BigDecimal TWENTY = BigDecimal.valueOf(20);
	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

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

		creditPaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, paymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(storedPaymentEvent);
		assertThat(paymentState.getAvailable()).isEqualTo(available);
		assertThat(paymentState.getCharged()).isEqualTo(charged);
	}

	@Test
	public void chargedAmountShouldBeUnchangedWhenChargedAmountIsOneHundredAndCreditIsTwentyAndPaymentEventIsApproved() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO charged = mock(MoneyDTO.class);
		final MoneyDTO refunded = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(ONE_HUNDRED);
		when(refunded.getAmount()).thenReturn(ZERO);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setCharged(charged);
		paymentState.setRefunded(refunded);

		final MoneyDTO credit = mock(MoneyDTO.class);
		when(credit.getAmount()).thenReturn(TWENTY);

		final PaymentEvent newPaymentEvent = mock(PaymentEvent.class);
		when(newPaymentEvent.getPaymentStatus()).thenReturn(APPROVED);
		when(newPaymentEvent.getAmount()).thenReturn(credit);

		creditPaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, newPaymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(storedPaymentEvent);
		assertThat(paymentState.getCharged().getAmount()).isEqualTo(ONE_HUNDRED);
		assertThat(paymentState.getRefunded().getAmount()).isEqualTo(TWENTY);
	}

	@Test
	public void chargedAmountShouldBeUnchangedWhenChargedAmountIsOneHundredAndCreditIsTwentyAndPaymentEventIsSkipped() {
		final PaymentEvent storedPaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO charged = mock(MoneyDTO.class);
		final MoneyDTO refunded = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(ONE_HUNDRED);
		when(refunded.getAmount()).thenReturn(ZERO);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(storedPaymentEvent);
		paymentState.setCharged(charged);
		paymentState.setRefunded(refunded);

		final MoneyDTO credit = mock(MoneyDTO.class);
		when(credit.getAmount()).thenReturn(TWENTY);

		final PaymentEvent newPaymentEvent = mock(PaymentEvent.class);
		when(newPaymentEvent.getPaymentStatus()).thenReturn(SKIPPED);
		when(newPaymentEvent.getAmount()).thenReturn(credit);

		creditPaymentEventHandler.accumulatePaymentEventInPaymentGroupState(paymentState, newPaymentEvent);

		assertThat(paymentState.getPaymentEvent()).isEqualTo(storedPaymentEvent);
		assertThat(paymentState.getCharged().getAmount()).isEqualTo(ONE_HUNDRED);
		assertThat(paymentState.getRefunded().getAmount()).isEqualTo(TWENTY);
	}

	@Test
	public void handleShouldThrowIllegalStateExceptionWhenChargedIsLessThenCredit() {
		final PaymentEvent chargePaymentEvent = mock(PaymentEvent.class);
		final MoneyDTO available = mock(MoneyDTO.class);
		final MoneyDTO charged = mock(MoneyDTO.class);
		final MoneyDTO refunded = mock(MoneyDTO.class);
		when(charged.getAmount()).thenReturn(TWENTY);
		when(refunded.getAmount()).thenReturn(ZERO);

		final PaymentGroupState paymentState = new PaymentGroupState();
		paymentState.setPaymentEvent(chargePaymentEvent);
		paymentState.setAvailable(available);
		paymentState.setCharged(charged);
		paymentState.setRefunded(refunded);

		final MoneyDTO credit = mock(MoneyDTO.class);
		when(credit.getAmount()).thenReturn(ONE_HUNDRED);

		final PaymentEvent newPaymentEvent = mock(PaymentEvent.class);
		when(newPaymentEvent.getPaymentStatus()).thenReturn(APPROVED);
		when(newPaymentEvent.getAmount()).thenReturn(credit);

		assertThatThrownBy(() -> creditPaymentEventHandler
				.accumulatePaymentEventInPaymentGroupState(paymentState, newPaymentEvent))
				.isInstanceOf(IllegalStateException.class);

	}

}
