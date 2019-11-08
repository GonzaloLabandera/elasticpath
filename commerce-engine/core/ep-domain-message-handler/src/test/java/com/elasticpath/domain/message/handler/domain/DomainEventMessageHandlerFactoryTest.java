/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

import com.elasticpath.domain.message.handler.EventMessageHandler;

/**
 * Tests {@link DomainEventMessageHandlerFactory}.
 */
public class DomainEventMessageHandlerFactoryTest {
	private static final String EVENT_TYPE = "eventType";

	@Test
	public void shouldReturnCorrespondingHandlerForEventType() {
		final EventMessageHandler expectedHandler = mock(EventMessageHandler.class);
		final Map<String, EventMessageHandler> eventMessageHandlers = Collections.singletonMap(EVENT_TYPE, expectedHandler);

		DomainEventMessageHandlerFactory domainEventMessageHandlerFactory = new DomainEventMessageHandlerFactory(eventMessageHandlers);
		Optional<EventMessageHandler> optionHandler = domainEventMessageHandlerFactory.getHandler(EVENT_TYPE);

		assertThat(optionHandler).contains(expectedHandler);
	}

}
