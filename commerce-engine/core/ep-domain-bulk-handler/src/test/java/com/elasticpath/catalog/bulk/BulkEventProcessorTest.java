/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;

/**
 * Tests {@link BulkEventProcessor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BulkEventProcessorTest {

	private static final String BRAND_BULK_UPDATE = "BRAND_BULK_UPDATE";

	/**
	 * Method BulkEventHandler#handleBulkEvent should be called while an exchange is processing by BulkEventProcessor.
	 */
	@Test
	public void testShouldCallHandleBulkEventWithEventMessageFromExchange() {
		final EventType eventType = mock(EventType.class);
		when(eventType.getName()).thenReturn(BRAND_BULK_UPDATE);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getEventType()).thenReturn(eventType);

		final Message message = mock(Message.class);
		when(message.getBody()).thenReturn(eventMessage);

		final Exchange exchange = mock(Exchange.class);
		when(exchange.getIn()).thenReturn(message);

		final BulkEventHandler bulkEventHandler = mock(BulkEventHandler.class);
		final Map<String, BulkEventHandler> bulkEventHandlers = new HashMap<>();
		bulkEventHandlers.put(BRAND_BULK_UPDATE, bulkEventHandler);
		final BulkEventProcessor bulkEventProcessor = new BulkEventProcessor(bulkEventHandlers);

		bulkEventProcessor.process(exchange);

		verify(bulkEventHandler).handleBulkEvent(eventMessage);
	}

	/**
	 * Method BulkEventProcessor#process should throws IllegalArgumentException if BulkEventHandler is absent for EventType.
	 */
	@Test
	public void testShouldThrowIllegalArgumentExceptionWhenBulkEventHandlerIsAbsentForEventTypeBrandBulkUpdate() {
		final EventType eventType = mock(EventType.class);
		when(eventType.getName()).thenReturn(BRAND_BULK_UPDATE);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getEventType()).thenReturn(eventType);

		final Message message = mock(Message.class);
		when(message.getBody()).thenReturn(eventMessage);

		final Exchange exchange = mock(Exchange.class);
		when(exchange.getIn()).thenReturn(message);

		final Map<String, BulkEventHandler> bulkEventHandlers = new HashMap<>();
		final BulkEventProcessor bulkEventProcessor = new BulkEventProcessor(bulkEventHandlers);

		assertThatThrownBy(() -> bulkEventProcessor.process(exchange)).isInstanceOf(IllegalArgumentException.class);
	}

}