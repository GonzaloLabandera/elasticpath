/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.util;

import java.util.Collection;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl;

/**
 * Stubbed persistence engine that throws an exception when a certain DML is executed with a specific parameter.
 */
public final class FailingJpaPersistenceEngine extends JpaPersistenceEngineImpl {
	private final long uidToFailFor;
	private final String namedQueryToFailFor;

	/**
	 * Custom constructor.
	 *
	 * @param autoWiredPersistenceEngine the auto-wired persistence engine (from openjpa.xml)
	 * @param uidToFailFor the entity ID (e.g. cart or shipment UidPk) to match and throw exception for.
	 * @param namedQueryToFailFor the named query to match and and throw exception for.
	 */
	public FailingJpaPersistenceEngine(final JpaPersistenceEngineImpl autoWiredPersistenceEngine, final long uidToFailFor,
									   final String namedQueryToFailFor) {

		setEntityManager(autoWiredPersistenceEngine.getEntityManager());
		setSessionFactory(autoWiredPersistenceEngine.getSessionFactory());
		setTransactionManager(autoWiredPersistenceEngine.getTxManager());
		setFetchPlanHelper(autoWiredPersistenceEngine.getFetchPlanHelper());
		setQueryReaderFactory(autoWiredPersistenceEngine.getQueryReaderFactory());

		this.uidToFailFor = uidToFailFor;
		this.namedQueryToFailFor = namedQueryToFailFor;

		super.init();
	}

	@Override
	public <E> int executeNamedQueryWithList(final String queryName, final String listParameterName, final Collection<E> values,
											 final Object... parameters) {
		if (queryName.equals(namedQueryToFailFor)
				&& values.contains(uidToFailFor)) {

			throw new EpSystemException("Test error: Batch processing failed for entity ID: " + uidToFailFor);
		}

		return super.executeNamedQueryWithList(queryName, listParameterName, values, parameters);
	}
}

