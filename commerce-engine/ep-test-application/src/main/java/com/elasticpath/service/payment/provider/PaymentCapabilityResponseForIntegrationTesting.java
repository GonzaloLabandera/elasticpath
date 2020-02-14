/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.payment.provider;

import java.time.LocalDateTime;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;

/**
 * Default implementation for {@link PaymentCapabilityResponse}.
 */
class PaymentCapabilityResponseForIntegrationTesting extends PaymentCapabilityResponse {

	private Map<String, String> data = ImmutableMap.of("test-key", "test-value");
	private LocalDateTime processedDateTime = LocalDateTime.now();
	private boolean requestHold;

	@Override
	public Map<String, String> getData() {
		return data;
	}

	@Override
	public void setData(final Map<String, String> data) {
		this.data = data;
	}

	@Override
	public LocalDateTime getProcessedDateTime() {
		return processedDateTime;
	}

	@Override
	public void setProcessedDateTime(final LocalDateTime processedDateTime) {
		this.processedDateTime = processedDateTime;
	}

	@Override
	public boolean isRequestHold() {
		return requestHold;
	}

	@Override
	public void setRequestHold(final boolean requestHold) {
		this.requestHold = requestHold;
	}
}
