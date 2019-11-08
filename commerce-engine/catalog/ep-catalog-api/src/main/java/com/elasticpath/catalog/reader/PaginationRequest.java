/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader;

/**
 * Represents pagination of all projections request.
 */
public interface PaginationRequest {

	/**
	 * Return limit for result.
	 *
	 * @return value for limit.
	 */
	int getLimit();

	/**
	 * Return start after for result.
	 *
	 * @return value for start after.
	 */
	String getStartAfter();
}
