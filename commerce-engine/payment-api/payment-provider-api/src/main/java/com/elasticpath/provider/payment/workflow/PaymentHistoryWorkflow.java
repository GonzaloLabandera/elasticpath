/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequest;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponse;

/**
 * The interface Payment History Workflow service.
 */
public interface PaymentHistoryWorkflow {

	/**
	 * Returns payment event history amounts like refunded, charged for a list of payment events.
	 *
	 * @param paymentEventHistoryRequest the payment event history request.
	 * @return payment event history response.
	 */
	PaymentEventHistoryResponse getPaymentEventHistoryAmounts(PaymentEventHistoryRequest paymentEventHistoryRequest);
}
