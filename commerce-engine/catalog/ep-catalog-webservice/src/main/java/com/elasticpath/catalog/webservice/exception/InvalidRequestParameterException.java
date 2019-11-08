/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.exception;

/**
 * This exception will be thrown when parameters of request is invalid.
 */
public class InvalidRequestParameterException extends RuntimeException {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Default constructor.
	 */
	public InvalidRequestParameterException() {
		super();
	}

	/**
	 * Constructor for InvalidRequestParameterException.
	 *
	 * @param message - exception message.
	 * @param throwable parameter, that added to stacktrace of exception.
	 */
	public InvalidRequestParameterException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Constructor for InvalidRequestParameterException.
	 *
	 * @param message - exception message.
	 */
	public InvalidRequestParameterException(final String message) {
		super(message);
	}
}
