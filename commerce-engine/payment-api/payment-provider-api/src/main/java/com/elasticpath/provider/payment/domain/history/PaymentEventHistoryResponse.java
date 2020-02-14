/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.history;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;

/**
 * The payment event history response for Payment API.
 */
public class PaymentEventHistoryResponse {

	private MoneyDTO amountCharged;

	private MoneyDTO amountRefunded;

	/**
	 * Gets amount charged.
	 *
	 * @return the amount charged
	 */
	public MoneyDTO getAmountCharged() {
		return amountCharged;
	}

	/**
	 * Sets amount charged.
	 *
	 * @param amountCharged the amount charged
	 */
	public void setAmountCharged(final MoneyDTO amountCharged) {
		this.amountCharged = amountCharged;
	}

	/**
	 * Gets amount refunded.
	 *
	 * @return the c
	 */
	public MoneyDTO getAmountRefunded() {
		return amountRefunded;
	}

	/**
	 * Sets amount refunded.
	 *
	 * @param amountRefunded the amount refunded
	 */
	public void setAmountRefunded(final MoneyDTO amountRefunded) {
		this.amountRefunded = amountRefunded;
	}
}
