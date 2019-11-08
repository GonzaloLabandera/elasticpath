/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.update.processor.connectivity.impl.exception;

/**
 * Exception that throws if LocaleByValue map is not present.
 */
public class LocaleByValueNotFoundException extends RuntimeException {

	/**
	 * Values not found message.
	 */
	public static final String VALUES_NOT_FOUND_MESSAGE_FOR_MAP = "Values not found for map: ";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Creates instance of {@link LocaleByValueNotFoundException}.
	 */
	public LocaleByValueNotFoundException() {
		super();
	}

	/**
	 * Constructor with parameter.
	 *
	 * @param message - error message of exception.
	 */
	public LocaleByValueNotFoundException(final String message) {
		super(message);
	}

	/**
	 * Constructor with parameter.
	 *
	 * @param throwable parameter, that added to stacktrace of exception.
	 */
	public LocaleByValueNotFoundException(final Throwable throwable) {
		super(throwable);
	}
}
