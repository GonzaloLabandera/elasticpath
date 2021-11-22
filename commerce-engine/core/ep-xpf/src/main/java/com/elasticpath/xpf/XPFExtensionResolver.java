/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

import java.util.List;

import org.pf4j.ExtensionPoint;

import com.elasticpath.xpf.dto.ExtensionPointConfigurationDTO;

/**
 * Extension resolver for Extension Points.
 */
public interface XPFExtensionResolver {

	/**
	 * Assign extension for a plugin Id.
	 * @param pluginId the plugin id
	 */
	void assignPluginExtensions(String pluginId);

	/**
	 * Returns all available extension class names for an extension point.
	 * @param extensionPoint The extension point to retrieve classes for
	 * @return List of class names
	 */
	List<String> getAllAvailableExtensionClassNames(XPFExtensionPointEnum extensionPoint);

	/**
	 * Returns all currently assigned and active class names for an extension point.
	 * @param extensionPoint The extension point to retrieve classes for
	 * @return List of class names
	 */
	List<String> getAssignedExtensionClassNames(XPFExtensionPointEnum extensionPoint);

	/**
	 * Returns all currently assigned and active extension configurations for an extension from different XPFExtensionPointEnum.
	 * @param extensionClass The extension class to retrieve extension configurations for
	 * @return List of extension configurations
	 */
	List<ExtensionPointConfigurationDTO> getAssignedExtensionConfigurations(Class<?> extensionClass);

	/**
	 * Returns all currently assigned and active class names for an extension point and plugin.
	 * @param extensionPoint The extension point to retrieve classes for
	 * @param pluginId The plugin id
	 * @return List of class names
	 */
	List<String> getAssignedExtensionClassNames(XPFExtensionPointEnum extensionPoint, String pluginId);

	/**
	 * Assigns an extension class to an extension point for a given selector and priority.
	 * @param extensionClassName The extension class to assign
	 * @param pluginId The plugin id
	 * @param extensionPoint The extension point to assign the extension to
	 * @param selector The selector to assign it to
	 * @param priority the priority of the extension
	 */
	void assignExtensionToSelector(String extensionClassName, String pluginId, XPFExtensionPointEnum extensionPoint,
								   XPFExtensionSelector selector, int priority);

	/**
	 * Removes an extension class from an extension point for a given selector.
	 * @param extensionClassName The extension class to remove
	 * @param pluginId The plugin id
	 * @param extensionPoint The extension point to remove the extension from
	 * @param selector The selector to remove it from
	 */
	void removeExtensionFromSelector(String extensionClassName, String pluginId, XPFExtensionPointEnum extensionPoint,
									 XPFExtensionSelector selector);

	/**
	 * Filter down to the currently assigned extensions for a given extension point and selectionContext. Returns
	 * the extensions sorted in priority order.
	 * @param extensions Available extensions
	 * @param extensionPointEnum The extension point to resolve for
	 * @param selectionContext The selection context
	 * @param <EXTENSIONPOINT> The extension point type
	 * @return List of the assigned extensions sorted by priority
	 */
	<EXTENSIONPOINT extends ExtensionPoint> List<EXTENSIONPOINT> resolveExtensionPoints(List<EXTENSIONPOINT> extensions,
																						XPFExtensionPointEnum extensionPointEnum,
																						XPFExtensionSelector selectionContext);

	/**
	 * Updates plugin id in extension configurations.
	 *
	 * @param oldId The old plugin Id.
	 * @param newId The new plugin Id.
	 */
	void updatePluginId(String oldId, String newId);
}
