/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service;

/**
 * Business state message id for payment module errors.
 *
 * @see <a href="https://developers.elasticpath.com/commerce/7.5/Cortex-API-Front-End-Development/API-Reference/Business-State-Errors">
 * Business State Errors
 * </a>
 */
public enum PaymentsExceptionMessageId {

	//
	// Inherited
	//

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
	 * Payment failed - payment method is missing.
	 */
	PAYMENT_METHOD_MISSING("payment.method.missing", "Payment method must be present"),
	/**
	 * Payment failed - payment method has expired.
	 */
	PAYMENT_METHOD_EXPIRED("payment.method.expired", "Payment method has expired."),
	/**
	 * Payment failed - shipping address is not defined.
	 */
	PAYMENT_METHOD_INVALID("payment.method.invalid", "Payment method is invalid."),
	/**
	 * Payment failed - currency not supported.
	 */
	PAYMENT_CURRENCY_NOT_SUPPORTED("payment.currency.not.supported", "Payment currency is not supported by the store."),
	/**
	 * Payment failed - capability unsupported error.
	 */
	PAYMENT_CAPABILITY_UNSUPPORTED ("payment.capability.unsupported", "Capability is not supported by payment provider"),

	//
	// Added for Payment API
	//

	/**
	 * Payment instrument creation failed.
	 */
	PAYMENT_INSTRUMENT_CREATION_FAILED("payment.instrument.creation.failed", "Payment instrument creation failed."),

	/**
	 * Payment instrument creation failed - empty name.
	 */
	PAYMENT_INSTRUMENT_EMPTY_NAME("payment.instrument.name.required", "Name must not be blank.");

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
