/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents an implementation of {@link PaymentEventCurrencyValidator}.
 */
public class PaymentEventCurrencyValidator implements PaymentEventValidator {

	@Override
	public void validate(final List<PaymentEvent> paymentEvents) {
		final Set<String> paymentEventsCurrencyCodes = paymentEvents.stream()
				.map(PaymentEvent::getAmount)
				.map(MoneyDTO::getCurrencyCode)
				.collect(Collectors.toSet());

		if (paymentEventsCurrencyCodes.size() > 1) {
			throw new IllegalStateException("Different currency codes in one payment events sequence");
		}

	}
}
