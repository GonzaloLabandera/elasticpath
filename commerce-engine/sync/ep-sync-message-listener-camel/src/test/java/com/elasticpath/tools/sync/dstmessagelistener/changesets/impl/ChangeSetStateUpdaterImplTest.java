/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;

/**
 * Test class for {@link ChangeSetStateUpdaterImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeSetStateUpdaterImplTest {

	@Mock
	private ChangeSetManagementService changeSetManagementService;

	@Mock
	private LoadTuner loadTuner;

	@InjectMocks
	private ChangeSetStateUpdaterImpl changeSetStateUpdater;

	@Test
	public void verifyDelegatesToChangeSetManagementService() {
		final ChangeSet changeSet = mock(ChangeSet.class);
		final String changeSetGuid = UUID.randomUUID().toString();

		given(changeSet.getGuid())
				.willReturn(changeSetGuid);

		final ChangeSet expectedUpdatedChangeSet = mock(ChangeSet.class);

		final ChangeSetStateCode newState = ChangeSetStateCode.FINALIZED;

		given(changeSetManagementService.updateState(changeSetGuid, newState, loadTuner))
				.willReturn(expectedUpdatedChangeSet);

		final ChangeSet actualUpdatedChangeSet = changeSetStateUpdater.updateState(changeSet, newState);

		assertThat(actualUpdatedChangeSet)
				.isSameAs(expectedUpdatedChangeSet);
	}

}