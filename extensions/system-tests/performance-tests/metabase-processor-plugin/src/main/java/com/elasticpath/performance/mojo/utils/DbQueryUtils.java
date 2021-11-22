/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static com.elasticpath.performance.mojo.beans.ResultBean.CUSTOMER_DEV_COMPARISON;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.upperCase;

/**
 * The util class with db query-related constants, templates and methods.
 */
@SuppressWarnings({"PMD.ConsecutiveLiteralAppends", "PMD.AvoidStringBufferField"})
public final class DbQueryUtils {
	/** H2 driver class name.*/
	public static final String H2_DRIVER_CLASS = "org.h2.Driver";
	/** The name of the metabase table holding cucumber performance results. */
	public static final String CUCUMBER_PERFORMANCE_TABLE = "cucumber_performance_results";
	/** The query for finding unique EPC versions. */
	public static final String UNIQUE_EPC_VERSIONS_QUERY = "SELECT DISTINCT epc_version FROM " + CUCUMBER_PERFORMANCE_TABLE;
	/** The H2 insert statement template.*/
	public static final StringBuilder H2_INSERT_STATEMENT_TEMPLATE = new StringBuilder();
	/** The query for verifying the existence of the 'cucumber_performance_results' table. */
	public static final String SHOW_TABLES_QUERY = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE lower(table_schema)='public' AND table_name='"
			+ CUCUMBER_PERFORMANCE_TABLE + "'";
	/** The insert statement for storing performance results into metabase. */
	public static final StringBuilder INSERT_RELEASE_DATA_INTO_METABASE_STATEMENT = new StringBuilder();

	private static final StringBuilder GET_LATEST_RELEASE_DATA_QUERY_TEMPLATE = new StringBuilder();
	private static final String METABASE_PUBLIC_SCHEMA = "public"; //default for PostgreSQL
	private static final StringBuilder EXPORT_LATEST_RESULTS_FOR_ALL_RELEASES_QUERY = new StringBuilder();


	static {
		GET_LATEST_RELEASE_DATA_QUERY_TEMPLATE
				.append("SELECT id,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates,")
				.append("total_db_deletes,total_db_time FROM \"%s\".\"%s\" WHERE ");

		EXPORT_LATEST_RESULTS_FOR_ALL_RELEASES_QUERY
				.append("SELECT date_executed,epc_version,application,cuke_scenario,")
				.append("total_db_selects,total_db_inserts,total_db_updates,total_db_deletes,total_db_time,commit_hash")
				.append(" FROM ")
				.append(CUCUMBER_PERFORMANCE_TABLE)
				.append(" WHERE jenkins_job_id = ")
				.append("(SELECT max(jenkins_job_id) FROM ")
				.append(CUCUMBER_PERFORMANCE_TABLE)
				.append(" WHERE epc_version='%s') and epc_version='%s'");

		H2_INSERT_STATEMENT_TEMPLATE
				.append("INSERT INTO \"PUBLIC\".\"")
				.append(CUCUMBER_PERFORMANCE_TABLE)
				.append("\" (date_executed,epc_version,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates")
				.append(",total_db_deletes,total_db_time,commit_hash)")
				.append(" VALUES ");

		INSERT_RELEASE_DATA_INTO_METABASE_STATEMENT
				.append("INSERT INTO ")
				.append(CUCUMBER_PERFORMANCE_TABLE)
				.append(" (jenkins_job_id, date_executed,epc_version,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates")
				.append(",total_db_deletes,total_db_time,commit_hash)")
				.append(" VALUES (?,?,?,?,?,?,?,?,?,?,?)");
	}

	/**
	 * Format the query that returns the latest release data, based on comparison type (CI PR, customer or EP developer).
	 *
	 * @param comparisonType @see {@link com.elasticpath.performance.mojo.beans.ResultBean}
	 * @param epcVersion the epc version
	 * @return the formatted query
	 */
	public static String getFormattedLatestReleaseDataQuery(final int comparisonType, final String epcVersion) {
		if (comparisonType == CUSTOMER_DEV_COMPARISON) {
			return format(GET_LATEST_RELEASE_DATA_QUERY_TEMPLATE.toString() + " epc_version='%s'", upperCase(METABASE_PUBLIC_SCHEMA),
					CUCUMBER_PERFORMANCE_TABLE, epcVersion);
		}

		String query = GET_LATEST_RELEASE_DATA_QUERY_TEMPLATE.toString()
				+ " jenkins_job_id = (SELECT max(jenkins_job_id) FROM %s WHERE epc_version='%s') and epc_version='%s'";

		return format(query, METABASE_PUBLIC_SCHEMA, CUCUMBER_PERFORMANCE_TABLE, CUCUMBER_PERFORMANCE_TABLE, epcVersion, epcVersion);
	}

	/**
	 * Formats the export query with EPC version value.
	 *
	 * @param epcVersion the epc version
	 * @return formatted export query
	 */
	public static String getFormattedExportQuery(final String epcVersion) {
		return format(EXPORT_LATEST_RESULTS_FOR_ALL_RELEASES_QUERY.toString(), epcVersion, epcVersion);
	}

	private DbQueryUtils() {
		//noop
	}
}
