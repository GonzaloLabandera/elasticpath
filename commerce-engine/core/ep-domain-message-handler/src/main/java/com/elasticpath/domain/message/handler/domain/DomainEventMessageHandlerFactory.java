/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.domain;

import java.util.Map;
import java.util.Optional;

import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerFactory;

/**
 * Implementation of {@link EventMessageHandlerFactory} to create domain event message handlers.
 */
public class DomainEventMessageHandlerFactory implements EventMessageHandlerFactory {

	private final Map<String, EventMessageHandler> eventMessageHandlers;

	/**
	 * Constructor.
	 *
	 * @param eventMessageHandlers map of {@link EventMessageHandler} for processing domain event messages
	 */
	public DomainEventMessageHandlerFactory(final Map<String, EventMessageHandler> eventMessageHandlers) {
		this.eventMessageHandlers = eventMessageHandlers;
	}

	@Override
	public Optional<EventMessageHandler> getHandler(final String eventType) {
		return Optional.ofNullable(eventMessageHandlers.get(eventType));
	}

}
