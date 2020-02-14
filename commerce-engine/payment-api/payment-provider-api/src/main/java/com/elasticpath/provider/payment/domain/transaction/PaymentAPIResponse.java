/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction;

import java.util.List;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Payment API response containing list of payment events.
 */
public class PaymentAPIResponse {
	private final List<PaymentEvent> events;
	private boolean success;
	private String internalMessage;
	private String externalMessage;

	/**
	 * Constructor.
	 *
	 * @param events  list of payment events
	 * @param success the success
	 */
	public PaymentAPIResponse(final List<PaymentEvent> events, final boolean success) {
		this.events = events;
		this.success = success;
	}

	/**
	 * Constructor.
	 *
	 * @param events          list of payment events
	 * @param externalMessage external message.
	 * @param internalMessage internal message.
	 */
	public PaymentAPIResponse(final List<PaymentEvent> events, final String externalMessage, final String internalMessage) {
		this.events = events;
		this.externalMessage = externalMessage;
		this.internalMessage = internalMessage;
	}

	/**
	 * Get payment event list.
	 *
	 * @return payment events
	 */
	public List<PaymentEvent> getEvents() {
		return events;
	}

	/**
	 * Is transaction successful boolean.
	 *
	 * @return true if successful
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Gets internal message.
	 *
	 * @return the internal message
	 */
	public String getInternalMessage() {
		return internalMessage;
	}

	/**
	 * Gets external message.
	 *
	 * @return the external message
	 */
	public String getExternalMessage() {
		return externalMessage;
	}
}
