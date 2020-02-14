/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.creation;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Response object to encapsulate the payment instrument creation response.
 */
public class PaymentInstrumentCreationResponse {

	private final Map<String, String> details;

	/**
	 * Constructor.
	 *
	 * @param details Map containing payment instrument creation response details.
	 */
	public PaymentInstrumentCreationResponse(final Map<String, String> details) {
		this.details = Optional.ofNullable(details)
				.map(Collections::unmodifiableMap)
				.orElse(Collections.emptyMap());
	}

	/**
	 * Gets creation response details.
	 *
	 * @return the creation response details
	 */
	public Map<String, String> getDetails() {
		return details;
	}

}
