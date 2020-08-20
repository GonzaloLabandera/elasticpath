/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.persistence.impl;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.createDynamicSQLQuery;

import java.util.List;

import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.TestPersistenceEngine;
import com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl;

/**
 * This persistence engine is used in integration tests.
 * It provides additional methods for running native queries.
 */
public class TestPersistenceEngineImpl extends JpaPersistenceEngineImpl implements TestPersistenceEngine {
	private static final Logger LOG = LoggerFactory.getLogger(TestPersistenceEngineImpl.class);

	@Override
	public <T> List<T> retrieveNative(final String nativeQueryStr, final Object... parameters) {
		OpenJPAQuery<T> nativeQuery = createDynamicSQLQuery(getEntityManager(), nativeQueryStr);
		nativeQuery.setParameters(parameters);

		return nativeQuery.getResultList();
	}

	@Override
	public int executeNativeQuery(final String dynamicQuery, final Object... parameters) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Executing dynamic query {}", dynamicQuery);
		}

		OpenJPAQuery<?> query = OpenJPAPersistence.cast(getEntityManager().createNativeQuery(dynamicQuery));

		query.setParameters(parameters);

		return query.executeUpdate();
	}
}
