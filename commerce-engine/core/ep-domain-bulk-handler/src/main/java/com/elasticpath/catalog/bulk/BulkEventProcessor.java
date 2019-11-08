/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExpectedBodyTypeException;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.elasticpath.messaging.EventMessage;

/**
 * An implementation of Camel Processor for bulk events processing.
 */
public class BulkEventProcessor implements Processor {

	private final Map<String, BulkEventHandler> bulkEventHandlers;

	/**
	 * Constructor.
	 *
	 * @param bulkEventHandlers a map of {@link BulkEventHandler} for processing bulk events.
	 */
	public BulkEventProcessor(final Map<String, BulkEventHandler> bulkEventHandlers) {
		this.bulkEventHandlers = bulkEventHandlers;
	}

	@Override
	public void process(final Exchange exchange) {
		final EventMessage eventMessage = getEventMessage(exchange);

		final String eventType = eventMessage.getEventType().getName();

		bulkEventHandlers
				.computeIfAbsent(eventType, key -> { throw new IllegalArgumentException("Bulk event handler for " + eventType + " not found"); })
				.handleBulkEvent(eventMessage);
	}

	private EventMessage getEventMessage(final Exchange exchange) {
		final Message message = exchange.getIn();
		final Object body = message.getBody();

		if (!(body instanceof EventMessage)) {
			throw new ExpectedBodyTypeException(exchange, EventMessage.class);
		}

		return (EventMessage) body;
	}

}
