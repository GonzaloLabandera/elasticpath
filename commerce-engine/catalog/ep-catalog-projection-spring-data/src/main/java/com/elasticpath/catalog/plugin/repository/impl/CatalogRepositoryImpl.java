/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository.impl;

import com.elasticpath.catalog.plugin.repository.CatalogProjectionHistoryRepository;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepository;
import com.elasticpath.catalog.plugin.repository.CatalogRepository;

/**
 * Implementation of {@link CatalogRepository} that perform operation in database for
 * {@link com.elasticpath.catalog.plugin.entity.ProjectionEntity} and {@link com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity}
 * repositories.
 */
public class CatalogRepositoryImpl implements CatalogRepository {
	private final CatalogProjectionRepository catalogProjectionRepository;
	private final CatalogProjectionHistoryRepository catalogProjectionHistoryRepository;

	/**
	 * Constructor.
	 *
	 * @param catalogProjectionRepository        perform operation in database for {@link com.elasticpath.catalog.plugin.entity.ProjectionEntity}.
	 * @param catalogProjectionHistoryRepository perform operation in database for
	 * {@link com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity}
	 */
	public CatalogRepositoryImpl(final CatalogProjectionRepository catalogProjectionRepository,
								 final CatalogProjectionHistoryRepository catalogProjectionHistoryRepository) {
		this.catalogProjectionRepository = catalogProjectionRepository;
		this.catalogProjectionHistoryRepository = catalogProjectionHistoryRepository;
	}

	@Override
	public CatalogProjectionRepository getProjectionRepository() {
		return catalogProjectionRepository;
	}

	@Override
	public CatalogProjectionHistoryRepository getProjectionHistoryRepository() {
		return catalogProjectionHistoryRepository;
	}
}
