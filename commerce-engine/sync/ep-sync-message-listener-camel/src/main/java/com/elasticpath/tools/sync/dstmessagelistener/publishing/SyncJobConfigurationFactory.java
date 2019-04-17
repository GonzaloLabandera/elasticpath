/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing;

import com.elasticpath.tools.sync.client.SyncJobConfiguration;

/**
 * Creates new {@link SyncJobConfiguration} instances.
 */
public interface SyncJobConfigurationFactory {

	/**
	 * Creates a new {@link SyncJobConfiguration} instance.
	 *
	 * @param changeSetGuid the GUID of the Change Set to publish
	 * @return a new {@link SyncJobConfiguration} instance
	 */
	SyncJobConfiguration createSyncJobConfiguration(String changeSetGuid);

}
