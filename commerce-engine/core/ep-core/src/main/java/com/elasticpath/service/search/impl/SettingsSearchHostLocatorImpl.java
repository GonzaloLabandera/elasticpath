/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.service.search.SearchHostLocator;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Implementation of SearchHostLocator that retrieves the search host location from the settings service.
 * If deployed in a multiple search server/cluster configuration, a master search host URL should be
 * defined in the settings service with a context of 'master'. On the master search server, this class should
 * be configured so that 'requiresMaster' is true so it will use the master search host URL for index writing.
 */
public class SettingsSearchHostLocatorImpl implements SearchHostLocator {

	private static final String PRIMARY = "PRIMARY";

	private static final String REPLICA = "REPLICA";

	private static final String EP_SEARCH_MODE = "ep.search.mode";

	/**
	 * This has been replaced with getSearchMode.
	*/
	private boolean requiresMaster;

	private SettingValueProvider<String> replicaSearchHostLocationProvider;

	private SettingValueProvider<String> primarySearchHostLocationProvider;

	private static final Logger LOG = LogManager.getLogger(SettingsSearchHostLocatorImpl.class);

	/**
	 * Obtain the URL of the search host stored in the setting service.
	 * If multiple search servers are deployed in a cluster, a setting value with the context of 'master'
	 * should be set to define a different server URL for the master search server that writes to the indexes.
	 *
	 * @return the URL string of the search host.
	 */
	@Override
	public String getSearchHostLocation() {
		if (getSearchMode().equalsIgnoreCase(PRIMARY)) {
			return getPrimaryHostLocation();
		}

		return getReplicaSearchHostLocationProvider().get();
	}

	protected String getPrimaryHostLocation() {
		return getPrimarySearchHostLocationProvider().get();
	}

	@Deprecated
	protected String getDefaultHostLocation() {
		return getReplicaSearchHostLocationProvider().get();
	}

	/**
	 * Set whether the master search server is required.
	 * A master search server URL is available optionally depending on if
	 * search servers are deployed in a clustered configuration.
	 *
	 * @param requiresMaster true if getting master server responsible for indexing in a cluster.
	 */
	@Deprecated
	public void setRequiresMaster(final String requiresMaster) {
		this.requiresMaster = Boolean.parseBoolean(requiresMaster);
	}

	/**
	 * Determines the search mode as either PRIMARY or REPLICA.
	 *
	 * @return the search mode
	 */
	protected String getSearchMode() {
		String searchMode = System.getProperty(EP_SEARCH_MODE);
		if (searchMode != null) {
			return searchMode;
		}
		return getFallBackRequiresMaster();
	}

	/**
	 * Determine the search mode value of PRIMARY or REPLICA from the deprecated requiresMaster boolean.
	 *
	 * @return The search mode.
	 */
	private String getFallBackRequiresMaster() {
		LOG.warn("Deprecated ‘ep.search.requires.master’ ep.properties value should be replaced with ‘ep.search.mode’ JVM system property.");
		if (requiresMaster) {
			return PRIMARY;
		} else {
			return REPLICA;
		}
	}

	protected SettingValueProvider<String> getReplicaSearchHostLocationProvider() {
		return replicaSearchHostLocationProvider;
	}

	public void setReplicaSearchHostLocationProvider(final SettingValueProvider<String> replicaSearchHostLocationProvider) {
		this.replicaSearchHostLocationProvider = replicaSearchHostLocationProvider;
	}

	protected SettingValueProvider<String> getPrimarySearchHostLocationProvider() {
		return primarySearchHostLocationProvider;
	}

	public void setPrimarySearchHostLocationProvider(final SettingValueProvider<String> primarySearchHostLocationProvider) {
		this.primarySearchHostLocationProvider = primarySearchHostLocationProvider;
	}
}
