/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history.handler;

import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;

/**
 * Represents an implementation of {@link PaymentEventHandler} for cancel reserve payment event.
 */
public class CancelReservePaymentEventHandler implements PaymentEventHandler {

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Override
	public void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		paymentGroupState.setPaymentEvent(paymentEvent);
		paymentGroupState.setAvailable(moneyDtoCalculator.zeroMoneyDto());
	}

	@Override
	public PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		throw new IllegalStateException(
				"Payment group states with cancel reserve origin must never be combined, there is always only one such group per reservation");
	}

	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}
}
