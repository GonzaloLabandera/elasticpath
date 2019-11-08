/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.message;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExpectedBodyTypeException;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.springframework.batch.core.JobExecution;

import com.elasticpath.catalog.batch.CatalogJobRunner;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;

/**
 * Implementation of {@link Processor} for Catalog Batch {@link EventMessage} processing.
 */
public class CatalogBatchEventMessageProcessor implements Processor {

	private final Map<String, CatalogJobRunner> jobRunners;

	/**
	 * Constructor.
	 *
	 * @param jobRunners map of Catalog Batch job runners.
	 */
	public CatalogBatchEventMessageProcessor(final Map<String, CatalogJobRunner> jobRunners) {
		this.jobRunners = jobRunners;
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final EventMessage eventMessage = getEventMessage(exchange);
		final EventType eventType = eventMessage.getEventType();

		final JobExecution execution = jobRunners.get(eventType.getName()).run(eventMessage.getGuid(), eventMessage.getData());
		exchange.setProperty("execution", execution);
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
