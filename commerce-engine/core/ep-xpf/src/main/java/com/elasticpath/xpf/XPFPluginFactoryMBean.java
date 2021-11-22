/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

/**
 * A factory for managing external plugins. This interface should not be used by other services; it should only be invoked by JMX.
 */
public interface XPFPluginFactoryMBean {

	/**
	 * Loads the plugin referred to by the URI.
	 *
	 * @param jarUri The URI of the jar containing the plugin.
	 */
	void loadPlugin(String jarUri);

	/**
	 * Unloads the plugin specified by the plugin ID.
	 *
	 * @param pluginId The plugin ID to unload
	 */
	void unloadPlugin(String pluginId);

	/**
	 * Replaces the plugin at the oldJarUri with the one at newJarUri.
	 *
	 * @param oldPluginId The plugin ID to replace
	 * @param newJarUri The URI of the plugin to replace with
	 */
	void replacePlugin(String oldPluginId, String newJarUri);
}
