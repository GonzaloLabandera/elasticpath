/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets;

import com.elasticpath.domain.changeset.ChangeSet;

/**
 * Loads Change Sets from the persistent data store.
 */
public interface ChangeSetLoader {

	/**
	 * Loads a Change Set from the persistent data store.
	 *
	 * @param changeSetGuid the GUID of the Change Set to load
	 * @return the Change Set
	 */
	ChangeSet load(String changeSetGuid);

}
