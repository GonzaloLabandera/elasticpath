/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader;

/**
 * Represents pagination for future projections request.
 */
public interface PaginationResponse {

	/**
	 * Return next page pagination.
	 *
	 * @return {@link NextPage}.
	 */
	NextPage getNext();
}