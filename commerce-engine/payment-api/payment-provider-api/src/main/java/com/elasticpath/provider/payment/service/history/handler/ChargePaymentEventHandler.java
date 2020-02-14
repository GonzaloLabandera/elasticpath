/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history.handler;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.FAILED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;

import java.util.Objects;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.history.util.MoneyDtoCalculator;

/**
 * Represents an implementation of {@link PaymentEventHandler} for charge payment event.
 */
public class ChargePaymentEventHandler implements PaymentEventHandler {

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Override
	public void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		paymentGroupState.setPaymentEvent(paymentEvent);
		if (Objects.equals(paymentEvent.getPaymentStatus(), FAILED)) {
			paymentGroupState.setAvailable(moneyDtoCalculator.zeroMoneyDto());
		}

		if (Objects.equals(paymentEvent.getPaymentStatus(), APPROVED) || Objects.equals(paymentEvent.getPaymentStatus(), SKIPPED)) {
			checkChargedMoreThanReserved(paymentEvent.getAmount(), paymentGroupState.getAvailable());
			paymentGroupState.setAvailable(moneyDtoCalculator.zeroMoneyDto());
			paymentGroupState.setCharged(paymentEvent.getAmount());
		}
	}

	@Override
	public PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		final PaymentGroupState combinedState = new PaymentGroupState();
		combinedState.setPaymentEvent(state.getPaymentEvent());
		combinedState.setAvailable(state.getAvailable());
		combinedState.setCharged(state.getCharged());
		combinedState.setRefunded(moneyDtoCalculator.plus(state.getRefunded(), anotherState.getRefunded()));
		combinedState.setReverseCharged(moneyDtoCalculator.plus(state.getReverseCharged(), anotherState.getReverseCharged()));
		return combinedState;
	}

	/**
	 * Checks if we are not attempting to charge more than was reserved, otherwise throws exception.
	 *
	 * @param charged  charged amount
	 * @param reserved reserved amount
	 */
	protected void checkChargedMoreThanReserved(final MoneyDTO charged, final MoneyDTO reserved) {
		if (moneyDtoCalculator.compare(charged, reserved) > 0) {
			throw new IllegalStateException("Attempting to charge more than was reserved: " + charged + " > " + reserved);
		}
	}

	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}
}
