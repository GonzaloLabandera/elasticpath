/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.commons.exception;

/**
 * This exception will be thrown when there problems with JMS connection when EventMessage is publishing.
 */
public class EventMessagePublishingException extends RuntimeException {

	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Message for this exception.
	 */
	public static final String JMS_SERVER_IS_UNAVAILABLE_MESSAGE = "JMS Server is unavailable. Please contact support if the problem persists.";

	/**
	 * Creates a new EventMessagePublishingException object with the given message.
	 *
	 * @param message message the reason for this EventMessagePublishingException.
	 */
	public EventMessagePublishingException(final String message) {
		super(message);
	}
}
