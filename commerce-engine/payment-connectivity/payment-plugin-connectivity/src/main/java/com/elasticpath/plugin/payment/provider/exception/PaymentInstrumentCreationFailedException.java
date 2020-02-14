/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.exception;

import java.util.List;

/**
 * Payment instrument creation failed exception.
 */
public class PaymentInstrumentCreationFailedException extends Exception {

	private static final long serialVersionUID = 5000000001L;

	private final List<ErrorMessage> structuredErrorMessages;

	/**
	 * Constructor.
	 *
	 * @param structuredErrorMessages lis of error messages
	 */
	public PaymentInstrumentCreationFailedException(final List<ErrorMessage> structuredErrorMessages) {
		this.structuredErrorMessages = structuredErrorMessages;
	}

	/**
	 * Get list of structured error messages.
	 *
	 * @return messages list
	 */
	public List<ErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}
}
