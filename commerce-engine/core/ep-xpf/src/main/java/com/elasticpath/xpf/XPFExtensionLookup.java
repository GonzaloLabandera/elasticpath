/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

import java.util.List;

import org.pf4j.ExtensionPoint;

/**
 * The public-facing interface through which consumers of ep-xpf interact with the extension point framework.
 * Provides the ability to fetch extension implementations for a given extension point.
 */
public interface XPFExtensionLookup {

	/**
	 * Gets a single extension point implementation for the given extension point.
	 *
	 * @param clazz The Extension Point interface whose extensions we are searching for
	 * @param extensionPoint The extension point
	 * @param selectionContext The selection context
	 * @param <EXTENSIONPOINT> The extension point type
	 * @return EXTENSIONPOINT The extension point implementation
	 */
	<EXTENSIONPOINT extends ExtensionPoint> EXTENSIONPOINT getSingleExtension(Class<EXTENSIONPOINT> clazz,
																			  XPFExtensionPointEnum extensionPoint,
																			  XPFExtensionSelector selectionContext);

	/**
	 * Gets all extension point implementations for the given extension point.
	 *
	 * @param clazz The extension point interface whose extensions we are searching for
	 * @param extensionPoint The extension point
	 * @param selectionContext The selection context
	 * @param <EXTENSIONPOINT> The extension point type
	 * @return List<EXTENSIONPOINT> The extension point implementations
	 */
	<EXTENSIONPOINT extends ExtensionPoint> List<EXTENSIONPOINT> getMultipleExtensions(Class<EXTENSIONPOINT> clazz,
																					   XPFExtensionPointEnum extensionPoint,
																					   XPFExtensionSelector selectionContext);
}
