/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service.history.handler;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents an interface to provide accumulation handling operations with payment events.
 */
public interface PaymentEventHandler {

	/**
	 * Accumulate payment event in payment group state.
	 *
	 * @param paymentGroupState payment group state.
	 * @param paymentEvent      payment event.
	 */
	void accumulatePaymentEventInPaymentGroupState(PaymentGroupState paymentGroupState, PaymentEvent paymentEvent);

	/**
	 * Combines payment group states based on originating event.
	 *
	 * @param state        payment group state
	 * @param anotherState another payment group state
	 * @return combined payment group state
	 */
	PaymentGroupState combinePaymentGroupStates(PaymentGroupState state, PaymentGroupState anotherState);

}
