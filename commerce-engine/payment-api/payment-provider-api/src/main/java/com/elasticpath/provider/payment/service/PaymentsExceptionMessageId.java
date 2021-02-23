/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.service;

/**
 * Business state message id for payment module errors.
 *
 * @see <a href="https://documentation.elasticpath.com/commerce/docs/cortex/frontend-dev/api/structured-messages.html#business-state-errors">
 * Business State Errors
 * </a>
 */
public enum PaymentsExceptionMessageId {

	/**
	 * Payment failed - generic error.
	 */
	PAYMENT_FAILED("payment.failed", "Error occurred when processing payment."),

	/**
	 * Payment failed - declined by payment processor.
	 */
	PAYMENT_DECLINED("payment.declined", "Payment was declined."),

	/**
	 * Payment failed - insufficient funds or daily limit exceeded.
	 */
	PAYMENT_INSUFFICIENT_FUNDS("payment.insufficient.funds", "Insufficient funds to process payment."),

	/**
	 * Payment instrument creation failed.
	 */
	PAYMENT_INSTRUMENT_CREATION_FAILED("payment.instrument.creation.failed", "Payment instrument creation failed."),

	/**
	 * Payment instrument creation failed - empty name.
	 */
	PAYMENT_INSTRUMENT_EMPTY_NAME("payment.instrument.name.required", "Name must not be blank."),

	/**
	 * Payment failed - payment method is missing.
	 */
	PAYMENT_METHOD_MISSING("payment.method.missing", "Payment method must be present"),

	/**
	 * Payment failed - capability unsupported error.
	 */
	PAYMENT_CAPABILITY_UNSUPPORTED("payment.capability.unsupported", "Capability is not supported by payment provider");

	private final String key;
	private final String defaultDebugMessage;

	/**
	 * Constructor.
	 *
	 * @param key                 unique error message key
	 * @param defaultDebugMessage default debug error message
	 */
	PaymentsExceptionMessageId(final String key, final String defaultDebugMessage) {
		this.key = key;
		this.defaultDebugMessage = defaultDebugMessage;
	}

	/**
	 * Get unique error message key.
	 *
	 * @return unique error message key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Get default debug message.
	 *
	 * @return debug message
	 */
	public String getDefaultDebugMessage() {
		return defaultDebugMessage;
	}
}
