/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.json;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class XPFExtensionPointConfiguration {
	private final Identifier identifier;
	private final boolean enabled;
	private final Integer priority;
	private final String defaultSelectorMode;
	private final List<Selector> selectors;
	private final Set<Setting> settings;

	/**
	 * Constructor.
	 *
	 * @param identifier          the configuration identifier
	 * @param enabled             the enabled
	 * @param priority            the priority
	 * @param defaultSelectorMode the defaultSelectorMode
	 * @param selectors           the selectors
	 * @param settings            the settings
	 */
	@ConstructorProperties({"identifier", "enabled", "priority", "defaultSelectorMode", "selectors", "settings"})
	public XPFExtensionPointConfiguration(final Identifier identifier, final boolean enabled, final Integer priority,
										  final String defaultSelectorMode, final List<Selector> selectors, final Set<Setting> settings) {
		this.identifier = identifier;
		this.enabled = enabled;
		this.priority = priority;
		this.defaultSelectorMode = defaultSelectorMode;
		this.selectors = selectors;
		this.settings = settings;
	}
}
