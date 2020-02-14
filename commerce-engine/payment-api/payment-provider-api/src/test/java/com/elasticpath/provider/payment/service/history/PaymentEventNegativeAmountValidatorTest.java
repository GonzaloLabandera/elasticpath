/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.validator.PaymentEventNegativeAmountValidator;

/**
 * Tests for {@link PaymentEventNegativeAmountValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentEventNegativeAmountValidatorTest {

	private final PaymentEventNegativeAmountValidator paymentEventNegativeAmountValidator = new PaymentEventNegativeAmountValidator();

	@Test
	public void validateShouldThrowIllegalStateExceptionWhenEventHasNegativeAmount() {
		final BigDecimal negativeAmount = BigDecimal.valueOf(-1);

		final MoneyDTO moneyDTO = mock(MoneyDTO.class);
		when(moneyDTO.getAmount()).thenReturn(negativeAmount);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getAmount()).thenReturn(moneyDTO);

		assertThatThrownBy(() -> paymentEventNegativeAmountValidator.validate(Collections.singletonList(paymentEvent)))
				.isInstanceOf(IllegalStateException.class);

	}

	@Test
	public void validateNotThrowAnyExceptionWhenEventHasPositiveAmount() {
		final BigDecimal positiveAmount = BigDecimal.ONE;

		final MoneyDTO moneyDTO = mock(MoneyDTO.class);
		when(moneyDTO.getAmount()).thenReturn(positiveAmount);

		final PaymentEvent paymentEvent = mock(PaymentEvent.class);
		when(paymentEvent.getAmount()).thenReturn(moneyDTO);

		assertThatCode(() -> paymentEventNegativeAmountValidator.validate(Collections.singletonList(paymentEvent))).doesNotThrowAnyException();
	}

}