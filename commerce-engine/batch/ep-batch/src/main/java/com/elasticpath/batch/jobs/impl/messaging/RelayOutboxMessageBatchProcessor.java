/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.messaging;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;

import com.elasticpath.batch.jobs.AbstractBatchProcessor;
import com.elasticpath.domain.messaging.OutboxMessage;

/**
 * The batch job processor for relaying outbox messages.
 */
public class RelayOutboxMessageBatchProcessor extends AbstractBatchProcessor<OutboxMessage> {
	@EndpointInject(context = "ep-batch-outbox-relay")
	private ProducerTemplate producer;

	@Override
	protected void executeBulkOperations(final List<OutboxMessage> batch) {
		batch.forEach(outboxMessage -> producer.sendBody(outboxMessage.getCamelUri(), outboxMessage.getMessageBody()));
		List<Long> uidpks = batch.stream()
				.map(OutboxMessage::getUidPk)
				.collect(Collectors.toList());
		getPersistenceEngine().executeNamedQueryWithList("DELETE_OUTBOXMESSAGES_BY_UIDS", LIST_PARAMETER_NAME, uidpks);
	}
}
