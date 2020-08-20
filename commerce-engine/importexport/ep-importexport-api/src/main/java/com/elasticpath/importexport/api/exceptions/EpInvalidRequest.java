/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.exceptions;

/**
 * Exception to throw when an invalid API request is received.
 */
public class EpInvalidRequest extends RuntimeException {

	private static final long serialVersionUID = 7425314495320243269L;

	/**
	 * Constructor.
	 * @param message the detail message
	 */
	public EpInvalidRequest(final String message) {
		super(message);
	}
}
