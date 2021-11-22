/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

import java.util.List;

/**
 * MBean interface. This interface should not be used by other services; it should only be invoked by JMX.
 */
public interface XPFExtensionResolverMBean {
	/**
	 * Returns all available extension class names for an extension point.
	 * @param extensionPoint The extension point to retrieve classes for
	 * @return List of class names
	 */
	List<String> getAllAvailableExtensionClassNames(String extensionPoint);

	/**
	 * Returns all currently assigned and active class names for an extension point.
	 * @param extensionPoint The extension point to retrieve classes for
	 * @return List of class names
	 */
	List<String> getAssignedExtensionClassNames(String extensionPoint);

	/**
	 * Assigns an extension point class to a store with the given priority.
	 * @param extensionClass The extension class to assign
	 * @param pluginId       The external plugin ID that the extension class belongs to, or null for embedded extensions
	 * @param extensionPoint The extension point to assign the extension to
	 * @param storeCode The store to assign it to
	 * @param priority the priority of the extension
	 */
	void assignToStore(String extensionClass, String pluginId, String extensionPoint, String storeCode, int priority);

	/**
	 * Removes an extension class from a store.
	 * @param extensionClass The extension class to remove
	 * @param pluginId       The external plugin ID that the extension class belongs to, or null for embedded extensions
	 * @param extensionPoint The extension point to remove the extension from
	 * @param storeCode The store to remove it from
	 */
	void removeFromStore(String extensionClass, String pluginId, String extensionPoint, String storeCode);
}
