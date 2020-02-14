/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.plugin.payment.provider.capabilities;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Payment capability request failed exception.
 */
public class PaymentCapabilityRequestFailedException extends Exception {

	private static final long serialVersionUID = 5000000001L;

	private final String internalMessage;
	private final String externalMessage;
	private final boolean temporaryFailure;

	/**
	 * Constructor.
	 *
	 * @param internalMessage  internal message
	 * @param externalMessage  external message
	 * @param temporaryFailure if this is temporary failure
	 */
	public PaymentCapabilityRequestFailedException(final String internalMessage,
												   final String externalMessage,
												   final boolean temporaryFailure) {
		this.internalMessage = internalMessage;
		this.externalMessage = externalMessage;
		this.temporaryFailure = temporaryFailure;
	}

	/**
	 * Constructor.
	 *
	 * @param cause            exception cause
	 * @param externalMessage  external message
	 * @param temporaryFailure if this is temporary failure
	 */
	public PaymentCapabilityRequestFailedException(final Throwable cause,
												   final String externalMessage,
												   final boolean temporaryFailure) {
		this(convertStackTraceToString(cause), externalMessage, temporaryFailure);
	}

	private static String convertStackTraceToString(final Throwable throwable) {
		if (throwable == null) {
			return "Cause is null, probably this was a NullPointerException";
		}
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter, true);
		throwable.printStackTrace(printWriter);
		return stringWriter.getBuffer().toString();
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

	/**
	 * Is temporary failure boolean.
	 *
	 * @return the boolean
	 */
	public boolean isTemporaryFailure() {
		return temporaryFailure;
	}

}
