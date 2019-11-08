/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader.impl;

import com.elasticpath.catalog.reader.NextPage;

/**
 * An implementation of {@link NextPage}.
 */
public class NextPageImpl implements NextPage {

	private final int limit;
	private final String startAfter;
	private final boolean hasMoreResults;

	/**
	 * Constructor.
	 *
	 * @param limit          is limit of amount of result.
	 * @param startAfter     is start after value for result.
	 * @param hasMoreResults indicates if more results exist.
	 */
	NextPageImpl(final int limit, final String startAfter, final boolean hasMoreResults) {
		this.limit = limit;
		this.startAfter = startAfter;
		this.hasMoreResults = hasMoreResults;
	}

	/**
	 * Return limit for result.
	 *
	 * @return value for limit.
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * Return startAfter for result.
	 *
	 * @return value for startAfter.
	 */
	public String getStartAfter() {
		return startAfter;
	}

	/**
	 * Return hasMoreResults for result.
	 *
	 * @return value for hasMoreResults.
	 */
	public boolean isHasMoreResults() {
		return hasMoreResults;
	}
}