/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.misc.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.Query;
import com.elasticpath.persistence.openjpa.support.JPAUtil;
import com.elasticpath.service.misc.TimeService;

/**
 * Provides an implementation of <code>TimeService</code> to retrieve a timestamp from the database server.
 */
public class DatabaseServerTimeServiceImpl implements TimeService {

	private static final Logger LOG = LogManager.getLogger(DatabaseServerTimeServiceImpl.class);
	/**
	 * Non-Postgresql query for retrieving db time.
	 */
	protected static final String TIME_RETRIEVE_QUERY = "SELECT LOCALTIMESTAMP FROM DUAL";
	/**
	 * Postgresql query for retrieving db time.
	 */
	protected static final String POSTGRESQL_TIME_RETRIEVE_QUERY = "SELECT LOCALTIMESTAMP";

	private String databaseType;
	private PersistenceEngine persistenceEngine;

	/**
	 * Returns the current date and time from the database.
	 *
	 * @return the current date and time from the database.
	 */
	@Override
	public Date getCurrentTime() {
		Date now;
		try {
			now = executeTimeQuery();
		} catch (final Exception e) {
			LOG.warn("Could not retrieve db server time!", e);
			LOG.warn("Will use application server timestamp instead.");
			now = new Date();
		}

		// WARNING: Do no close the session in this method, since this service may
		// get injected in other services which still need to use the session.
		// This won't cause a session leak, since this service is always in a transaction proxy.

		return now;
	}

	/**
	 * Return current database time.
	 *
	 * @return the current database time.
	 */
	protected Date executeTimeQuery() {
		final PersistenceSession session = getPersistenceEngine().getPersistenceSession();
		final Query<Object> query = session.createSQLQuery(getTimeRetrieveQuery());

		final List<Object> results = query.list();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Got " + results.size() + " rows for time query: " + getTimeRetrieveQuery());
		}

		if (results.isEmpty()) {
			throw new EpSystemException("No result returned.");
		}

		// Get the current date/time from the first result
		Object objDate = results.get(0);
		if (objDate instanceof LocalDateTime) {
			return Date.from(((LocalDateTime) objDate).atZone(ZoneId.systemDefault()).toInstant());
		}

		return (Date) objDate;
	}

	/**
	 * Gets the type of database that is currently being used by the persistence engine.
	 *
	 * @return The database type returned from the database connection.
	 */
	private String getDatabaseType() {
		// Get the database type from the database connection if it has not yet been set
		if (databaseType == null) {
			databaseType = JPAUtil.getDatabaseType(getPersistenceEngine());
		}

		return databaseType;
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * Get the time query baased on db type.
	 *
	 * @return the time query
	 */
	protected String getTimeRetrieveQuery() {
		String currentDatabaseType = getDatabaseType();
		if (JPAUtil.POSTGRESQL_DB_TYPE.equals(currentDatabaseType)) {
			return POSTGRESQL_TIME_RETRIEVE_QUERY;
		}

		return TIME_RETRIEVE_QUERY;
	}
}
