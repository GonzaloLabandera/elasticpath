/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.exception;

/**
 * This exception in case invalid extension.
 */
public class InvalidExtensionException extends RuntimeException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Constructor.
	 *
	 * @param message the message
	 */
	public InvalidExtensionException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message the message
	 * @param cause {@link Throwable}.
	 */
	public InvalidExtensionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
