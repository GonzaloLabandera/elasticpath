/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.tools.sync.target.result.Summary;

/**
 * Invokes the Data Sync Tool to synchronise Change Sets.
 */
public interface DataSyncToolInvoker {

	/**
	 * Executes the synchronization process for a given Change Set.
	 *
	 * @param changeSet the Change Set to synchronise
	 * @return a summary of the synchronisation attempt
	 */
	Summary processSyncToolJob(ChangeSet changeSet);

}
