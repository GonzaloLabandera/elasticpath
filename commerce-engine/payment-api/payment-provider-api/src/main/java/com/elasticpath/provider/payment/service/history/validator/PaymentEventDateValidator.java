/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.validator;

import static com.elasticpath.plugin.payment.provider.dto.TransactionType.MODIFY_RESERVE;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.REVERSE_CHARGE;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents an implementation of {@link PaymentEventDateValidator}.
 */
public class PaymentEventDateValidator implements PaymentEventValidator {

	private final List<TransactionType> validatedTransactionTypes = Arrays.asList(MODIFY_RESERVE, REVERSE_CHARGE);

	@Override
	public void validate(final List<PaymentEvent> paymentEvents) {
		final List<PaymentEvent> validatedPaymentEvents = paymentEvents.stream()
				.filter(isPaymentEventTransactionTypeOneOf(validatedTransactionTypes)).collect(Collectors.toList());

		final Set<Date> paymentSequenceDates = validatedPaymentEvents.stream()
				.map(PaymentEvent::getDate)
				.collect(Collectors.toSet());

		final long dateCount = paymentSequenceDates.size();
		final long paymentEventCount = validatedPaymentEvents.size();

		if (dateCount != paymentEventCount) {
			throw new IllegalStateException("Payment event sequence contains same date for different payment events");
		}
	}

	private Predicate<PaymentEvent> isPaymentEventTransactionTypeOneOf(final List<TransactionType> transactionTypes) {
		return paymentEvent -> transactionTypes.contains(paymentEvent.getPaymentType());
	}

}
