/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.exception;

/**
 * This exception in case not supported schema of jar URI.
 */
public class SchemaNotSupportedException extends RuntimeException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Constructor.
	 *
	 * @param message the message
	 */
	public SchemaNotSupportedException(final String message) {
		super(message);
	}
}
