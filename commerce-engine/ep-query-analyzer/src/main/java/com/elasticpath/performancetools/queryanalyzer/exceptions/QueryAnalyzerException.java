/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performancetools.queryanalyzer.exceptions;

/**
 * Custom exception.
 */
public class QueryAnalyzerException extends RuntimeException {
	/** Serial version id. */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Custom constructor.
	 * @param errMessage the exception
	 */
	public QueryAnalyzerException(final String errMessage) {
		super(errMessage);
	}

	/**
	 * Custom constructor.
	 *
	 * @param errMessage QueryAnalyzerException
	 * @param throwable the exception
	 */
	public QueryAnalyzerException(final String errMessage, final Throwable throwable) {
		super(errMessage, throwable);
	}
}
