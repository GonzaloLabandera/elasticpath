/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

import com.elasticpath.xpf.XPFExtensionDefaultSelectorModeEnum;
import com.elasticpath.xpf.XPFExtensionSelector;

/**
 * Configuration for extension point.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class ExtensionPointConfigurationDTO {
	private final Integer priority;
	private final String extensionClassName;
	private final XPFExtensionDefaultSelectorModeEnum defaultSelectorMode;
	private final List<XPFExtensionSelector> selectors;
	private volatile String pluginId;
	private final String extensionGuid;
	private final Set<PluginSettingDTO> extensionSettings;

	/**
	 * Constructor.
	 *
	 * @param extensionClassName  the extension class name
	 * @param defaultSelectorMode the default selector mode
	 * @param selectors           the selectors
	 * @param priority            the priority
	 * @param pluginId            the plugin id
	 * @param extensionGuid       the extension guid
	 * @param extensionSettings   the extension settings
	 */
	public ExtensionPointConfigurationDTO(final String extensionClassName, final XPFExtensionDefaultSelectorModeEnum defaultSelectorMode,
										  final List<XPFExtensionSelector> selectors, final Integer priority, final String pluginId,
										  final String extensionGuid, final Set<PluginSettingDTO> extensionSettings) {
		this.priority = priority;
		this.extensionClassName = extensionClassName;
		this.defaultSelectorMode = defaultSelectorMode;
		this.selectors = selectors;
		this.pluginId = pluginId;
		this.extensionGuid = extensionGuid;
		this.extensionSettings = extensionSettings;
	}

	/**
	 * Constructor.
	 *
	 * @param extensionClassName  the extension class name
	 * @param defaultSelectorMode the default selector mode
	 * @param selectors           the selectors
	 * @param priority            the priority
	 * @param pluginId            the plugin id
	 * @param extensionSettings   the extension settings
	 */
	public ExtensionPointConfigurationDTO(final String extensionClassName, final XPFExtensionDefaultSelectorModeEnum defaultSelectorMode,
										  final List<XPFExtensionSelector> selectors, final int priority, final String pluginId,
										  final Set<PluginSettingDTO> extensionSettings) {
		this.defaultSelectorMode = defaultSelectorMode;
		this.priority = priority;
		this.extensionClassName = extensionClassName;
		this.selectors = selectors;
		this.pluginId = pluginId;
		this.extensionGuid = UUID.randomUUID().toString();
		this.extensionSettings = extensionSettings;
	}

	public Integer getPriority() {
		return priority;
	}
}
