/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages;

import java.util.Collection;
import java.util.Map;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * A builder for construction and sending of an Event Message that signals a successful or failed Change Set publish attempt.
 *
 * @param <T> Builder type
 */
public interface DataSyncEventMessageBuilder<T extends DataSyncEventMessageBuilder<?>> {


	/**
	 * build the configured Event Message.
	 *
	 * @return the event message
	 */
	EventMessage build();


	/**
	 * Specifies the Change Set GUID.
	 *
	 * @param changeSetGuid the GUID of the change set
	 * @return the builder
	 */
	T withChangeSetGuid(String changeSetGuid);

	/**
	 * Specifies the Change Set name.
	 *
	 * @param changeSetName the name of the change set
	 * @return the builder
	 */
	T withChangeSetName(String changeSetName);

	/**
	 * Specifies the Change Set creator data.
	 *
	 * @param changeSetCreatorData a map of data representing the creator of the change set
	 * @return the builder
	 */
	T withChangeSetCreatorData(Map<String, String> changeSetCreatorData);

	/**
	 * Specifies the Change Set publish initiator data.
	 *
	 * @param changeSetPublishInitiatorData a map of data representing the initiator of the change set publish request
	 * @return the builder
	 */
	T withChangeSetPublishInitiator(Map<String, String> changeSetPublishInitiatorData);

	/**
	 * Indicates the status of a Change Set publish attempt.
	 *
	 * @param success whether or not the Change Set publish attempt was successful
	 * @return the builder
	 */
	T withSuccess(Boolean success);

	/**
	 * Specifies the summary of a Change Set publish attempt.
	 *
	 * @param publishSummary the summary of a Change Set publish attempt
	 * @return the builder
	 */
	T withPublishSummary(String publishSummary);

	/**
	 * Specifies the success results of a Change Set publish attempt.
	 *
	 * @param syncSuccessResults the success results of a Change Set publish attempt
	 * @return the builder
	 */
	T withSyncSuccessResults(Collection<SyncResultItem> syncSuccessResults);

	/**
	 * Specifies the error results of a Change Set publish attempt.
	 *
	 * @param syncErrorResults the error results of a Change Set publish attempt
	 * @return the builder
	 */
	T withSyncErrorResults(Collection<SyncErrorResultItem> syncErrorResults);

}