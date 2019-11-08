/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.message.handler.EventMessageHandler;
import com.elasticpath.domain.message.handler.EventMessageHandlerFactory;
import com.elasticpath.domain.message.handler.exception.EventMessageProcessingException;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;

/**
 * Tests {@link DomainEventMessageProcessor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DomainEventMessageProcessorTest {
	private static final String EVENT_NAME = "eventName";

	@Mock
	private EventMessageHandlerFactory eventMessageHandlerFactory;

	@InjectMocks
	private DomainEventMessageProcessor domainEventMessageProcessor;

	@Test
	public void shouldCallHandleMessageForEventMessage() {
		final EventMessageHandler eventMessageHandler = mock(EventMessageHandler.class);
		when(eventMessageHandlerFactory.getHandler(EVENT_NAME)).thenReturn(Optional.of(eventMessageHandler));

		final EventMessage eventMessage = createEventMessage(EVENT_NAME);
		final Exchange exchange = createExchangeWithBody(eventMessage);
		domainEventMessageProcessor.process(exchange);

		verify(eventMessageHandlerFactory).getHandler(EVENT_NAME);
	}

	@Test
	public void shouldCallGetHandlerForEventType() {
		final EventMessageHandler eventMessageHandler = mock(EventMessageHandler.class);
		when(eventMessageHandlerFactory.getHandler(EVENT_NAME)).thenReturn(Optional.of(eventMessageHandler));

		final EventMessage eventMessage = createEventMessage(EVENT_NAME);
		final Exchange exchange = createExchangeWithBody(eventMessage);
		domainEventMessageProcessor.process(exchange);

		verify(eventMessageHandler).handleMessage(eventMessage);
	}

	@Test
	public void shouldNotAddExceptionToExchangeInCaseEventMessageProcessingException() {
		final EventMessageHandler eventMessageHandler = mock(EventMessageHandler.class);
		final EventMessage eventMessage = createEventMessage(EVENT_NAME);
		final EventMessageProcessingException exception = new EventMessageProcessingException(StringUtils.EMPTY);
		final Exchange exchange = createExchangeWithBody(eventMessage);

		when(eventMessageHandlerFactory.getHandler(EVENT_NAME)).thenReturn(Optional.of(eventMessageHandler));
		doThrow(exception).when(eventMessageHandler).handleMessage(eventMessage);

		domainEventMessageProcessor.process(exchange);

		verify(exchange, never()).setException(exception);
		verify(exchange, never()).setException(any());
	}

	private Exchange createExchangeWithBody(final Object body) {
		final Message message = mock(Message.class);
		final Exchange exchange = mock(Exchange.class);

		when(exchange.getIn()).thenReturn(message);
		when(message.getBody()).thenReturn(body);

		return exchange;
	}

	private EventMessage createEventMessage(final String eventName) {
		final EventType eventType = mock(EventType.class);
		when(eventType.getName()).thenReturn(eventName);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getEventType()).thenReturn(eventType);

		return eventMessage;
	}

}