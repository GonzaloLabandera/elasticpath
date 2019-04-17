/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.changesets.impl;

import static java.lang.String.format;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.service.changeset.ChangeSetManagementService;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.exception.ChangeSetNotFoundException;

/**
 * Implementation of {@link ChangeSetLoader}.
 */
public class ChangeSetLoaderImpl implements ChangeSetLoader {

	private static final String UNKNOWN_CHANGE_SET_NAME = "<UNKNOWN>";

	private ChangeSetManagementService changeSetManagementService;

	private LoadTuner loadTuner;

	@Override
	public ChangeSet load(final String changeSetGuid) {
		final ChangeSet changeSet = getChangeSetManagementService().get(changeSetGuid, getLoadTuner());

		if (changeSet == null) {
			throw new ChangeSetNotFoundException(format("Change set [%s] not found", changeSetGuid), UNKNOWN_CHANGE_SET_NAME);
		}

		return changeSet;
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
