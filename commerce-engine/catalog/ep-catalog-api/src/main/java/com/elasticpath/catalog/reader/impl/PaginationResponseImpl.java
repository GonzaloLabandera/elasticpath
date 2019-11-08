/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader.impl;

import com.elasticpath.catalog.reader.NextPage;
import com.elasticpath.catalog.reader.PaginationResponse;

/**
 * An implementation of {@link PaginationResponse}.
 */
public class PaginationResponseImpl implements PaginationResponse {
	private final NextPage next;

	/**
	 * Constructor.
	 *
	 * @param limit          is limit of amount of result.
	 * @param startAfter     is start after value for result.
	 * @param hasMoreResults indicates if more results exist.
	 */
	public PaginationResponseImpl(final int limit, final String startAfter, final boolean hasMoreResults) {
		this.next =  new NextPageImpl(limit, startAfter, hasMoreResults);
	}

	/**
	 * Return next page pagination.
	 *
	 * @return {@link NextPage}.
	 */
	@Override
	public NextPage getNext() {
		return next;
	}
}
