/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.catalogview.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test pagination service.
 */
public class PaginationServiceImplTest {

	private static final int NUM_RESULTS_0 = 0;

	private static final int PAGENUMBER_0 = 0;

	private static final int PAGENUMBER_4 = 4;

	private static final int PAGENUMBER_3 = 3;

	private static final int NUM_RESULTS_67 = 67;

	private static final int PAGINATION_20 = 20;

	private static final int NUM_RESULTS_60 = 60;

	private final PaginationServiceImpl paginationService = new PaginationServiceImpl();

	/**
	 * Calculate last page number when results even.
	 */
	@Test
	public void testGetLastPageNumberEven() {

		int pageNumber = paginationService.getLastPageNumber(NUM_RESULTS_60, PAGINATION_20);
		assertEquals(PAGENUMBER_3, pageNumber);

	}

	/**
	 * Calculate last page number when results odd.
	 */
	@Test
	public void testGetLastPageNumberOdd() {

		int pageNumber = paginationService.getLastPageNumber(NUM_RESULTS_67, PAGINATION_20);
		assertEquals(PAGENUMBER_4, pageNumber);

	}

	/**
	 * Calculate last page number when results zero.
	 */
	@Test
	public void testGetLastPageNumberZero() {

		int pageNumber = paginationService.getLastPageNumber(NUM_RESULTS_0, PAGINATION_20);
		assertEquals(PAGENUMBER_0, pageNumber);

	}
}
