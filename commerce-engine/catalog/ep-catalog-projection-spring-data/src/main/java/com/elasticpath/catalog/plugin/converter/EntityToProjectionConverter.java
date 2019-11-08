/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Represent converter, that convert {@link ProjectionEntity} to specific {@link Projection}.
 *
 * @param <T> - type of capability.
 */
public interface EntityToProjectionConverter<T extends Projection> {
	/**
	 * Convert {@link ProjectionEntity} to {@link Projection}.
	 *
	 * @param entity is entity to convert.
	 * @return specific projection.
	 */
	T convert(ProjectionEntity entity);
}