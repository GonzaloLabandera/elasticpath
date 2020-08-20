/*
 * Copyright (c) Elastic Path Software Inc., 2005
 */
package com.elasticpath.commons.exception;

import com.elasticpath.base.exception.EpSystemException;



/**
 * The exception for shared Id doesn't exist in the database.
 */
public class SharedIdNonExistException extends EpSystemException {
	
	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;
	
	/**
	 * Creates a new <code>SharedIdNonExistException</code> object with the given message.
	 *
	 * @param message the message 
	 */
	public SharedIdNonExistException(final String message) {
		super(message);
	}

	/**
	 * Creates a new <code>SharedIdNonExistException</code> object using the given message and cause exception.
	 *
	 * @param message the mesage
	 * @param cause the cause exception
	 */
	public SharedIdNonExistException(final String message, final Throwable cause) {
		super(message, cause);
	}
}