/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.validator.PaymentEventCurrencyValidator;

/**
 * Tests for {@link PaymentEventCurrencyValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentEventCurrencyValidatorTest {

	private final PaymentEventCurrencyValidator paymentEventCurrencyValidator = new PaymentEventCurrencyValidator();

	@Test
	public void validateShouldThrowIllegalStateExceptionWhenEventsHaveDifferentCurrency() {
		final MoneyDTO cadCurrencyMoneyDto = mock(MoneyDTO.class);
		when(cadCurrencyMoneyDto.getCurrencyCode()).thenReturn("CAD");

		final PaymentEvent paymentEvent1 = mock(PaymentEvent.class);
		when(paymentEvent1.getAmount()).thenReturn(cadCurrencyMoneyDto);

		final MoneyDTO usdCurrencyMoneyDto = mock(MoneyDTO.class);
		when(usdCurrencyMoneyDto.getCurrencyCode()).thenReturn("USD");

		final PaymentEvent paymentEvent2 = mock(PaymentEvent.class);
		when(paymentEvent2.getAmount()).thenReturn(usdCurrencyMoneyDto);

		assertThatThrownBy(() -> paymentEventCurrencyValidator.validate(Arrays.asList(paymentEvent1, paymentEvent2)))
				.isInstanceOf(IllegalStateException.class);

	}

	@Test
	public void validateNotThrowAnyExceptionWhenEventsHaveSameCurrency() {
		final MoneyDTO cadCurrencyMoneyDto1 = mock(MoneyDTO.class);
		when(cadCurrencyMoneyDto1.getCurrencyCode()).thenReturn("CAD");

		final PaymentEvent paymentEvent1 = mock(PaymentEvent.class);
		when(paymentEvent1.getAmount()).thenReturn(cadCurrencyMoneyDto1);

		final MoneyDTO cadCurrencyMoneyDto2 = mock(MoneyDTO.class);
		when(cadCurrencyMoneyDto2.getCurrencyCode()).thenReturn("CAD");

		final PaymentEvent paymentEvent2 = mock(PaymentEvent.class);
		when(paymentEvent2.getAmount()).thenReturn(cadCurrencyMoneyDto2);

		assertThatCode(() -> paymentEventCurrencyValidator.validate(Arrays.asList(paymentEvent1, paymentEvent2))).doesNotThrowAnyException();
	}
}