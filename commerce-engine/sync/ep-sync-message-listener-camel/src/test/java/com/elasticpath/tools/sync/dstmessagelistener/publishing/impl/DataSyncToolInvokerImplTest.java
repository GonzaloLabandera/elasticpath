/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.tools.sync.client.SyncJobConfiguration;
import com.elasticpath.tools.sync.client.SyncToolLauncher;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.SyncJobConfigurationFactory;
import com.elasticpath.tools.sync.target.result.Summary;

/**
 * Unit tests for {@link DataSyncToolInvokerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSyncToolInvokerImplTest {

	private static final String CHANGESET_GUID = "TEST_CHANGESET_GUID";

	@Mock
	private SyncToolLauncher syncToolLauncher;

	@Mock
	private SyncJobConfigurationFactory syncJobConfigurationFactory;

	@Mock
	private SyncJobConfiguration syncJobConfiguration;

	@Mock
	private ChangeSet changeSet;

	@InjectMocks
	private DataSyncToolInvokerImpl publisher;

	@Before
	public void setUp() throws Exception {
		when(changeSet.getGuid()).thenReturn(CHANGESET_GUID);
	}

	@Test
	public void verifyInvokerCreatesJobConfigurationAndInvokes() throws Exception {
		final Summary expectedSummary = mock(Summary.class);

		given(syncJobConfigurationFactory.createSyncJobConfiguration(CHANGESET_GUID))
				.willReturn(syncJobConfiguration);

		given(syncToolLauncher.processJob(syncJobConfiguration))
				.willReturn(expectedSummary);

		final Summary actualSummary = publisher.processSyncToolJob(changeSet);

		assertThat(actualSummary)
				.isSameAs(expectedSummary);
	}

}
