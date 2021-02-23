/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.batch.jobs.impl.messaging;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.domain.messaging.OutboxMessage;

/**
 * Job for retrieving messages from Camel Messages Outbox and sending to final destination using Camel.
 */
public class RelayOutboxMessagesJob extends AbstractBatchJob<OutboxMessage> {
	@Override
	protected String getJobName() {
		return "Relay Outbox Messages";
	}

	@Override
	protected String getBatchJPQLQuery() {
		return "SELECT_ALL_OUTBOXMESSAGE";
	}
}
