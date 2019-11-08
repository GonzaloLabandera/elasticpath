/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader.impl;

import java.util.Optional;

import com.elasticpath.catalog.reader.PaginationRequest;

/**
 * An implementation of {@link PaginationRequest}.
 */
public class PaginationRequestImpl implements PaginationRequest {
	private static final int DEFAULT_LIMIT = 10;
	private static final String DEFAULT_AFTER = "";

	private final int limit;
	private final String startAfter;

	/**
	 * Constructor.
	 *
	 * @param limit      is limit of amount of result.
	 * @param startAfter is start after value for result.
	 */
	public PaginationRequestImpl(final String limit, final String startAfter) {
		this.limit = Optional.ofNullable(limit).map(Integer::parseInt).orElse(DEFAULT_LIMIT);
		this.startAfter = Optional.ofNullable(startAfter).orElse(DEFAULT_AFTER);
	}

	/**
	 * Constructor.
	 */
	public PaginationRequestImpl() {
		this.limit = DEFAULT_LIMIT;
		this.startAfter = DEFAULT_AFTER;
	}

	/**
	 * Return limit for result.
	 *
	 * @return value for limit.
	 */
	@Override
	public int getLimit() {
		return limit;
	}

	/**
	 * Return start after for result.
	 *
	 * @return value for start after.
	 */
	@Override
	public String getStartAfter() {
		return startAfter;
	}
}