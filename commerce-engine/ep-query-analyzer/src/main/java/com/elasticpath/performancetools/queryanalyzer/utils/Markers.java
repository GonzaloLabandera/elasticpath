/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer.utils;

/**
 * Holder for various String markers used for searching within log file.
 */
public final class Markers {

	/**
	 * Marker for finding Cortex resource URIs.
	 */
	public static final String RELOS_RESOURCE_URI_MARKER = "RelOS Resource URI:";
	/**
	 * URI marker.
	 */
	public static final String URI_MARKER = "URI:";
	/**
	 * Marker for finding jpa queries.
	 */
	public static final String JPA_QUERY_MARKER = "Executing query:";
	/**
	 * Marker for finding jpa trace queries.
	 */
	public static final String JPA_TRACE_QUERY_MARKER = "TRACE org.apache.openjpa";
	/**
	 * Marker for finding sql queries.
	 */
	public static final String SQL_STATEMENT_MARKER = "(executing)?(batching)? (batch )?prepstmnt ";
	/** Marker for finding batch prepared statements. */
	public static final String SQL_BATCH_STATEMENT_MARKER = "executing batch prepstmnt ";
	/** Marker for finding both regular and batch prepared statements. */
	public static final String MULTI_LINE_MATCH_SQL_STATEMENT_MARKER = "(?s).*?executing (batch )?prepstmnt.*?";
	/**
	 * Marker for finding jpa eager relations.
	 */
	public static final String EAGER_RELATIONS_MARKER = "Eager relations: ";

	/**
	 * Marker for finding batched inserts.
	 */
	public static final String BATCHING_PREPSTMT_MARKER = "batching prepstmnt";

	/**
	 * Marker for finding executed batched inserts.
	 */
	public static final String EXECUTING_BATCH_PREPSTMT_MARKER = "executing batch prepstmnt";

	private Markers() {
		//constants class
	}
}
