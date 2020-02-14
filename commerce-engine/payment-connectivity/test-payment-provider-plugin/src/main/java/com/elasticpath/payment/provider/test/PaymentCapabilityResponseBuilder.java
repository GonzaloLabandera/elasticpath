/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.payment.provider.test;

import java.time.LocalDateTime;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;

/**
 * The type Builder.
 */
final class PaymentCapabilityResponseBuilder {
	private Map<String, String> data;
	private LocalDateTime processedDateTime;
	private boolean requestHold;

	private PaymentCapabilityResponseBuilder() {
	}

	/**
	 * A payment capability response builder.
	 *
	 * @return the builder
	 */
	static PaymentCapabilityResponseBuilder aResponse() {
		return new PaymentCapabilityResponseBuilder();
	}

	/**
	 * With payment event data builder.
	 *
	 * @param data the payment event data
	 * @return the builder
	 */
	PaymentCapabilityResponseBuilder withData(final Map<String, String> data) {
		this.data = data;
		return this;
	}

	/**
	 * With processed date time type builder.
	 *
	 * @param processedDateTime the local time of the processed request
	 * @return the builder
	 */
	PaymentCapabilityResponseBuilder withProcessedDateTime(final LocalDateTime processedDateTime) {
		this.processedDateTime = processedDateTime;
		return this;
	}

	/**
	 * With payment request hold type builder.
	 *
	 * @param requestHold the payment request hold status
	 * @return the builder
	 */
	PaymentCapabilityResponseBuilder withRequestHold(final boolean requestHold) {
		this.requestHold = requestHold;
		return this;
	}

	/**
	 * Build payment event.
	 *
	 * @return the payment event
	 */
	PaymentCapabilityResponse build() {
		PaymentCapabilityResponse response = new PaymentCapabilityResponse();
		response.setData(data);
		response.setProcessedDateTime(processedDateTime);
		response.setRequestHold(requestHold);
		return response;
	}
}
