/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.connectivity.impl;

import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogProjectionPluginProvider;

/**
 * Implementation of {@link CatalogProjectionPluginProvider}.
 */
public class CatalogProjectionPluginProviderImpl implements CatalogProjectionPluginProvider {

	private final CatalogProjectionPlugin catalogProjectionPlugin;

	/**
	 * Constructor.
	 *
	 * @param catalogProjectionPlugin {@link CatalogProjectionPlugin}
	 */
	public CatalogProjectionPluginProviderImpl(final CatalogProjectionPlugin catalogProjectionPlugin) {
		this.catalogProjectionPlugin = catalogProjectionPlugin;
	}

	@Override
	public CatalogProjectionPlugin getCatalogProjectionPlugin() {
		return catalogProjectionPlugin;
	}

}
