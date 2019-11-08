/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;

/**
 * Represents a base interface for converter from source to projection.
 *
 * @param <S> is source entity for projection.
 * @param <P> is projection type.
 */
public interface Converter<S, P extends Projection> {

	/**
	 * Converts from source to projection.
	 *
	 * @param source  is source entity for conversion.
	 * @param store   is given store of source
	 * @param catalog is given catalog of source
	 * @return projection
	 */
	P convert(S source, Store store, Catalog catalog);
}