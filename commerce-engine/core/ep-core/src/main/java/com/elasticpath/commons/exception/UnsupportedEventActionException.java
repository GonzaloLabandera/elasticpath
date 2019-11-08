/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.commons.exception;

/**
 * This exception will be thrown when event action is not supported by EventType.
 */
public class UnsupportedEventActionException extends RuntimeException {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Creates a new object with the given message.
	 *
	 * @param message the reason for exception.
	 */
	public UnsupportedEventActionException(final String message) {
		super(message);
	}

	/**
	 * Creates a new object with the given message.
	 *
	 * @param message the reason for exception
	 * @param throwable the <code>Throwable</code> that caused this exception.
	 */
	public UnsupportedEventActionException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
