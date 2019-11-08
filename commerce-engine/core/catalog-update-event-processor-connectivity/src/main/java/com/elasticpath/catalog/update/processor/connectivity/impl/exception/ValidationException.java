/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.exception;

/**
 * Validation exception.
 */
public class ValidationException extends RuntimeException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Validation exception.
	 *
	 * @param cause {@link Throwable}.
	 */
	public ValidationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Validation exception.
	 *
	 * @param message validation exception message.
	 */
	public ValidationException(final String message) {
		super(message);
	}

}
