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
public class PluginConfiguration {
	private final Identifier identifier;
	private final List<Setting> settings;

	/**
	 * Constructor.
	 *
	 * @param identifier configuration identifier
	 * @param settings   plugin settings
	 */
	@ConstructorProperties({"identifier", "settings"})
	public PluginConfiguration(final Identifier identifier, final List<Setting> settings) {
		this.identifier = identifier;
		this.settings = settings;
	}
}
