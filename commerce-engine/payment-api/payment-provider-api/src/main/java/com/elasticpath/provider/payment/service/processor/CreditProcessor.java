/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;

/**
 * Credit/refund payment processor.
 */
public interface CreditProcessor {
	/**
	 * Refund payment on payment instrument processing.
	 *
	 * @param creditRequest the implicit credit request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse credit(CreditRequest creditRequest);

	/**
	 * Manual refund payment processing.
	 *
	 * @param creditRequest the manual credit request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse manualCredit(ManualCreditRequest creditRequest);

	/**
	 * Reverse charge payment processing.
	 *
	 * @param reverseChargeRequest the reverse charge request
	 * @return Payment API response with list of payment events
	 */
	PaymentAPIResponse reverseCharge(ReverseChargeRequest reverseChargeRequest);
}
