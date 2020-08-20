/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.misc.impl;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.api.PersistenceSession;
import com.elasticpath.persistence.api.Query;
import com.elasticpath.service.misc.TimeService;

/**
 * Provides an implementation of <code>TimeService</code> to retrieve a timestamp from the database server.
 */
public class DatabaseServerTimeServiceImpl implements TimeService {

	private static final Logger LOG = Logger.getLogger(DatabaseServerTimeServiceImpl.class);
	private static final String TIME_RETRIEVE_QUERY = "SELECT LOCALTIMESTAMP FROM DUAL";

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
		final Query<Date> query = session.createSQLQuery(TIME_RETRIEVE_QUERY);

		final List<Date> results = query.list();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Got " + results.size() + " rows for time query: " + TIME_RETRIEVE_QUERY);
		}

		if (results.isEmpty()) {
			throw new EpSystemException("No result returned.");
		}

		// Get the current date/time from the first result
		return results.get(0);
	}

	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}
}
