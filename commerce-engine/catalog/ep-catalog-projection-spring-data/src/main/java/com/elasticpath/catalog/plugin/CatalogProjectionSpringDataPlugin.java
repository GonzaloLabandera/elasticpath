/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin;

import java.util.Map;
import java.util.Optional;

import com.elasticpath.catalog.CatalogReaderCapability;
import com.elasticpath.catalog.spi.CatalogProjectionPlugin;
import com.elasticpath.catalog.spi.CatalogWriterCapability;

/**
 * Implementation of {@link CatalogProjectionPlugin} that utilises Spring Data to persist projections to a data store.
 */
public class CatalogProjectionSpringDataPlugin implements CatalogProjectionPlugin {

	private final Map<Class<?>, CatalogWriterCapability<?>> writerCapabilities;
	private final Map<Class<?>, CatalogReaderCapability> readerCapabilities;

	/**
	 * Constructor.
	 *
	 * @param writerCapabilities map of {@link CatalogWriterCapability}.
	 * @param readerCapabilities map of {@link CatalogReaderCapability}.
	 */
	public CatalogProjectionSpringDataPlugin(final Map<Class<?>, CatalogWriterCapability<?>> writerCapabilities,
											 final Map<Class<?>, CatalogReaderCapability> readerCapabilities) {
		this.writerCapabilities = writerCapabilities;
		this.readerCapabilities = readerCapabilities;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CatalogWriterCapability<?>> Optional<T> getWriterCapability(final Class<T> capabilityClass) {
		return (Optional<T>) Optional.ofNullable(writerCapabilities.get(capabilityClass));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CatalogReaderCapability> Optional<T> getReaderCapability(final Class<T> capabilityClass) {
		return (Optional<T>) Optional.ofNullable(readerCapabilities.get(capabilityClass));
	}
}
