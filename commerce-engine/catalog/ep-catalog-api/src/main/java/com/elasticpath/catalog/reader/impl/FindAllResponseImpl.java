/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.reader.impl;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.reader.FindAllResponse;
import com.elasticpath.catalog.reader.PaginationResponse;

/**
 * An implementation of {@link FindAllResponse}.
 *
 * @param <T> - type of projection.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FindAllResponseImpl<T extends Projection> implements FindAllResponse<T> {
	private final PaginationResponse pagination;
	private final ZonedDateTime currentDateTime;
	private final List<T> results;

	/**
	 * Constructor.
	 *
	 * @param pagination      is pagination for next request.
	 * @param currentDateTime is time of current request.
	 * @param results         list of {@link Projection} of Projection.
	 */
	public FindAllResponseImpl(final PaginationResponse pagination, final ZonedDateTime currentDateTime, final List<T> results) {
		this.pagination = pagination;
		this.currentDateTime = currentDateTime;
		this.results = results;
	}

	/**
	 * Return PaginationResponse.
	 *
	 * @return {@link PaginationResponse}.
	 */
	public PaginationResponse getPagination() {
		return pagination;
	}

	/**
	 * Return currentDateTime.
	 *
	 * @return {@link ZonedDateTime}.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
	public ZonedDateTime getCurrentDateTime() {
		return currentDateTime;
	}

	/**
	 * Return list of projections.
	 *
	 * @return {@link Projection}.
	 */
	public List<T> getResults() {
		return results;
	}
}