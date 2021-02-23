/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderhold.messaging;

import com.elasticpath.base.exception.EpServiceException;

/**
 * Defining an exception during unable to lock order.
 */
public class UnableToLockOrderException extends EpServiceException {

	private static final long serialVersionUID = 50000000001L;

	/**
	 * Constructor.
	 *
	 * @param message the error message.
	 */
	public UnableToLockOrderException(final String message) {
		super(message);
	}

	/**
	 * Constructor that allows wrapping an original exception.
	 *
	 * @param message the error message.
	 * @param throwable the originating exception
	 */
	public UnableToLockOrderException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
