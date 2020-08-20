/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.catalogview.impl;

import com.elasticpath.service.catalogview.PaginationService;

/**
 * Pagination service.
 */
public class PaginationServiceImpl implements PaginationService {
	@Override
	public int getLastPageNumber(final int numberOfResults, final int pageSize) {
		int maxNumPages = numberOfResults / pageSize;

		if (numberOfResults % pageSize != 0) {
			maxNumPages += 1;
		}
		return maxNumPages;
	}
}
