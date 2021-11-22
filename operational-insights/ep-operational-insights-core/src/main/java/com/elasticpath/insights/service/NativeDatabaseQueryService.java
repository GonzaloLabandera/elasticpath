/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.service;

import java.util.Date;
import java.util.List;

import com.elasticpath.insights.domain.RevenueDto;

/**
 * Service for retrieving data by running native queries.
 */
public interface NativeDatabaseQueryService {

	/**
	 * Get revenue data since given date.
	 * @param asOfDate revenue data to be retrieve since date
	 * @return list of revenue dto objects.
	 */
	List<RevenueDto> getRevenueSinceDate(Date asOfDate);
}
