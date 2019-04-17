/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.tools.sync.exception.ChangeSetNotFoundException;

/**
 * Test class for {@link ChangeSetLoaderImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ChangeSetLoaderImplTest {

	private static final String CHANGE_SET_GUID = UUID.randomUUID().toString();

	@Mock
	private ChangeSetManagementService changeSetManagementService;

	@Mock
	private LoadTuner loadTuner;

	@InjectMocks
	private ChangeSetLoaderImpl loader;

	@Test
	public void verifyChangeSetNotFoundExceptionThrownWhenNoChangeSetFound() {
		given(changeSetManagementService.get(CHANGE_SET_GUID, loadTuner)).willReturn(null);

		assertThatThrownBy(() -> loader.load(CHANGE_SET_GUID))
				.isInstanceOf(ChangeSetNotFoundException.class);
	}

	@Test
	public void verifyChangeSetReturnedWhenFound() {
		final ChangeSet expectedChangeSet = mock(ChangeSet.class);
		given(changeSetManagementService.get(CHANGE_SET_GUID, loadTuner))
				.willReturn(expectedChangeSet);

		final ChangeSet actualChangeSet = loader.load(CHANGE_SET_GUID);

		assertThat(actualChangeSet)
				.isSameAs(expectedChangeSet);
	}

}