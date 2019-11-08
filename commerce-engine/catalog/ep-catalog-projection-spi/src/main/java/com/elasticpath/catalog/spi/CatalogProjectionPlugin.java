/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi;

import java.util.Optional;

import com.elasticpath.catalog.CatalogReaderCapability;

/**
 * Provides access to catalog projection writers and readers.
 */
public interface CatalogProjectionPlugin {

	/**
	 * Returns an instance of the writer capability, if supported by this plugin.
	 *
	 * @param capabilityClass the class of the capability
	 * @param <T>             the type of capability required
	 * @return an optional Catalog Writer Capability
	 */
	<T extends CatalogWriterCapability<?>> Optional<T> getWriterCapability(Class<T> capabilityClass);

	/**
	 * Returns an instance of the reader capability, if supported by this plugin.
	 *
	 * @param capabilityClass the class of the capability
	 * @param <T>             the type of capability required
	 * @return an optional Catalog Reader Capability
	 */
	<T extends CatalogReaderCapability> Optional<T> getReaderCapability(Class<T> capabilityClass);
}
