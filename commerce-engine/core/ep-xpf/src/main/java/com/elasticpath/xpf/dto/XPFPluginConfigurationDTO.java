/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.dto;

import java.util.List;

import lombok.Data;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class XPFPluginConfigurationDTO {
	private final List<PluginSettingDTO> pluginSettings;

	/**
	 * Constructor.
	 *
	 * @param pluginSettings the plugin settings
	 */
	public XPFPluginConfigurationDTO(final List<PluginSettingDTO> pluginSettings) {
		this.pluginSettings = pluginSettings;
	}

	public List<PluginSettingDTO> getPluginSettings() {
		return pluginSettings;
	}
}