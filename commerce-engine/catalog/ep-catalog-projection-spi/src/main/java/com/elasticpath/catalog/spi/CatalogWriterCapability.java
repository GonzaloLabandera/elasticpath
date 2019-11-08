/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.spi;

import java.util.List;

/**
 * A capability that may be supported by a {@link CatalogProjectionPlugin}.
 *
 * @param <T> - type of capability.
 */
public interface CatalogWriterCapability<T> {

	/**
	 * Persists the given projection.
	 *
	 * @param projection the projection to persist
	 * @return true if projection is persisted successfully, false if projection is not persisted.
	 */
	boolean write(T projection);

	/**
	 * Removes the projection corresponding to the given GUID.
	 *
	 * @param guid the projection of the entity to remove
	 */
	void delete(String guid);

	/**
	 * Removes the projection corresponding to the given code and store.
	 *
	 * @param code  of projection.
	 * @param store store of projection.
	 */
	void delete(String code, String store);

	/**
	 * Persists given projections.
	 *
	 * @param projections the list of projections to persist
	 * @return the list of persisted projections
	 */
	List<T> writeAll(List<T> projections);
}
