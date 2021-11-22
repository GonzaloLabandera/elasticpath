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
public class ExtensionConfigurations {
	private final List<XPFExtensionPointConfiguration> extensions;

	/**
	 * Constructor.
	 *
	 * @param extensions extension configurations
	 */
	@ConstructorProperties({"extensions"})
	public ExtensionConfigurations(final List<XPFExtensionPointConfiguration> extensions) {
		this.extensions = extensions;
	}
}
