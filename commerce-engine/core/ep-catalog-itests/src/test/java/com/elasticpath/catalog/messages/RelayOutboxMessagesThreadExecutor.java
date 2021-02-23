/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.catalog.messages;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.elasticpath.batch.jobs.impl.messaging.RelayOutboxMessagesJob;

/**
 * Creates a scheduled thread pool to run the send camel messages job every second, simulating what Quartz does within Batch server.
 */
public class RelayOutboxMessagesThreadExecutor {
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private RelayOutboxMessagesJob relayOutboxMessagesJob;

	/**
	 * Start the executor service.
	 */
	public void start() {
		scheduler.scheduleWithFixedDelay(() -> relayOutboxMessagesJob.execute(), 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Stop the executor service.
	 */
	public void stop() {
		scheduler.shutdown();
	}

	public void setRelayOutboxMessagesJob(final RelayOutboxMessagesJob relayOutboxMessagesJob) {
		this.relayOutboxMessagesJob = relayOutboxMessagesJob;
	}
}
