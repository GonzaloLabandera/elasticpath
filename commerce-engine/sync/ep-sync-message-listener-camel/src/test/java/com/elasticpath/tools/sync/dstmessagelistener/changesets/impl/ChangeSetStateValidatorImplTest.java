/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;

/**
 * Tests {@link ChangeSetStateValidatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeSetStateValidatorImplTest {

	@Mock
	private ChangeSet mockChangeSet;

	@InjectMocks
	private ChangeSetStateValidatorImpl changeSetStateValidator;

	@Test
	public void testValidStateIsValid() {
		final ChangeSetStateCode stateCode = ChangeSetStateCode.READY_TO_PUBLISH;

		given(mockChangeSet.getStateCode()).willReturn(stateCode);

		assertThat(changeSetStateValidator.validate(mockChangeSet, stateCode))
				.isTrue();
	}

	@Test
	public void testInvalidStateIsInvalid() {
		final ChangeSetStateCode validStateCode = ChangeSetStateCode.READY_TO_PUBLISH;
		final ChangeSetStateCode invalidStateCode = ChangeSetStateCode.FINALIZED;

		given(mockChangeSet.getStateCode()).willReturn(invalidStateCode);

		assertThat(changeSetStateValidator.validate(mockChangeSet, validStateCode))
				.isFalse();
	}

	@Test
	public void testAllAcceptableStatesAreExamined() {
		final ChangeSetStateCode validStateCode = ChangeSetStateCode.READY_TO_PUBLISH;
		final ChangeSetStateCode redHerringStateCode = ChangeSetStateCode.LOCKED;
		final ChangeSetStateCode[] additionalRedHerringStateCodes = new ChangeSetStateCode[]{
				ChangeSetStateCode.OPEN
		};

		given(mockChangeSet.getStateCode()).willReturn(redHerringStateCode);

		assertThat(changeSetStateValidator.validate(mockChangeSet,
				redHerringStateCode,
				ArrayUtils.add(additionalRedHerringStateCodes, validStateCode)))
			.isTrue();
	}

}
