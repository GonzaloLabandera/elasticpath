/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import com.elasticpath.xpf.XPFExtensionDefaultSelectorModeEnum;
import com.elasticpath.xpf.XPFExtensionSelector;
import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;
import com.elasticpath.xpf.dto.PluginSettingDTO;

/**
 * A builder that builds {@link ExtensionPointConfigurationDTO}.
 */
public class ExtensionPointConfigurationBuilder {
	private String extensionClassName;
	private XPFExtensionDefaultSelectorModeEnum selectorMode;
	private List<XPFExtensionSelector> selectors;
	private Integer priority;
	private String pluginId;
	private String extensionGuid;
	private Set<PluginSettingDTO> extensionSettings;

	/**
	 * Sets the extension class name.
	 *
	 * @param extensionClassName the extension class name
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setExtensionClassName(final String extensionClassName) {
		this.extensionClassName = extensionClassName;
		return this;
	}

	/**
	 * Sets the default selector mode.
	 *
	 * @param selectorMode the default selector mode
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setDefaultSelectorMode(final XPFExtensionDefaultSelectorModeEnum selectorMode) {
		this.selectorMode = selectorMode;
		return this;
	}

	/**
	 * Sets the selectors.
	 *
	 * @param selectors the selectors
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setSelectors(final List<XPFExtensionSelector> selectors) {
		this.selectors = selectors;
		return this;
	}

	/**
	 * Sets the selector.
	 *
	 * @param selector the selector
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setSelector(final XPFExtensionSelector selector) {
		final List<XPFExtensionSelector> configSelectors = new ArrayList<>();
		configSelectors.add(selector);
		this.selectors = configSelectors;
		return this;
	}

	/**
	 * Sets the priority.
	 *
	 * @param priority the priority
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setPriority(final int priority) {
		this.priority = priority;
		return this;
	}

	/**
	 * Sets the pluginId.
	 *
	 * @param pluginId the pluginId
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setPluginId(final String pluginId) {
		this.pluginId = pluginId;
		return this;
	}

	/**
	 * Sets the extensionGuid.
	 *
	 * @param extensionGuid the extensionGuid
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setExtensionGuid(final String extensionGuid) {
		this.extensionGuid = extensionGuid;
		return this;
	}

	/**
	 * Sets the extension settings.
	 *
	 * @param extensionSettings the extension settings
	 * @return the builder
	 */
	public ExtensionPointConfigurationBuilder setExtensionSettings(final Set<PluginSettingDTO> extensionSettings) {
		this.extensionSettings = extensionSettings;
		return this;
	}

	/**
	 * Build {@link ExtensionPointConfigurationDTO} object.
	 *
	 * @return the new {@link ExtensionPointConfigurationDTO}
	 */
	public ExtensionPointConfigurationDTO build() {
		return new ExtensionPointConfigurationDTO(extensionClassName, selectorMode, ObjectUtils.firstNonNull(selectors, Collections.emptyList()),
				priority, pluginId, extensionGuid, ObjectUtils.firstNonNull(extensionSettings, Collections.emptySet()));
	}
}