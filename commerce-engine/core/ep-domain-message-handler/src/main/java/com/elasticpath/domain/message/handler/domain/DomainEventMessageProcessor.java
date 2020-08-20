/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.domain;

import java.util.Optional;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ExpectedBodyTypeException;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerFactory;
import com.elasticpath.domain.message.handler.exception.EventMessageProcessingException;
import com.elasticpath.messaging.EventMessage;

/**
 * Implementation of {@link Processor} for {@link EventMessage} processing.
 */
public class DomainEventMessageProcessor implements Processor {

	private static final Logger LOGGER = Logger.getLogger(DomainEventMessageProcessor.class);

	private final EventMessageHandlerFactory eventMessageHandlerFactory;

	/**
	 * Constructor.
	 *
	 * @param eventMessageHandlerFactory {@link EventMessageHandlerFactory} to produce {@link EventMessageHandler}.
	 */
	public DomainEventMessageProcessor(final EventMessageHandlerFactory eventMessageHandlerFactory) {
		this.eventMessageHandlerFactory = eventMessageHandlerFactory;
	}

	@Override
	public void process(final Exchange exchange) {
		try {
			final EventMessage eventMessage = getEventMessage(exchange);
			final String eventType = eventMessage.getEventType().getName();

			final Optional<EventMessageHandler> optionalHandler = eventMessageHandlerFactory.getHandler(eventType);

			optionalHandler.orElseThrow(() -> new CamelExecutionException("Handler doesn't exist for event: " + eventType, exchange))
					.handleMessage(eventMessage);
		} catch (final EventMessageProcessingException e) {
			LOGGER.error("Event message processing exception.", e);
		} catch (final Exception e) {
			LOGGER.error("Event message processing exception.", e);
			exchange.setException(e);
		}
	}

	/**
	 * Extract {@link EventMessage} from {@link Exchange}.
	 *
	 * @param exchange Apache Camel message.
	 * @return event message extracted from exchange.
	 */
	private EventMessage getEventMessage(final Exchange exchange) {
		final Message message = exchange.getIn();
		final Object body = message.getBody();

		if (!(body instanceof EventMessage)) {
			throw new ExpectedBodyTypeException(exchange, EventMessage.class);
		}

		return (EventMessage) body;
	}
}