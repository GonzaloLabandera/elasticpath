/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.history;

import java.util.List;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * The payment event history request for Payment API.
 */
public class PaymentEventHistoryRequest {

	private List<PaymentEvent> ledger;

	/**
	 * Gets ledger.
	 *
	 * @return the ledger
	 */
	public List<PaymentEvent> getLedger() {
		return ledger;
	}

	/**
	 * Sets ledger.
	 *
	 * @param ledger the ledger
	 */
	public void setLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
	}
}
