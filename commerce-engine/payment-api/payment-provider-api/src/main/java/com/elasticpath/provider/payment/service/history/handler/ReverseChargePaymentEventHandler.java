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
 * Represents an implementation of {@link PaymentEventHandler} for reverse charge payment event.
 */
public class ReverseChargePaymentEventHandler implements PaymentEventHandler {

	private final MoneyDtoCalculator moneyDtoCalculator = new MoneyDtoCalculator();

	@Override
	public void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		if (Objects.equals(paymentEvent.getPaymentStatus(), APPROVED) || Objects.equals(paymentEvent.getPaymentStatus(), SKIPPED)) {
			checkAnythingCharged(paymentGroupState);
			checkAnythingReverseCharged(paymentGroupState);
			checkChargedEqualsReverseCharged(paymentGroupState.getCharged(), paymentEvent.getAmount());
			paymentGroupState.setReverseCharged(paymentEvent.getAmount());
		}
	}

	@Override
	public PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		throw new IllegalStateException("Reverse charge must never become an origin of a payment group state, origin must always be charge");
	}

	/**
	 * Checks if there were any charges recorded in the state, otherwise throws exception.
	 *
	 * @param paymentGroupState payment state grouped by originating reservation
	 */
	protected void checkAnythingCharged(final PaymentGroupState paymentGroupState) {
		final MoneyDTO charged = paymentGroupState.getCharged();
		if (moneyDtoCalculator.compare(moneyDtoCalculator.zeroMoneyDto(), charged) == 0) {
			throw new IllegalStateException("Attempting to reverse absent charge");
		}
	}

	/**
	 * Checks if we are not attempting to reverse zero otherwise throws exception.
	 *
	 * @param paymentGroupState payment state grouped by originating reservation
	 */
	protected void checkAnythingReverseCharged(final PaymentGroupState paymentGroupState) {
		final MoneyDTO reverseCharged = paymentGroupState.getReverseCharged();
		if (moneyDtoCalculator.compare(moneyDtoCalculator.zeroMoneyDto(), reverseCharged) != 0) {
			throw new IllegalStateException("Attempting to make a second reverse operation");
		}
	}

	private void checkChargedEqualsReverseCharged(final MoneyDTO charged, final MoneyDTO paymentEventAmount) {
		if (moneyDtoCalculator.compare(charged, paymentEventAmount)  != 0) {
			throw new IllegalStateException("Attempting to reverse charge more than was charged");
		}
	}

	protected MoneyDtoCalculator getMoneyDtoCalculator() {
		return moneyDtoCalculator;
	}
}
