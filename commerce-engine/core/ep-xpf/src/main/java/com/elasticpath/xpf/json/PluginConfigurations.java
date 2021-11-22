/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.json;

import java.beans.ConstructorProperties;
import java.util.List;

import lombok.Data;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class PluginConfigurations {
	private final List<PluginConfiguration> plugins;

	/**
	 * Constructor.
	 *
	 * @param plugins plugin configurations
	 */
	@ConstructorProperties({"plugins"})
	public PluginConfigurations(final List<PluginConfiguration> plugins) {
		this.plugins = plugins;
	}
}
