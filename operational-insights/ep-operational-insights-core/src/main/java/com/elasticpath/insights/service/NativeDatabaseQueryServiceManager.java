/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.service;

import java.util.Map;
import java.util.Optional;

import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.support.JPAUtil;

/**
 * Context object to obtain the appropriate data service applicable for the persistence connection.
 */
public class NativeDatabaseQueryServiceManager {

	private PersistenceEngine persistenceEngine;

	private Optional<NativeDatabaseQueryService> nativeDatabaseQueryService = Optional.empty();

	private Map<String, NativeDatabaseQueryService> dataServiceMap;

	/**
	 * Initializes the value of nativeDatabaseQueryService.
	 */
	public void init() {
		String databaseType = JPAUtil.getDatabaseType(persistenceEngine);
		nativeDatabaseQueryService = Optional.ofNullable(dataServiceMap.get(databaseType));
	}

	/**
	 * Get the appropriate dataservice applicable for the persistence connection.
	 *
	 * @return applicable data service
	 */
	public Optional<NativeDatabaseQueryService> getDataService() {
		return nativeDatabaseQueryService;
 	}

	public void setDataServiceMap(final Map<String, NativeDatabaseQueryService> dataServiceMap) {
		this.dataServiceMap = dataServiceMap;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	protected Map<String, NativeDatabaseQueryService> getDataServiceMap() {
		return dataServiceMap;
	}
}
