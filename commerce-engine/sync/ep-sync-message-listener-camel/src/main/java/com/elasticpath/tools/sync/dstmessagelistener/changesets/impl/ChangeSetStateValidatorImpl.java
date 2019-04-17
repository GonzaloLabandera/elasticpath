/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets.impl;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateValidator;

/**
 * Validator that tests a Change Set against a number of acceptable states.
 */
public class ChangeSetStateValidatorImpl implements ChangeSetStateValidator {

	@Override
	public boolean validate(final ChangeSet changeSet,
						 final ChangeSetStateCode validStateCode, final ChangeSetStateCode... additionalValidStateCodes) {
		final ChangeSetStateCode[] acceptableChangeSetStates = ArrayUtils.add(additionalValidStateCodes, validStateCode);

		return Arrays.stream(acceptableChangeSetStates)
				.anyMatch(acceptableState -> changeSet.getStateCode().equals(acceptableState));
	}

}
