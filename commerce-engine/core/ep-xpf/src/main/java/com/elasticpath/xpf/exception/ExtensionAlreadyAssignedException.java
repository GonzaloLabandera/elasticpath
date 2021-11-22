/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.exception;

/**
 * If extension has already been assigned.
 */
public class ExtensionAlreadyAssignedException extends RuntimeException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Extension have already assigned exception.
	 *
	 * @param cause {@link Throwable}.
	 */
	public ExtensionAlreadyAssignedException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Extension have already assigned exception.
	 *
	 * @param message validation exception message.
	 */
	public ExtensionAlreadyAssignedException(final String message) {
		super(message);
	}
}
