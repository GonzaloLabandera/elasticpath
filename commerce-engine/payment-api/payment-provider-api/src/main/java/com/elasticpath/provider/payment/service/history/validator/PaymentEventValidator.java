/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.validator;

import java.util.List;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents the validator of payment events list.
 */
public interface PaymentEventValidator {

	/**
	 * Validates list of payment events.
	 * Throws IllegalStateException if list of payment events is invalid.
	 *
	 * @param paymentEvents list of payment events.
	 */
	void validate(List<PaymentEvent> paymentEvents);

}
