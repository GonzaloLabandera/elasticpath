/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.impl;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openjpa.persistence.jdbc.IsolationLevel;

import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * This is class is used to validate db connection transaction isolation level.
 * The level must be READ_COMMITTED otherwise an exception is thrown.
 */
public class DbTxIsolationValidator {
	private static final Logger LOG = LogManager.getLogger(DbTxIsolationValidator.class);

	private PersistenceEngine persistenceEngine;

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * The main method for checking db TX isolation level.
	 * It's called from the Spring bean defintion in openjpa.xml
	 *
	 * @throws SQLException the exception
	 */
	public void verifyTxIsolationIsReadCommitted() throws SQLException {
		if (persistenceEngine.getSessionFactory() != null) {
			LOG.info("Verifying database transaction isolation ...");

			try (Connection connection = persistenceEngine.getConnection()) {

				IsolationLevel connectionTxIsolation = IsolationLevel.fromConnectionConstant(connection.getTransactionIsolation());

				if (connectionTxIsolation != IsolationLevel.READ_COMMITTED) {
					String errorMessage = "The database transaction isolation must be READ_COMMITTED but it is "
						+ connectionTxIsolation.name();

					LOG.error(errorMessage);
					throw new IllegalStateException(errorMessage);
				}
			}

			LOG.info("Database transaction isolation is READ_COMMITTED");
		}
	}
}
