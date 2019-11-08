/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.batch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.item.ItemProcessor;

/**
 * Writer that fails a given number of times.
 */
public class FailingProcessor implements ItemProcessor<Integer, String> {

	private final Exception exception;
	private final int timesToFail;
	private Map<Integer, Integer> timesAlreadyRunMap = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 *
	 * @param exception   exception to throw.
	 * @param timesToFail times to fail.
	 */
	public FailingProcessor(final Exception exception, final int timesToFail) {
		this.exception = exception;
		this.timesToFail = timesToFail;
	}

	@Override
	public String process(final Integer item) throws Exception {
		Integer timesAlreadyRun = timesAlreadyRunMap.get(item);
		if (timesAlreadyRun == null || timesAlreadyRun < timesToFail) {
			timesAlreadyRunMap.put(item, (timesAlreadyRun == null ? 0 : timesAlreadyRun) + 1);
			throw exception;
		}
		return "data frame number " + item;
	}
}