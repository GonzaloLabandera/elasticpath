/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.persistence;

import java.util.List;

import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Test persistence engine. Extends the production version and adds on top a few native methods.
 */
public interface TestPersistenceEngine extends PersistenceEngine {

	/**
	 * Execute native query.
	 *
	 * @param query the query to execute.
	 * @param parameters the parameters.
	 * @return number of updated/deleted records.
	 */
	int executeNativeQuery(String query, Object... parameters);

	/**
	 * Run native query.
	 *
	 * @param nativeQueryStr the native query.
	 * @param parameters the parameters.
	 * @param <T> the object's type to retrieve
	 * @return the list of rows.
	 */
	<T> List<T> retrieveNative(String nativeQueryStr, Object... parameters);
}
