/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;

/**
 * Charge payment processor.
 */
public interface ChargeProcessor {
	/**
	 * Charge payment processing.
	 *
	 * @param chargeRequest the charge request
	 * @return list of payment events
	 */
	PaymentAPIResponse chargePayment(ChargeRequest chargeRequest);
}
