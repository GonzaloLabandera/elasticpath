/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.batch;

import org.springframework.batch.item.ItemReader;

/**
 * Mock Failing Reader issuing single object.
 */
public class MockSuccessfulReader implements ItemReader<Integer> {

	private final int itemsToReadBeforeStopping;
	private int itemsAlreadyRead;

	/**
	 * Constructor.
	 *
	 * @param itemsToReadBeforeStopping items to read before stopping.
	 */
	public MockSuccessfulReader(final int itemsToReadBeforeStopping) {
		this.itemsToReadBeforeStopping = itemsToReadBeforeStopping;
	}

	@Override
	public Integer read() {
		if (itemsAlreadyRead >= itemsToReadBeforeStopping) {
			return null;
		}
		return ++itemsAlreadyRead;
	}
}