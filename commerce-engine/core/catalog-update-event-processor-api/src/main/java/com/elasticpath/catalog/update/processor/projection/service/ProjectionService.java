/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.projection.service;

import java.util.List;

import com.elasticpath.domain.catalog.Catalog;

/**
 * Represents a base interface for projection service.
 *
 * @param <S> is source entity for projection.
 * @param <P> is projection type.
 */
public interface ProjectionService<S, P> {

	/**
	 * Build list of projections from source.
	 *
	 * @param source is source entity for projection building
	 * @param catalog is catalog that this object belongs to.
	 * @return list of projections.
	 */
	List<P> buildProjections(S source, Catalog catalog);

	/**
	 * Build list of projections from source for all stores.
	 *
	 * @param source is source entity for projection building
	 * @return list of projections.
	 */
	List<P> buildAllStoresProjections(S source);
}
