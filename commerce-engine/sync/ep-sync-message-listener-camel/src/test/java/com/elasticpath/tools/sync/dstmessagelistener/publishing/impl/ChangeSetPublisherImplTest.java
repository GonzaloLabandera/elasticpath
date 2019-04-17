/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateUpdater;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateValidator;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.DataSyncToolInvoker;
import com.elasticpath.tools.sync.exception.ChangeSetNotFoundException;
import com.elasticpath.tools.sync.target.result.Summary;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;

/**
 * Test class for {@link ChangeSetPublisherImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ChangeSetPublisherImplTest {

	@Mock
	private ChangeSetLoader changeSetLoader;

	@Mock
	private ChangeSetStateValidator changeSetStateValidator;

	@Mock
	private DataSyncToolInvoker dataSyncToolInvoker;

	@Mock
	private ChangeSetStateUpdater changeSetStateUpdater;

	@Mock
	private ChangeSet changeSet;

	@InjectMocks
	private ChangeSetPublisherImpl publisher;

	@Mock
	private SyncErrorResultItem syncErrorResultItemOne;

	@Mock
	private SyncErrorResultItem syncErrorResultItemTwo;

	@Rule
	public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

	private final Consumer<ChangeSetSummaryMessage> summaryMessagePopulatedAssertions = summaryMessage -> {
		softly.assertThat(summaryMessage.getChangeSetGuid()).isEqualTo(ChangeSetUserConstants.CHANGE_SET_GUID);
	};

	@Before
	public void setUp() throws Exception {
		syncErrorResultItemOne = mock(SyncErrorResultItem.class);
		syncErrorResultItemTwo = mock(SyncErrorResultItem.class);
		final Class jobEntryImplClass = Class.forName("com.elasticpath.tools.sync.job.impl.JobEntryImpl");
		when(syncErrorResultItemOne.getJobEntryType()).thenReturn(jobEntryImplClass);
		when(syncErrorResultItemTwo.getJobEntryType()).thenReturn(jobEntryImplClass);
		when(changeSet.getGuid()).thenReturn(ChangeSetUserConstants.CHANGE_SET_GUID);
		when(changeSet.getStateCode()).thenReturn(ChangeSetStateCode.OPEN);
	}

	@Test
	public void verifyExceptionThrownWhenNoSuchChangeSet() {
		doThrow(ChangeSetNotFoundException.class).when(changeSetLoader).load(ChangeSetUserConstants.CHANGE_SET_GUID);

		assertThatThrownBy(() -> publisher.publish(ChangeSetUserConstants.CHANGE_SET_GUID))
				.isInstanceOf(ChangeSetNotFoundException.class);
	}

	@Test
	public void verifySummaryHasFailedStatusWhenInvalidChangeSetState() {

		given(changeSet.getStateCode()).willReturn(ChangeSetStateCode.OPEN);
		given(changeSetLoader.load(ChangeSetUserConstants.CHANGE_SET_GUID)).willReturn(changeSet);
		given(changeSetStateValidator.validate(changeSet, ChangeSetStateCode.READY_TO_PUBLISH, ChangeSetStateCode.FINALIZED,
				ChangeSetStateCode.LOCKED)).willReturn(false);

		final ChangeSetSummaryMessage summaryMessage = publisher.publish(ChangeSetUserConstants.CHANGE_SET_GUID);

		assertThat(summaryMessage.isSuccess())
				.as("Publish must fail when change set state is incompatible.")
				.isFalse();

		assertThat(summaryMessage.getPublishSummary())
				.isEqualTo("ChangeSet with GUID [" + ChangeSetUserConstants.CHANGE_SET_GUID + "] "
						+ "current state [OPEN] does not match the acceptable state [READY_TO_PUBLISH]");
	}

	@Test
	public void verifySuccessfulPublishReturnsSummary() {
		final List<SyncResultItem> syncResults = ImmutableList.of(syncErrorResultItemOne, syncErrorResultItemTwo);
		final Summary successfulSummary = createSuccessfulSummary(syncResults);

		given(changeSetLoader.load(ChangeSetUserConstants.CHANGE_SET_GUID)).willReturn(changeSet);
		given(changeSetStateValidator.validate(changeSet, ChangeSetStateCode.READY_TO_PUBLISH, ChangeSetStateCode.FINALIZED,
				ChangeSetStateCode.LOCKED)).willReturn(true);
		given(dataSyncToolInvoker.processSyncToolJob(changeSet)).willReturn(successfulSummary);

		final ChangeSetSummaryMessage summaryMessage = publisher.publish(ChangeSetUserConstants.CHANGE_SET_GUID);

		assertThat(summaryMessage).satisfies(summaryMessagePopulatedAssertions);
		assertThat(summaryMessage.getSyncSuccessResults())
				.hasSameElementsAs(syncResults);
	}

	@Test
	public void verifySuccessfulPublishFinalisesChangeSet() {
		final Summary successfulSummary = createSuccessfulSummary(Collections.emptyList());

		given(changeSetLoader.load(ChangeSetUserConstants.CHANGE_SET_GUID)).willReturn(changeSet);
		given(changeSetStateValidator.validate(changeSet, ChangeSetStateCode.READY_TO_PUBLISH, ChangeSetStateCode.FINALIZED,
				ChangeSetStateCode.LOCKED)).willReturn(true);
		when(dataSyncToolInvoker.processSyncToolJob(changeSet)).thenReturn(successfulSummary);

		publisher.publish(ChangeSetUserConstants.CHANGE_SET_GUID);

		verify(changeSetStateUpdater).updateState(changeSet, ChangeSetStateCode.FINALIZED);
	}

	@Test
	public void verifyUnsuccessfulPublishReturnsSummary() {
		final List<SyncErrorResultItem> syncErrors = ImmutableList.of(syncErrorResultItemOne, syncErrorResultItemTwo);
		final Summary unsuccessfulSummary = createUnsuccessfulSummary(syncErrors);

		given(changeSetLoader.load(ChangeSetUserConstants.CHANGE_SET_GUID)).willReturn(changeSet);
		given(changeSetStateValidator.validate(changeSet, ChangeSetStateCode.READY_TO_PUBLISH, ChangeSetStateCode.FINALIZED,
				ChangeSetStateCode.LOCKED)).willReturn(true);
		given(dataSyncToolInvoker.processSyncToolJob(changeSet)).willReturn(unsuccessfulSummary);

		final ChangeSetSummaryMessage summaryMessage = publisher.publish(ChangeSetUserConstants.CHANGE_SET_GUID);

		assertThat(summaryMessage).satisfies(summaryMessagePopulatedAssertions);
		assertThat(summaryMessage.getSyncErrorResults())
				.hasSameElementsAs(syncErrors);
	}

	@Test
	public void verifyUnsuccessfulPublishDoesNotFinaliseChangeSet() {
		final Summary unsuccessfulSummary = createUnsuccessfulSummary(ImmutableList.of(syncErrorResultItemOne));

		given(changeSetLoader.load(ChangeSetUserConstants.CHANGE_SET_GUID)).willReturn(changeSet);
		given(changeSetStateValidator.validate(changeSet, ChangeSetStateCode.READY_TO_PUBLISH, ChangeSetStateCode.FINALIZED,
				ChangeSetStateCode.LOCKED)).willReturn(true);
		given(dataSyncToolInvoker.processSyncToolJob(changeSet)).willReturn(unsuccessfulSummary);

		publisher.publish(ChangeSetUserConstants.CHANGE_SET_GUID);

		verify(changeSetStateUpdater, never()).updateState(changeSet, ChangeSetStateCode.FINALIZED);
	}

	private Summary createSuccessfulSummary(final List<SyncResultItem> syncResultItems) {
		final Summary summary = mock(Summary.class);

		when(summary.getSuccessResults()).thenReturn(syncResultItems);

		return summary;
	}

	private Summary createUnsuccessfulSummary(final List<SyncErrorResultItem> syncResultItems) {
		final Summary summary = mock(Summary.class);

		when(summary.hasErrors()).thenReturn(true);
		when(summary.getSyncErrors()).thenReturn(syncResultItems);

		return summary;
	}

}