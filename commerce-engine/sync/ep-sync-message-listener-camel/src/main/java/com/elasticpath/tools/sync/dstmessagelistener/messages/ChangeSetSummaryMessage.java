/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages;

import java.util.Collection;

import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * A summary of a Change Set publishing attempt.
 */
public interface ChangeSetSummaryMessage {

	/**
	 * Returns true if publishing was successful.
	 *
	 * @return if successful or not
	 */
	boolean isSuccess();

	/**
	 * Returns the GUID of the Change Set that was published.
	 *
	 * @return the GUID of the Change Set that was published
	 */
	String getChangeSetGuid();

	/**
	 * Returns a human-readable description of the publish attempt.
	 *
	 * @return a human-readable description of the publish attempt
	 */
	String getPublishSummary();

	/**
	 * Returns details of successfully synchronised entities.
	 *
	 * @return details of successfully synchronised entities
	 */
	Collection<SyncResultItem> getSyncSuccessResults();

	/**
	 * Returns details of entities that failed to synchronise.
	 *
	 * @return details of entities that failed to synchronise
	 */
	Collection<SyncErrorResultItem> getSyncErrorResults();

	/**
	 * Returns list of success results merged with error results.
	 *
	 * @return list of SyncResultItem instances
	 */
	Collection<SyncResultItem> getAllResults();

}
