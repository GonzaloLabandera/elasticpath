/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.capabilities.instructions;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Payment instrument creation instructions.
 */
public class PICInstructions {

	private final Map<String, String> communicationInstructions;
	private final Map<String, String> payload;

	/**
	 * Constructor.
	 *
	 * @param communicationInstructions payment communication instructions.
	 * @param payload                   payment instructions payload.
	 */
	public PICInstructions(final Map<String, String> communicationInstructions, final Map<String, String> payload) {
		this.communicationInstructions =
				Optional.ofNullable(communicationInstructions).map(Collections::unmodifiableMap).orElse(Collections.emptyMap());
		this.payload = Optional.ofNullable(payload).map(Collections::unmodifiableMap).orElse(Collections.emptyMap());
	}

	public Map<String, String> getCommunicationInstructions() {
		return communicationInstructions;
	}

	public Map<String, String> getPayload() {
		return payload;
	}

}
