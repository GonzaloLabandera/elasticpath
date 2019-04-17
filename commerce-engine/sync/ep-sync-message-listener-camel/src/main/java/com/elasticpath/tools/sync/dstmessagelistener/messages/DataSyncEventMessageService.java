/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages;

/**
 * Service responsible for publishing Event Messages describing the result of a Change Set publishing attempt.
 */
public interface DataSyncEventMessageService {

	/**
	 * Prepares a builder for construction and sending of an Event Message that signals a successful or failed Change Set publish attempt.
	 *
	 * @param <T> Builder type
	 * @return the builder
	 */
	<T extends DataSyncEventMessageBuilder<T>> DataSyncEventMessageBuilder<T> prepareMessage();
}
