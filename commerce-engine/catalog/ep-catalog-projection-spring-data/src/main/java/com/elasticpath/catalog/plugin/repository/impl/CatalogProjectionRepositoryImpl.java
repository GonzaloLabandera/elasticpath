/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository.impl;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepositoryCustom;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Implementation of {@link CatalogProjectionRepositoryCustom}.
 */
public class CatalogProjectionRepositoryImpl implements CatalogProjectionRepositoryCustom {

	private static final Logger LOGGER = LogManager.getLogger(CatalogProjectionRepositoryImpl.class);

	private static final int BATCH_SIZE = 1000;
	private static final int MAX_ERRORS = 10;

	@Autowired
	private PersistenceEngine persistenceEngine;

	@Override
	@SuppressWarnings("unchecked")
	public Optional<ProjectionEntity> extractProjectionEntity(final String type, final String code, final String store) {
		return persistenceEngine
			.<ProjectionEntity>retrieveByNamedQuery("FIND_ALL_BY_TYPE_AND_CODE_AND_STORE", type, code, store)
			.stream()
			.findFirst();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ProjectionEntity> extractProjectionsByTypeAndCode(final String type, final String code) {
		return persistenceEngine.retrieveByNamedQuery("FIND_ALL_BY_TYPE_AND_CODE", type, code);
	}

	@Override
	public List<ProjectionEntity> findNotDeletedProjectionEntities(final String type, final String code) {
		return persistenceEngine.retrieveByNamedQuery("FIND_ALL_BY_TYPE_AND_CODE_NOT_DELETED", type, code);
	}

	@Override
	public List<ProjectionEntity> extractProjectionsByTypeAndStoreWithPagination(final String type, final String store, final int limit,
																				 final String startAfter) {
		return persistenceEngine
				.retrieveByNamedQuery("FIND_ALL_BY_TYPE_AND_STORE_WITH_PAGINATION", new Object[]{type, store, startAfter}, 0, limit);
	}

	@Override
	public List<ProjectionEntity> extractProjectionsByTypeAndStoreWithPaginationAndModified(final String type, final String store, final int limit,
																							final String startAfter, final Date modifiedSince) {
		return persistenceEngine
				.retrieveByNamedQuery("FIND_ALL_BY_TYPE_AND_STORE_WITH_PAGINATION_AND_MODIFIED",
					new Object[]{type, store, startAfter, modifiedSince}, 0, limit);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ProjectionEntity> findLatestProjectionsWithCodes(final String type, final String store, final List<String> codes) {
		return persistenceEngine
				.retrieveByNamedQueryWithList("FIND_LATEST_PROJECTIONS_WITH_CODES_FROM_LIST",
					LIST_PARAMETER_NAME, codes, type, store);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ProjectionEntity> findLatestProjectionsWithCodes(final String type, final List<String> codes) {
		return persistenceEngine
				.retrieveByNamedQueryWithList("FIND_LATEST_PROJECTIONS_WITH_CODES_FROM_LIST_FOR_ALL_STORES",
					LIST_PARAMETER_NAME, codes, type);
	}

	@Override
	public Optional<Date> extractNearestExpiredTime(final Date currentDate) {

		List<Date> result = persistenceEngine.retrieveByNamedQuery("FIND_NEAREST_EXPIRED_TIME", currentDate);
		if (CollectionUtils.isEmpty(result)) {
			return Optional.empty();
		}

		return Optional.ofNullable(result.get(0));
	}

	@Override
	public int deleteAllProjectionsInBatchByType(final String type) {
		List<String> projectionGuidList;

		int deletedRows = 0;
		int exceptionCounter = 0;
		do {
			projectionGuidList = getProjectionIdsByType(exceptionCounter * BATCH_SIZE, type);

			if (projectionGuidList.isEmpty()) {
				break;
			}

			try {
				deletedRows += deleteProjectionsById(projectionGuidList);
			} catch (Exception ex) {
				exceptionCounter++;
				LOGGER.error("Caught an exception trying to delete projections!", ex);
			}

			if (exceptionCounter >= MAX_ERRORS) {
				throw new EpPersistenceException("Max errors reached when trying to delete projections!");
			}
		} while (!projectionGuidList.isEmpty());

		return deletedRows;
	}


	private List<String> getProjectionIdsByType(final int startRow, final String type) {
		if (type == null) {
			throw new EpSystemException("type must not be null");
		}

		return persistenceEngine.retrieveByNamedQuery(
				"FIND_CATALOG_PROJECTION_GUIDS_BY_TYPE", new Object[] { type }, startRow, BATCH_SIZE
		);
	}

	private int deleteProjectionsById(final List<String> projectionGuidList) {
		if (!projectionGuidList.isEmpty()) {
			return persistenceEngine.executeNamedQueryWithList(
					"DELETE_CATALOG_PROJECTION_BY_PROJECTION_GUID", LIST_PARAMETER_NAME, projectionGuidList
			);
		}

		return 0;
	}
}