/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;

/**
 * Validates Change Set states.
 */
public interface ChangeSetStateValidator {

	/**
	 * Verifies that the current ChangeSet state is valid.
	 *
	 * @param changeSet the ChangeSet to validate
	 * @param validStateCode a state code considered valid
	 * @param additionalValidStateCodes state codes considered valid
	 * @return true if the change set state matches a state considered valid
	 */
	boolean validate(ChangeSet changeSet, ChangeSetStateCode validStateCode, ChangeSetStateCode... additionalValidStateCodes);

}