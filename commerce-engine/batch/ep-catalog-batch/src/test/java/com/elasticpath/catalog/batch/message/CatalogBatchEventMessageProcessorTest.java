/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.message;

import static com.elasticpath.catalog.batch.CatalogJobRunnerImpl.CLEAN_UP_DATABASE_FLAG;
import static com.elasticpath.catalog.batch.message.CatalogBatchEventType.START_JOB;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.batch.CatalogJobRunner;
import com.elasticpath.messaging.EventMessage;

/**
 * Tests for {@link CatalogBatchEventMessageProcessor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogBatchEventMessageProcessorTest {

	private static final String GUID = "guid";

	@Test
	public void shouldCallJobRunnerRunWithGuidAndCleanUpDatabaseWhenCallProcessForExchange() throws Exception {
		final String cleanUpDatabase = "false";
		final CatalogJobRunner catalogJobRunner = mock(CatalogJobRunner.class);

		final EventMessage eventMessage = mock(EventMessage.class);
		when(eventMessage.getEventType()).thenReturn(START_JOB);
		when(eventMessage.getGuid()).thenReturn(GUID);
		when(eventMessage.getData()).thenReturn(Collections.singletonMap("cleanUpDatabase", cleanUpDatabase));

		final Message message = mock(Message.class);
		when(message.getBody()).thenReturn(eventMessage);

		final Exchange exchange = mock(Exchange.class);
		when(exchange.getIn()).thenReturn(message);

		final CatalogBatchEventMessageProcessor catalogBatchEventMessageProcessor =
				new CatalogBatchEventMessageProcessor(Collections.singletonMap(START_JOB.getName(), catalogJobRunner));

		catalogBatchEventMessageProcessor.process(exchange);

		verify(catalogJobRunner).run(GUID, Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, cleanUpDatabase));
	}

}