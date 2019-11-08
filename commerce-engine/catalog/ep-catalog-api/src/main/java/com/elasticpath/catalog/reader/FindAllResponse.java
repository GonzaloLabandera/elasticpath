/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader;

import java.time.ZonedDateTime;
import java.util.List;

import com.elasticpath.catalog.entity.Projection;

/**
 * Represents respond of all projections request.
 *
 * @param <T> - type of projection.
 */
public interface FindAllResponse<T extends Projection> {

	/**
	 * Return PaginationResponse.
	 *
	 * @return {@link PaginationResponse}.
	 */
	PaginationResponse getPagination();

	/**
	 * Return currentDateTime.
	 *
	 * @return {@link ZonedDateTime}.
	 */
	ZonedDateTime getCurrentDateTime();

	/**
	 * Return list of projections.
	 *
	 * @return {@link Projection}.
	 */
	List<T> getResults();
}
