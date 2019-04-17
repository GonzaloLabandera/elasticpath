/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;

/**
 * Updates the Change Sets' states.
 */
public interface ChangeSetStateUpdater {

	/**
	 * Updates the state code of a Change Set.
	 *
	 * @param changeSet the Change Set to update
	 * @param newState the state to set
	 * @return the updated Change Set
	 */
	ChangeSet updateState(ChangeSet changeSet, ChangeSetStateCode newState);

}
