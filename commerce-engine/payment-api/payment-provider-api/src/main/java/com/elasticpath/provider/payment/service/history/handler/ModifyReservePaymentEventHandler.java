/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history.handler;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.SKIPPED;

import java.util.Objects;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents an implementation of {@link PaymentEventHandler} for modify reserve payment event.
 */
public class ModifyReservePaymentEventHandler implements PaymentEventHandler {

	@Override
	public void accumulatePaymentEventInPaymentGroupState(final PaymentGroupState paymentGroupState, final PaymentEvent paymentEvent) {
		if (Objects.equals(paymentEvent.getPaymentStatus(), APPROVED) || Objects.equals(paymentEvent.getPaymentStatus(), SKIPPED)) {
			paymentGroupState.setAvailable(paymentEvent.getAmount());
		}
		paymentGroupState.setPaymentEvent(paymentEvent);
	}

	@Override
	public PaymentGroupState combinePaymentGroupStates(final PaymentGroupState state, final PaymentGroupState anotherState) {
		throw new IllegalStateException(
				"Payment group states with modify reserve origin must never be combined, there is always only one such group per reservation");
	}

}
