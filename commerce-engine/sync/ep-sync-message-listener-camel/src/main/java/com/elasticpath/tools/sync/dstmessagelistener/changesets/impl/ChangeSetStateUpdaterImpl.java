/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets.impl;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.domain.changeset.ChangeSetStateCode;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetStateUpdater;

/**
 * Implementation of {@link ChangeSetStateUpdater}.
 */
public class ChangeSetStateUpdaterImpl implements ChangeSetStateUpdater {

	private ChangeSetManagementService changeSetManagementService;

	private LoadTuner loadTuner;

	@Override
	public ChangeSet updateState(final ChangeSet changeSet, final ChangeSetStateCode newState) {
		return getChangeSetManagementService().updateState(changeSet.getGuid(), newState, getLoadTuner());
	}

	protected ChangeSetManagementService getChangeSetManagementService() {
		return changeSetManagementService;
	}

	public void setChangeSetManagementService(final ChangeSetManagementService changeSetManagementService) {
		this.changeSetManagementService = changeSetManagementService;
	}

	protected LoadTuner getLoadTuner() {
		return loadTuner;
	}

	public void setLoadTuner(final LoadTuner loadTuner) {
		this.loadTuner = loadTuner;
	}

}
