/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.service.impl;

import java.util.Date;
import java.util.List;

import com.elasticpath.insights.domain.RevenueDto;
import com.elasticpath.insights.service.NativeDatabaseQueryService;
import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Service for retrieving data by running native queries in postgresql.
 */
public class PostgreSQLNativeDatabaseQueryServiceImpl implements NativeDatabaseQueryService {
	private static final String REVENUE_SINCE_DATE = "REVENUE_SINCE_DATE";

	private PersistenceEngine persistenceEngine;

	@Override
	public List<RevenueDto> getRevenueSinceDate(final Date asOfDate) {
		return persistenceEngine.retrieveByNamedNativeQuery(REVENUE_SINCE_DATE, RevenueDto.class, asOfDate);
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}
}
