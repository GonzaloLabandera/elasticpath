/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history.handler;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;

import java.util.Objects;

import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;

/**
 * Represents an implementation of {@link PaymentEventHandler} for reserve payment event.
 */
public class ReservePaymentEventHandler implements PaymentEventHandler {

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Override
	public void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		if (Objects.equals(paymentEvent.getPaymentStatus(), APPROVED) || Objects.equals(paymentEvent.getPaymentStatus(), SKIPPED)) {
			paymentGroupState.setAvailable(paymentEvent.getAmount());
		} else {
			paymentGroupState.setAvailable(moneyDtoCalculator.zeroMoneyDto());
		}
		paymentGroupState.setPaymentEvent(paymentEvent);
		paymentGroupState.setCharged(moneyDtoCalculator.zeroMoneyDto());
		paymentGroupState.setRefunded(moneyDtoCalculator.zeroMoneyDto());
		paymentGroupState.setReverseCharged(moneyDtoCalculator.zeroMoneyDto());
	}

	@Override
	public PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		throw new IllegalStateException("Payment group states originating with reservation event must not be combined, this is a tree root event");
	}

	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}
}
