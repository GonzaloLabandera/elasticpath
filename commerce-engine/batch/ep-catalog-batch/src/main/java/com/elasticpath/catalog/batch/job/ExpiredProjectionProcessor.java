/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.batch.job;

import org.springframework.batch.item.ItemProcessor;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.plugin.converter.ProjectionToEntityConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Represents an implementation of {@link ItemProcessor} to convert a list of ProjectionEntity to a list of Projection.
 *
 * @param <T> type of projection.
 */
public class ExpiredProjectionProcessor<T extends Projection> implements ItemProcessor<ProjectionEntity, T> {
	private final ProjectionToEntityConverter projectionToEntityConverter;

	/**
	 * Constructor.
	 *
	 * @param projectionToEntityConverter is projection to entity converter.
	 */
	public ExpiredProjectionProcessor(final ProjectionToEntityConverter projectionToEntityConverter) {
		this.projectionToEntityConverter = projectionToEntityConverter;
	}

	@Override
	public T process(final ProjectionEntity item)  {
		return projectionToEntityConverter.convertToProjection(item);
	}
}
