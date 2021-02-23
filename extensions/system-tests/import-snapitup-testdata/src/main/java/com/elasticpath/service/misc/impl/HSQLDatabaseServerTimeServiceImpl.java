/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.misc.impl;

/**
 * HSQL-specific implementation of <code>TimeService</code> to retrieve a timestamp from the database server.
 * The default query, "SELECT LOCALSTAMP FROM DUAL doesn't work with HSQL and it's replaced with
 * "SELECT CURRENT_TIMESTAMP FROM JPA_GENERATED_KEYS WHERE ID='DEFAULT'" in this implementation.
 *
 */
public class HSQLDatabaseServerTimeServiceImpl extends DatabaseServerTimeServiceImpl {

	private static final String HSQL_TIME_RETRIEVE_QUERY = "SELECT CURRENT_TIMESTAMP FROM JPA_GENERATED_KEYS WHERE ID='DEFAULT'";

	@Override
	protected String getTimeRetrieveQuery() {
		return HSQL_TIME_RETRIEVE_QUERY;
	}
}
