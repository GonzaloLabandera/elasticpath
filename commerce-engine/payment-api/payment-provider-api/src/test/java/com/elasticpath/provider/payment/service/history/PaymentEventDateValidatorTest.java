/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.validator.PaymentEventDateValidator;

/**
 * Tests for {@link PaymentEventDateValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentEventDateValidatorTest {

	private final PaymentEventDateValidator paymentEventDateValidator = new PaymentEventDateValidator();

	@Test
	public void validateShouldThrowIllegalStateExceptionWhenModifyReserveEventsHaveSameDates() {
		final Date date = new Date();

		final PaymentEvent paymentEvent1 = mockPaymentEvent(date, MODIFY_RESERVE);
		final PaymentEvent paymentEvent2 = mockPaymentEvent(date, MODIFY_RESERVE);

		assertThatThrownBy(() -> paymentEventDateValidator.validate(Arrays.asList(paymentEvent1, paymentEvent2)))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void validateNotThrowAnyExceptionWhenModifyReserveEventsHaveDifferentDates() {
		final Date date1 = new Date();
		final Date date2 = DateUtils.addMinutes(date1, 1);

		final PaymentEvent paymentEvent1 = mockPaymentEvent(date1, MODIFY_RESERVE);
		final PaymentEvent paymentEvent2 = mockPaymentEvent(date2, MODIFY_RESERVE);

		assertThatCode(() -> paymentEventDateValidator.validate(Arrays.asList(paymentEvent1, paymentEvent2))).doesNotThrowAnyException();
	}

	@Test
	public void validateShouldThrowIllegalStateExceptionWhenReverseChargeEventsHaveSameDates() {
		final Date date = new Date();

		final PaymentEvent paymentEvent1 = mockPaymentEvent(date, REVERSE_CHARGE);
		final PaymentEvent paymentEvent2 = mockPaymentEvent(date, REVERSE_CHARGE);

		assertThatThrownBy(() -> paymentEventDateValidator.validate(Arrays.asList(paymentEvent1, paymentEvent2)))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void validateNotThrowAnyExceptionWhenReverseChargeEventsHaveDifferentDates() {
		final Date date1 = new Date();
		final Date date2 = DateUtils.addMinutes(date1, 1);

		final PaymentEvent paymentEvent1 = mockPaymentEvent(date1, REVERSE_CHARGE);
		final PaymentEvent paymentEvent2 = mockPaymentEvent(date2, REVERSE_CHARGE);

		assertThatCode(() -> paymentEventDateValidator.validate(Arrays.asList(paymentEvent1, paymentEvent2))).doesNotThrowAnyException();
	}

	private PaymentEvent mockPaymentEvent(final Date date, final TransactionType transactionType) {
		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getDate()).thenReturn(date);
		when(paymentEvent.getPaymentType()).thenReturn(transactionType);

		return paymentEvent;
	}

}