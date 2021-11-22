/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.json;

import java.beans.ConstructorProperties;

import lombok.Data;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class Identifier {
	private final String pluginId;
	private final String extensionClass;
	private final String extensionPointKey;

	/**
	 * Constructor.
	 *
	 * @param pluginId          the plugin id
	 * @param extensionClass    the extension class
	 * @param extensionPointKey the extension point key
	 */
	@ConstructorProperties({"pluginId", "extensionClass", "extensionPointKey"})
	public Identifier(final String pluginId, final String extensionClass, final String extensionPointKey) {
		this.pluginId = pluginId;
		this.extensionClass = extensionClass;
		this.extensionPointKey = extensionPointKey;
	}
}
