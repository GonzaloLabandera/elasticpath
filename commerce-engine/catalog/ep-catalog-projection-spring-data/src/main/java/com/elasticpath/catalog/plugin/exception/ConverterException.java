/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.exception;

/**
 * Exception that throws if projection json is not valid.
 */
public class ConverterException extends RuntimeException {
	/**
	 * Converter exception message.
	 */
	public static final String CONVERTER_EXCEPTION_MESSAGE = "Projection cannot be converted to entity, projection= ";
	/**
	 * No converter exception message.
	 */
	public static final String NO_CONVERTER = "No suitable converter has been found for projection entity of type = ";

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Constructor.
	 */
	public ConverterException() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param message - error message of exception.
	 */
	public ConverterException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param cause is cause of exception.
	 */
	public ConverterException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with parameter.
	 *
	 * @param message   - error message of exception.
	 * @param throwable parameter, that added to stacktrace of exception.
	 */
	public ConverterException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
