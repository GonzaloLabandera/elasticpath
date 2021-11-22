/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.insights.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.exception.EpServiceException;

/**
 * Utility class for accessing database details.
 */
public final class DatabaseUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DatabaseUtil.class);
	private static final String UNABLE_TO_EXECUTE_QUERY = "Unable to execute query.";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private DatabaseUtil() {
		// No op
	}

	/**
	 * Determine if the passed query returns a result.
	 * @param connection the database connection
	 * @param query the database query
	 * @return true if the query returned a result
	 */
	public static Boolean getDatabaseResultPresent(final Connection connection, final String query) {
		if (connection != null) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					return resultSet.next();
				}
			} catch (SQLException exception) {
				LOG.error(UNABLE_TO_EXECUTE_QUERY, exception);
			}
		}
		return false;
	}

	/**
	 * Get the result of a database query.
	 * @param connection the database connection
	 * @param query the database query
	 * @param resultColumn the column number of the result to return
	 * @return the query result as a String
	 */
	public static String getDatabaseString(final Connection connection, final String query, final int resultColumn) {
		return getDatabaseStringInternal(connection, query, resultColumn);
	}

	/**
	 * Get the result of a database query.
	 * @param connection the database connection
	 * @param query the database query
	 * @param resultColumn the column number of the result to return
	 * @return the query result as a Boolean
	 */
	public static Boolean getDatabaseBoolean(final Connection connection, final String query, final int resultColumn) {
		return getDatabaseBooleanInternal(connection, query, resultColumn);
	}

	/**
	 * Get the result of a database query.
	 * @param connection the database connection
	 * @param query the database query
	 * @param resultColumn the column number of the result to return
	 * @return the query result as an Integer
	 */
	public static Integer getDatabaseInt(final Connection connection, final String query, final int resultColumn) {
		return getDatabaseIntInternal(connection, query, resultColumn);
	}

	private static String getDatabaseStringInternal(final Connection connection, final String query, final int resultColumn) {
		if (connection != null) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					if (resultSet.next()) {
						return resultSet.getString(resultColumn);
					}
					return "<not-available>";  // we didn't get a result
				}
			} catch (SQLException exception) {
				LOG.error(UNABLE_TO_EXECUTE_QUERY, exception);
			}
		}
		return "<not-available>";
	}

	private static Boolean getDatabaseBooleanInternal(final Connection connection, final String query, final int resultColumn) {
		if (connection != null) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					if (resultSet.next()) {
						return resultSet.getBoolean(resultColumn);
					}
				}
			} catch (SQLException exception) {
				LOG.error(UNABLE_TO_EXECUTE_QUERY, exception);
			}
		}
		throw new IllegalStateException("should have got a result");
	}

	private static int getDatabaseIntInternal(final Connection connection, final String query, final int resultColumn) {
		if (connection != null) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery(query)) {
					if (resultSet.next()) {
						return resultSet.getInt(resultColumn);
					}
					return -1;  // we didn't get a result
				}
			} catch (SQLException exception) {
				LOG.error(UNABLE_TO_EXECUTE_QUERY, exception);
			}
		}
		return -1;
	}

	/**
	 * Get the size of the specified table in kilobytes.
	 * @param connection the database connection
	 * @param tableName the table name
	 * @return the size in kb
	 */
	public static Integer getTableSizeInKb(final Connection connection, final String tableName) {
		return safeDb(conn -> getDatabaseIntInternal(conn, "SELECT"
				+ " ROUND(SUM(data_length + index_length) / 1024, 2) AS 'size'"
				+ " FROM information_schema.TABLES"
				+ " WHERE table_schema = '" + conn.getCatalog() + "'"
				+ " AND table_name = '" + tableName + "';", 1), connection);
	}

	/**
	 * Get the size of the entire database in kilobytes.
	 * @param connection the database connection
	 * @return the size in kb
	 */
	public static Integer getSchemaSizeInKb(final Connection connection) {
		return safeDb(conn -> getDatabaseIntInternal(conn, "SELECT "
				+ " ROUND(SUM(data_length + index_length) / 1024, 2) AS 'size'"
				+ " FROM information_schema.TABLES"
				+ " WHERE table_schema = '" + conn.getCatalog() + "';", 1), connection);
	}

	/**
	 * Interface for a supplier that throws a specified exception.
	 * @param <T> the return type
	 * @param <U> the parameter type
	 * @param <E> the exception type
	 */
	@FunctionalInterface
	public interface ThrowingSupplier<T, U, E extends Exception> {
		/**
		 * Execute the supplier function.
		 * @param input the parameter value
		 * @return the result
		 * @throws E the exception
		 */
		T exec(U input) throws E;
	}

	/**
	 * Execute the passed supplier function, converting any exceptions into RuntimeException.
	 * @param throwingSupplier the supplier that could throw an exception
	 * @param input the parameter value
	 * @param <T> the return type
	 * @param <U> the parameter type
	 * @return the result
	 */
	@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes"})
	public static <T, U> T safeDb(final ThrowingSupplier<T, U, Exception> throwingSupplier, final U input) {
		try {
			return throwingSupplier.exec(input);
		} catch (Exception ex) {
			throw new EpServiceException("Error during database operation", ex);
		}
	}
}
