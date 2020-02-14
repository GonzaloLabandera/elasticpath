/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.validator;

import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;

import java.util.List;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents an implementation of {@link PaymentEventSequenceValidator}.
 */
public class PaymentEventSequenceValidator implements PaymentEventValidator {
	private final Map<TransactionType, List<TransactionType>> validPaymentEventSequences;

	private static final String INVALID_PAYMENT_EVENT_SEQUENCE = "Invalid payment event sequence";

	/**
	 * Constructor.
	 *
	 * @param validPaymentEventSequences map with transaction type.
	 */
	public PaymentEventSequenceValidator(final Map<TransactionType, List<TransactionType>> validPaymentEventSequences) {
		this.validPaymentEventSequences = validPaymentEventSequences;
	}

	@Override
	public void validate(final List<PaymentEvent> paymentEvents) {
		if (!paymentEvents.get(0).getPaymentType().equals(RESERVE)) {
			throw new IllegalStateException(INVALID_PAYMENT_EVENT_SEQUENCE);
		}
		for (int i = 0; i < paymentEvents.size() - 1; i++) {
			ensureSequence(paymentEvents.get(i), paymentEvents.get(i + 1));
		}
	}

	private void ensureSequence(final PaymentEvent currentPaymentEvent, final PaymentEvent nextPaymentEvent) {
		final List<TransactionType> transactionTypes = validPaymentEventSequences.get(currentPaymentEvent.getPaymentType());
		if (currentPaymentEvent.getPaymentStatus().equals(PaymentStatus.APPROVED) && !transactionTypes.contains(nextPaymentEvent.getPaymentType())) {
			throw new IllegalStateException(INVALID_PAYMENT_EVENT_SEQUENCE);
		}
	}
}
