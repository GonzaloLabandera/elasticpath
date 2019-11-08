/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader;

/**
 * Represents pagination object for future projections request.
 */
public interface NextPage {
	/**
	 * Return limit for result.
	 *
	 * @return value for limit.
	 */
	int getLimit();

	/**
	 * Return startAfter for result.
	 *
	 * @return value for startAfter.
	 */
	String getStartAfter();

	/**
	 * Return hasMoreResults for result.
	 *
	 * @return value for hasMoreResults.
	 */
	boolean isHasMoreResults();
}
