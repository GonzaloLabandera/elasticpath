/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.messages.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.impl.ChangeSetImpl;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;
import com.elasticpath.tools.sync.target.result.Summary;
import com.elasticpath.tools.sync.target.result.SyncErrorResultItem;
import com.elasticpath.tools.sync.target.result.SyncResultItem;
import com.elasticpath.tools.sync.target.result.impl.SummaryImpl;

/**
 * Test class for {@link ChangeSetSummaryMessageImpl}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ChangeSetSummaryMessageImplTest {

	private final ChangeSet changeSet = new ChangeSetImpl();
	private final Summary summary = new SummaryImpl();
	private SyncErrorResultItem syncErrorResultItemOne;
	private SyncErrorResultItem syncErrorResultItemTwo;

	@Before
	public void setUp() throws Exception {
		syncErrorResultItemOne = mock(SyncErrorResultItem.class);
		syncErrorResultItemTwo = mock(SyncErrorResultItem.class);
		final Class jobEntryImplClass = Class.forName("com.elasticpath.tools.sync.job.impl.JobEntryImpl");
		when(syncErrorResultItemOne.getJobEntryType()).thenReturn(jobEntryImplClass);
		when(syncErrorResultItemTwo.getJobEntryType()).thenReturn(jobEntryImplClass);
	}

	@Test
	public void verifySuccessIsFalseWhenErrorMessageProvided() {
		assertThat(new ChangeSetSummaryMessageImpl(changeSet, "It went boom").isSuccess())
				.as("Success must be false when an error message is provided.")
				.isFalse();
	}

	@Test
	public void verifySuccessIsTrueWhenNoSyncErrorsProvided() {
		assertThat(new ChangeSetSummaryMessageImpl(changeSet, summary, Collections.emptySet(), Collections.emptySet()).isSuccess())
				.as("Success must be true when no sync errors are provided.")
				.isTrue();
	}

	@Test
	public void verifySuccessIsFalseWhenSyncErrorsProvided() {
		final ChangeSetSummaryMessageImpl summaryMessage =
				new ChangeSetSummaryMessageImpl(changeSet, summary, Collections.emptySet(), Collections.singleton(syncErrorResultItemOne));

		assertThat(summaryMessage.isSuccess())
				.as("Success must be false when sync errors are provided.")
				.isFalse();
	}

	@Test
	public void verifySummaryMessageIsErrorMessage() {
		final String errorMessage = "It went boom";
		assertThat(new ChangeSetSummaryMessageImpl(changeSet, errorMessage).getPublishSummary())
				.as("Summary message must be the error message when provided.")
				.isEqualTo(errorMessage);
	}

	@Test
	public void verifySummaryMessageContainsFailureStatusAndSyncInfo() {
		// Contains 2 items
		final Collection<SyncResultItem> successResults = ImmutableSet.of(syncErrorResultItemOne, syncErrorResultItemTwo);

		// Contains 1 item
		final Collection<SyncErrorResultItem> failureResults = ImmutableSet.of(syncErrorResultItemOne);

		final ChangeSetSummaryMessage summaryMessage =
				new ChangeSetSummaryMessageImpl(changeSet, summary, successResults, failureResults);

		assertThat(summaryMessage.getPublishSummary())
				.contains("Total Number of Objects: 3<br/><br/>"
						+ "Successful Objects:<br/>JobEntryImpl: 2<br/><br/>"
						+ "Failed Objects:<br/>JobEntryImpl: 1<br/>");
	}

	@Test
	public void verifySummaryMessageContainsSuccessStatusAndSyncInfo() {
		// Contains 2 items
		final Collection<SyncResultItem> successResults = ImmutableSet.of(syncErrorResultItemOne, syncErrorResultItemTwo);

		// Contains 0 items
		final Collection<SyncErrorResultItem> failureResults = Collections.emptyList();

		final ChangeSetSummaryMessage summaryMessage =
				new ChangeSetSummaryMessageImpl(changeSet, summary, successResults, failureResults);

		assertThat(summaryMessage.getPublishSummary())
				.contains("Total Number of Objects: 2<br/><br/>"
						+ "Successful Objects:<br/>JobEntryImpl: 2<br/>");
	}

}