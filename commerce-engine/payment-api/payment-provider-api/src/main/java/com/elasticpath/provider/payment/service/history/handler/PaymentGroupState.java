/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history.handler;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents the state of the group of payment events in the history.
 * Events are grouped by the originating reservation event.
 */
public class PaymentGroupState {

	private PaymentEvent paymentEvent;
	private MoneyDTO available;
	private MoneyDTO charged;
	private MoneyDTO refunded;
	private MoneyDTO reverseCharged;

	/**
	 * Gets payment event.
	 *
	 * @return payment event.
	 */
	public PaymentEvent getPaymentEvent() {
		return paymentEvent;
	}

	/**
	 * Sets payment event.
	 *
	 * @param paymentEvent payment event.
	 */
	public void setPaymentEvent(final PaymentEvent paymentEvent) {
		this.paymentEvent = paymentEvent;
	}

	/**
	 * Gets available amount of MoneyDTO.
	 *
	 * @return available amount of MoneyDTO.
	 */
	public MoneyDTO getAvailable() {
		return available;
	}

	/**
	 * Sets available amount.
	 *
	 * @param available available amount.
	 */
	public void setAvailable(final MoneyDTO available) {
		this.available = available;
	}

	/**
	 * Gets charged amount of MoneyDTO.
	 *
	 * @return charged amount of MoneyDTO.
	 */
	public MoneyDTO getCharged() {
		return charged;
	}

	/**
	 * Sets charged amount.
	 *
	 * @param charged charged amount.
	 */
	public void setCharged(final MoneyDTO charged) {
		this.charged = charged;
	}

	/**
	 * Gets refunded amount.
	 *
	 * @return MoneyDTO
	 */
	public MoneyDTO getRefunded() {
		return refunded;
	}

	/**
	 * Sets refunded amount.
	 *
	 * @param refunded refunded amount
	 */
	public void setRefunded(final MoneyDTO refunded) {
		this.refunded = refunded;
	}

	/**
	 * Gets reverse Charged amount.
	 *
	 * @return MoneyDTO
	 */
	public MoneyDTO getReverseCharged() {
		return reverseCharged;
	}

	/**
	 * Sets reverse Charged amount.
	 *
	 * @param reverseCharged reverse Charged amount
	 */
	public void setReverseCharged(final MoneyDTO reverseCharged) {
		this.reverseCharged = reverseCharged;
	}
}
