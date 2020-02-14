/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.validator;

import java.util.List;

import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;

/**
 * Represents an implementation of {@link PaymentEventNegativeAmountValidator}.
 */
public class PaymentEventNegativeAmountValidator implements PaymentEventValidator {

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Override
	public void validate(final List<PaymentEvent> paymentEvents) {
		if (paymentEvents.stream().map(PaymentEvent::getAmount).anyMatch(moneyDtoCalculator::isNegative)) {
			throw new IllegalStateException("Payment event contains negative money amount");
		}
	}

}
