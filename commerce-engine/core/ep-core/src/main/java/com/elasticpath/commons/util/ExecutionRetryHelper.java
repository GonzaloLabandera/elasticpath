/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper utility to retry if the method execution result in an exception.
 */
public final class ExecutionRetryHelper {
	private static final Logger LOG = LogManager.getLogger(ExecutionRetryHelper.class);

	/**
	 * Default constructor.
	 */
	private ExecutionRetryHelper() {
		// do nothing.
	}

	/**
	 *
	 * Executes the provided supplier for given number of iterations if the earlier executions results in exception.
	 *
	 * @param retriable Supplier method that needs to be executed
	 * @param retryCount number of times the retriable is to be executed if earlier execution results in exception
	 * @param operation name of operation being executed used for logging purposes
	 * @param <R> return type of the retriable
	 * @param exceptionCallBack call back method to be executed when retry count is exhausted
	 *
	 * @return value returned by retriable
	 */
	public static <R> R withRetry(final Supplier<R> retriable, final int retryCount, final String operation,
			final Consumer<Exception> exceptionCallBack) {
		int count = 0;
		while (true) {
			try {
				return retriable.get();
			} catch (RuntimeException exception) {
				if (++count > retryCount) {
					exceptionCallBack.accept(exception);
				}
			}
			LOG.debug("Retrying {}, attempt {}", operation, count);
		}
	}
}
