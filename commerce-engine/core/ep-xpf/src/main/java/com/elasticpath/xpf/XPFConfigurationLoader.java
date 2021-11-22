/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

import java.util.Map;

import com.google.common.collect.ListMultimap;

import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.dto.XPFPluginConfigurationDTO;

/**
 * Loads configuration for plugins and extensions.
 */
public interface XPFConfigurationLoader {

	/**
	 * Gets extension assignment configuration from annotations and config files.
	 *
	 * @return The configuration map.
	 */
	ListMultimap<XPFExtensionPointEnum, ExtensionPointConfigurationDTO> getExtensionPointConfigurationMap();

	/**
	 * Gets plugin configuration from disk.
	 *
	 * @return The configuration. A map of pluginId -> config.
	 */
	Map<String, XPFPluginConfigurationDTO> getPluginConfigurationMap();
}
