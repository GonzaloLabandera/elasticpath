/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository;

import org.springframework.data.repository.CrudRepository;

import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryId;

/**
 * Class that perform operation in database for {@link ProjectionHistoryEntity} repository.
 */
public interface CatalogProjectionHistoryRepository extends CrudRepository<ProjectionHistoryEntity, ProjectionHistoryId> {

	/**
	 * Delete all ProjectionHistoryEntity with given type.
	 *
	 * @param type of ProjectionHistoryEntity to remove.
	 */
	void deleteAllByHistoryIdType(String type);

}
