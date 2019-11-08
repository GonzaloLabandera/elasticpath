/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository;

/**
 * Class that perform operation in database for {@link com.elasticpath.catalog.plugin.entity.ProjectionEntity} and
 * {@link com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity} repositories.
 */
public interface CatalogRepository {
	/**
	 * @return class that perform operation in database for {@link com.elasticpath.catalog.plugin.entity.ProjectionEntity}.
	 */
	CatalogProjectionRepository getProjectionRepository();

	/**
	 * @return class that perform operation in database for {@link com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity}.
	 */
	CatalogProjectionHistoryRepository getProjectionHistoryRepository();
}
