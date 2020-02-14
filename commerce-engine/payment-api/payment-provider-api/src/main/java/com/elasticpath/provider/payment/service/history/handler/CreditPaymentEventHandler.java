/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history.handler;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;

import java.util.Objects;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;

/**
 * Represents an implementation of {@link PaymentEventHandler} for credit payment event.
 */
public class CreditPaymentEventHandler implements PaymentEventHandler {

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Override
	public void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		if (Objects.equals(paymentEvent.getPaymentStatus(), APPROVED) || Objects.equals(paymentEvent.getPaymentStatus(), SKIPPED)) {
			paymentGroupState.setRefunded(moneyDtoCalculator.plus(paymentGroupState.getRefunded(), paymentEvent.getAmount()));
			checkRefundedMoreThenCharged(paymentGroupState);
		}
	}

	@Override
	public PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		throw new IllegalStateException("Credit must never become an origin of a payment group state, origin must always be charge event");
	}

	/**
	 * Checks if we are not attempting to refund more than was charged, otherwise throws exception.
	 *
	 * @param paymentGroupState payment state grouped by originating reservation
	 */
	protected void checkRefundedMoreThenCharged(final PaymentGroupState paymentGroupState) {
		final MoneyDTO refunded = paymentGroupState.getRefunded();
		final MoneyDTO charged = paymentGroupState.getCharged();
		if (moneyDtoCalculator.compare(refunded, charged) > 0) {
			throw new IllegalStateException("Attempting to refund more than was charged: " + refunded + " > " + charged);
		}
	}

	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}
}
