/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * The payment success event.
 */
public class PaymentCapabilityResponse {

	private Map<String, String> data;
	private LocalDateTime processedDateTime;
	private boolean requestHold;

	/**
	 * Gets payment event data.
	 *
	 * @return the payment event data
	 */
	public Map<String, String> getData() {
		return data;
	}

	/**
	 * Sets payment event data.
	 *
	 * @param data the payment event data
	 */
	public void setData(final Map<String, String> data) {
		this.data = data;
	}

	/**
	 * Gets time when the payment was processed.
	 *
	 * @return local time
	 */
	public LocalDateTime getProcessedDateTime() {
		return processedDateTime;
	}

	/**
	 * Sets time when payment was processed.
	 *
	 * @param processedDateTime local time
	 */
	public void setProcessedDateTime(final LocalDateTime processedDateTime) {
		this.processedDateTime = processedDateTime;
	}

	/**
	 * Checks if this event is a hold request.
	 *
	 * @return true to hold the payment request
	 */
	public boolean isRequestHold() {
		return requestHold;
	}

	/**
	 * Sets the payment request on hold.
	 *
	 * @param requestHold true to hold the payment request
	 */
	public void setRequestHold(final boolean requestHold) {
		this.requestHold = requestHold;
	}
}
