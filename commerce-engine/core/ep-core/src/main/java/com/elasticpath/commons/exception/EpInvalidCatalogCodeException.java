/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.exception;

import java.util.List;

import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;

/**
 * This exception will be thrown when an invalid Catalog Code is given.
 */
public class EpInvalidCatalogCodeException extends EpBindException {

	/** Serial version id. */
	private static final long serialVersionUID = 8369987654176536799L;

	private final List<InvalidCatalogCodeMessage> messages;

	/**
	 * Creates a new object.
	 *
	 * @param msg the message
	 * @param messages a List of <code>InvalidCatalogCodeMessage</code> with messages by each invalid case.
	 */
	public EpInvalidCatalogCodeException(final String msg, final List<InvalidCatalogCodeMessage> messages) {
		super(msg);
		this.messages = messages;
	}

	/**
	 * Creates a new object.
	 *
	 * @param msg the message
	 * @param messages a List of <code>InvalidCatalogCodeMessage</code> with messages by each invalid case.
	 * @param cause the root cause
	 */
	public EpInvalidCatalogCodeException(final String msg, final List<InvalidCatalogCodeMessage> messages, final Throwable cause) {
		super(msg, cause);
		this.messages = messages;
	}

	/**
	 * @return a List of <code>InvalidCatalogCodeMessage</code> with messages by each invalid case.
	 */
	public List<InvalidCatalogCodeMessage> getErrorReasonList() {
		return messages;
	}

}
