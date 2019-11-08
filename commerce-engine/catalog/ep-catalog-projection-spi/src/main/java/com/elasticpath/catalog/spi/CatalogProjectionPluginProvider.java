/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi;

/**
 * Provides {@link CatalogProjectionPlugin} instances.
 */
public interface CatalogProjectionPluginProvider {

	/**
	 * Returns a catalog projection plugin.
	 *
	 * @return a catalog projection plugin
	 */
	CatalogProjectionPlugin getCatalogProjectionPlugin();
}
