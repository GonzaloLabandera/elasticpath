/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import static java.lang.String.format;

import org.apache.log4j.Logger;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.tools.sync.client.SyncJobConfiguration;
import com.elasticpath.tools.sync.client.SyncToolLauncher;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.DataSyncToolInvoker;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.SyncJobConfigurationFactory;
import com.elasticpath.tools.sync.target.result.Summary;

/**
 * Takes in a changeset Guid and uses the SyncToolLauncher to publish the change set.
 */
public class DataSyncToolInvokerImpl implements DataSyncToolInvoker {

	private static final Logger LOG = Logger.getLogger(DataSyncToolInvokerImpl.class);

	private SyncToolLauncher syncToolLauncher;

	private SyncJobConfigurationFactory syncJobConfigurationFactory;

	@Override
	public Summary processSyncToolJob(final ChangeSet changeSet) {
		final SyncJobConfiguration syncJobConfiguration = getSyncJobConfigurationFactory().createSyncJobConfiguration(changeSet.getGuid());

		LOG.debug(format("Invoking DST with Job Configuration %s", syncJobConfiguration));

		return getSyncToolLauncher().processJob(syncJobConfiguration);
	}

	protected SyncToolLauncher getSyncToolLauncher() {
		return syncToolLauncher;
	}

	public void setSyncToolLauncher(final SyncToolLauncher syncToolLauncher) {
		this.syncToolLauncher = syncToolLauncher;
	}

	protected SyncJobConfigurationFactory getSyncJobConfigurationFactory() {
		return syncJobConfigurationFactory;
	}

	public void setSyncJobConfigurationFactory(final SyncJobConfigurationFactory syncJobConfigurationFactory) {
		this.syncJobConfigurationFactory = syncJobConfigurationFactory;
	}

}