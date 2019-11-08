/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.repository.impl;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.repository.CatalogProjectionRepositoryCustom;
import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Implementation of {@link CatalogProjectionRepositoryCustom}.
 */
public class CatalogProjectionRepositoryImpl implements CatalogProjectionRepositoryCustom {

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
}