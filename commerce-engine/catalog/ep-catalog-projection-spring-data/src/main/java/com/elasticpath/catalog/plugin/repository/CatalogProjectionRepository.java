/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository;

import org.springframework.data.repository.CrudRepository;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;

/**
 * Class that perform operation in database for {@link ProjectionEntity}.
 */
public interface CatalogProjectionRepository extends CrudRepository<ProjectionEntity, ProjectionId>, CatalogProjectionRepositoryCustom {

	/**
	 * Delete all ProjectionEntity with given type.
	 *
	 * @param type of ProjectionEntity to remove.
	 * @return count of removed ProjectionEntity.
	 */
	int deleteAllByProjectionIdType(String type);

}
