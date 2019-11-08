/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.exception;

/**
 * Exception that throws if projection can not be created for the given event.
 */
public class EventMessageProcessingException extends RuntimeException {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Event message processing exception.
	 *
	 * @param message processing exception message.
	 */
	public EventMessageProcessingException(final String message) {
		super(message);
	}
}
