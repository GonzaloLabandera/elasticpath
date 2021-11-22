/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Сlass, that's describe custom methods for {@link ProjectionEntity}, that's works with database.
 */
public interface CatalogProjectionRepositoryCustom {
	/**
	 * Find {@link ProjectionEntity} in database by parameters.
	 *
	 * @param type  type of {@link ProjectionEntity}
	 * @param code  code of {@link ProjectionEntity}
	 * @param store store of {@link ProjectionEntity}
	 * @return {@link ProjectionEntity}
	 */
	Optional<ProjectionEntity> extractProjectionEntity(String type, String code, String store);

	/**
	 * Find {@link ProjectionEntity} in database by parameters.
	 *
	 * @param type type of {@link ProjectionEntity}
	 * @param code code of {@link ProjectionEntity}
	 * @return {@link ProjectionEntity}
	 */
	List<ProjectionEntity> extractProjectionsByTypeAndCode(String type, String code);

	/**
	 * Find collection of {@link ProjectionEntity} with latest versions by type and code.
	 *
	 * @param type projection type.
	 * @param code projection code.
	 * @return collection of ProjectionEntity.
	 */
	List<ProjectionEntity> findNotDeletedProjectionEntities(String type, String code);

	/**
	 * Find collection of {@link ProjectionEntity} with latest versions by type and code with given limit and start after startAfter.
	 * The results are sorted by projection code in ascending order.
	 *
	 * @param type       projection type.
	 * @param store      projection store.
	 * @param limit      is limit of amount.
	 * @param startAfter is value for start after.
	 * @return collection of ProjectionEntity.
	 */
	List<ProjectionEntity> extractProjectionsByTypeAndStoreWithPagination(String type, String store, int limit, String startAfter);

	/**
	 * Find collection of {@link ProjectionEntity} with latest versions by type and code with given limit and start after startAfter and
	 * modifiedSince.
	 * The results are sorted by projection code in ascending order.
	 *
	 * @param type          projection type.
	 * @param store         projection store.
	 * @param limit         is limit of amount.
	 * @param startAfter    is value for start after.
	 * @param modifiedSince is value for start modified since.
	 * @return collection of ProjectionEntity.
	 */
	List<ProjectionEntity> extractProjectionsByTypeAndStoreWithPaginationAndModified(String type, String store, int limit, String startAfter,
											 Date modifiedSince);

	/**
	 * Find latest projections with codes from list.
	 *
	 * @param type  projection type.
	 * @param store projection store.
	 * @param codes list of projection codes.
	 * @return collection of ProjectionEntity.
	 */
	List<ProjectionEntity> findLatestProjectionsWithCodes(String type, String store, List<String> codes);

	/**
	 * Find latest projections with codes from list.
	 *
	 * @param type  projection type.
	 * @param codes list of projection codes.
	 * @return collection of ProjectionEntity.
	 */
	List<ProjectionEntity> findLatestProjectionsWithCodes(String type, List<String> codes);

	/**
	 * Reads nearest date projections to expire.
	 *
	 * @param currentDate  current date.
	 * @return a nearest date projections to expire.
	 */
	Optional<Date> extractNearestExpiredTime(Date currentDate);

	/**
	 * Deletes all ProjectionEntities with given type.
	 *
	 * @param type of ProjectionEntity to remove.
	 * @return count of removed ProjectionEntity.
	 */
	int deleteAllProjectionsInBatchByType(String type);
}