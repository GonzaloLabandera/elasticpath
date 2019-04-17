/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import static java.lang.String.format;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateUpdater;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateValidator;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;
import com.elasticpath.tools.sync.dstmessagelistener.messages.impl.ChangeSetSummaryMessageImpl;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.ChangeSetPublisher;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.DataSyncToolInvoker;
import com.elasticpath.tools.sync.target.result.Summary;

/**
 * Façade implementation of {@link ChangeSetPublisher}.
 */
public class ChangeSetPublisherImpl implements ChangeSetPublisher {

	private ChangeSetLoader changeSetLoader;

	private ChangeSetStateValidator changeSetStateValidator;

	private DataSyncToolInvoker dataSyncToolInvoker;

	private ChangeSetStateUpdater changeSetStateUpdater;

	@Override
	public ChangeSetSummaryMessage publish(final String changeSetGuid) {
		final ChangeSet changeSet = getChangeSetLoader().load(changeSetGuid);

		if (!getChangeSetStateValidator().validate(changeSet, ChangeSetStateCode.READY_TO_PUBLISH, ChangeSetStateCode.FINALIZED,
				ChangeSetStateCode.LOCKED)) {
			final String failureMessage = format("ChangeSet with GUID [%s] current state [%s] does not match the acceptable state [%s]",
												 changeSet.getGuid(),
												 changeSet.getStateCode(),
												 ChangeSetStateCode.READY_TO_PUBLISH);

			// Put the change set the state back to LOCKED so it can be resubmitted.
			lockChangeSet(changeSet);

			return createChangeSetSummaryMessage(changeSet, failureMessage);
		}

		final Summary summary = getDataSyncToolInvoker().processSyncToolJob(changeSet);

		if (summary.hasErrors() && !ChangeSetStateCode.FINALIZED.equals(changeSet.getStateCode())) {
			// Put the change set the state back to LOCKED so it can be resubmitted.
			lockChangeSet(changeSet);
		} else {
			getChangeSetStateUpdater().updateState(changeSet, ChangeSetStateCode.FINALIZED);
		}

		return createChangeSetSummaryMessage(changeSet, summary);
	}

	private void lockChangeSet(final ChangeSet changeSet) {
		getChangeSetStateUpdater().updateState(changeSet, ChangeSetStateCode.LOCKED);
	}

	/**
	 * Factory method for creating new ChangeSetSummaryMessage instances.
	 *
	 * @param changeSet the GUID of the change set that was published
	 * @param errorMessage the error message that prevented the sync from taking place
	 * @return a new ChangeSetSummaryMessage instance
	 */
	protected ChangeSetSummaryMessage createChangeSetSummaryMessage(final ChangeSet changeSet, final String errorMessage) {
		return new ChangeSetSummaryMessageImpl(changeSet, errorMessage);
	}

	/**
	 * Factory method for creating new ChangeSetSummaryMessage instances.
	 *
	 * @param changeSet the GUID of the change set that was published
	 * @param summary a summary of the synchronisation attempt
	 * @return a new ChangeSetSummaryMessage instance
	 */
	protected ChangeSetSummaryMessage createChangeSetSummaryMessage(final ChangeSet changeSet, final Summary summary) {
		return new ChangeSetSummaryMessageImpl(changeSet, summary,
											   summary.getSuccessResults(),
											   summary.getSyncErrors()
		);
	}

	protected ChangeSetLoader getChangeSetLoader() {
		return changeSetLoader;
	}

	public void setChangeSetLoader(final ChangeSetLoader changeSetLoader) {
		this.changeSetLoader = changeSetLoader;
	}

	protected ChangeSetStateValidator getChangeSetStateValidator() {
		return changeSetStateValidator;
	}

	public void setChangeSetStateValidator(final ChangeSetStateValidator changeSetStateValidator) {
		this.changeSetStateValidator = changeSetStateValidator;
	}

	protected DataSyncToolInvoker getDataSyncToolInvoker() {
		return dataSyncToolInvoker;
	}

	public void setDataSyncToolInvoker(final DataSyncToolInvoker dataSyncToolInvoker) {
		this.dataSyncToolInvoker = dataSyncToolInvoker;
	}

	protected ChangeSetStateUpdater getChangeSetStateUpdater() {
		return changeSetStateUpdater;
	}

	public void setChangeSetStateUpdater(final ChangeSetStateUpdater changeSetStateUpdater) {
		this.changeSetStateUpdater = changeSetStateUpdater;
	}

}
