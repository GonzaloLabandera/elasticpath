/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.batch;

import org.springframework.batch.item.ItemProcessor;

/**
 * Writer that fails a given number of times.
 */
public class SucceedingProcessor implements ItemProcessor<Integer, String> {

	private final Exception exception;
	private final int timesToSucceedBeforeFailing;
	private int timesAlreadyRun;

	/**
	 * Constructor.
	 *
	 * @param exception                   exception to throw.
	 * @param timesToSucceedBeforeFailing times to succeed before failing.
	 */
	public SucceedingProcessor(final Exception exception, final int timesToSucceedBeforeFailing) {
		this.exception = exception;
		this.timesToSucceedBeforeFailing = timesToSucceedBeforeFailing;
	}

	@Override
	public String process(final Integer item) throws Exception {
		if (timesAlreadyRun >= timesToSucceedBeforeFailing) {
			throw exception;
		}
		timesAlreadyRun++;
		return "data frame number " + item;
	}
}