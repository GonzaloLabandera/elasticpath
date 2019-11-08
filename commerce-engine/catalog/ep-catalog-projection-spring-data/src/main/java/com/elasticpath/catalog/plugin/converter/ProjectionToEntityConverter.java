/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import java.util.Date;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;

/**
 * Represent converter, that convert {@link Projection} to {@link ProjectionEntity} and vice versa.
 */
public interface ProjectionToEntityConverter {

	/**
	 * Convert {@link Projection} to {@link ProjectionEntity}.
	 *
	 * @param source  source to convertFromProjection.
	 * @return {@link ProjectionEntity}.
	 */
	ProjectionEntity convertFromProjection(Projection source);

	/**
	 * Convert {@link ProjectionEntity} to {@link Projection}.
	 *
	 * @param projectionEntity source to convertFromProjection.
	 * @param <T> type of projection.
	 * @return {@link Projection}.
	 */
	<T extends Projection> T convertToProjection(ProjectionEntity projectionEntity);

	/**
	 * Convert {@link ProjectionEntity} to {@link ProjectionHistoryEntity}.
	 *
	 * @param source  source to convertToProjectionHistory.
	 * @return {@link ProjectionHistoryEntity}.
	 */
	ProjectionHistoryEntity convertToProjectionHistory(ProjectionEntity source);

	/**
	 * Converts to ProjectionEntity which is marked as deleted.
	 * Set up:
	 * deleted = true
	 * schemaVersion = null
	 * contentHash = null
	 * content = null
	 *
	 * @param projectionEntity source.
	 * @param date             projection entity date.
	 * @return deleted ProjectionEntity.
	 */
	ProjectionEntity convertToDeletedEntity(ProjectionEntity projectionEntity, Date date);
}
