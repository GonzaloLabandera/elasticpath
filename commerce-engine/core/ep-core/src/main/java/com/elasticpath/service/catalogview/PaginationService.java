/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.catalogview;

/**
 * Paginates.
 */
public interface PaginationService {

	/**
	 * Calculate last page number.
	 * @param numberOfResults are the number of results returned
	 * @param pageSize the number of results per page
	 * @return the last page number
	 */
	int getLastPageNumber(int numberOfResults, int pageSize);
}
