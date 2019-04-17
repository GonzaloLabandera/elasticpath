/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing.impl;

import com.elasticpath.tools.sync.client.SyncJobConfiguration;
import com.elasticpath.tools.sync.client.impl.SyncJobConfigurationImpl;
import com.elasticpath.tools.sync.dstmessagelistener.publishing.SyncJobConfigurationFactory;

/**
 * Implementation of {@link SyncJobConfigurationFactory}.
 */
public class SyncJobConfigurationFactoryImpl implements SyncJobConfigurationFactory {

	private String rootPath;
	private String subDir;

	@Override
	public SyncJobConfiguration createSyncJobConfiguration(final String changeSetGuid) {
		final SyncJobConfigurationImpl syncJobConfiguration = new SyncJobConfigurationImpl();

		syncJobConfiguration.setAdapterParameter(changeSetGuid);
		syncJobConfiguration.setRootPath(getRootPath());
		syncJobConfiguration.setSubDir(getSubDir());

		return syncJobConfiguration;
	}

	public void setRootPath(final String rootPath) {
		this.rootPath = rootPath;
	}

	protected String getRootPath() {
		return rootPath;
	}

	public void setSubDir(final String subDir) {
		this.subDir = subDir;
	}

	protected String getSubDir() {
		return subDir;
	}

}
