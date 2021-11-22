/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.util;

import java.sql.Connection;
import java.util.function.Supplier;

/**
 * Utility class for determining data shape.
 */
public class DataShapeUtil {
	private final Connection connection;

	/**
	 * Constructor.
	 * @param connection the database connection
	 */
	public DataShapeUtil(final Connection connection) {
		this.connection = connection;
	}

	/**
	 * Get the size of the specified table in kilobytes.
	 * @param tableName the table name
	 * @return the size in kb
	 */
	public Supplier<Integer> getTableSizeKb(final String tableName) {
		return () -> DatabaseUtil.getTableSizeInKb(connection, tableName);
	}

	/**
	 * Get a count of rows in the specified table.
	 * @param tableName the table name
	 * @return the count of rows
	 */
	public Supplier<Integer> getTableRowCount(final String tableName) {
		return () -> DatabaseUtil.getDatabaseInt(connection, "SELECT COUNT(*) FROM " + tableName + " ;", 1);
	}

	/**
	 * Get a count of rows in the specified table that match the where clause.
	 * @param tableName the table name
	 * @param whereClause the where clause to filter the results
	 * @return the count of rows
	 */
	public Supplier<Integer> getTableRowCount(final String tableName, final String whereClause) {
		return () -> DatabaseUtil.getDatabaseInt(connection, "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause + " ;", 1);
	}
}
