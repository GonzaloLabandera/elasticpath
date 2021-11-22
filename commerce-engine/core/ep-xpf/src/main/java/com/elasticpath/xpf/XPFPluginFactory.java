/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

import java.net.URI;

/**
 * A factory for managing external plugins.
 */
public interface XPFPluginFactory {

	/**
	 * Loads the plugin referred to by the URI.
	 *
	 * @param jarUri The URI of the jar containing the plugin.
	 */
	void loadPlugin(URI jarUri);

	/**
	 * Unloads the plugin specified by the URI.
	 *
	 * @param pluginId The plugin ID to unload.
	 */
	void unloadPlugin(String pluginId);

	/**
	 * Replaces the plugin at the oldJarUri with the one at newJarUri.
	 *
	 * @param pluginId The plugin ID to replace.
	 * @param newJarUri The URI of the plugin to replace with.
	 */
	void replacePlugin(String pluginId, URI newJarUri);
}
