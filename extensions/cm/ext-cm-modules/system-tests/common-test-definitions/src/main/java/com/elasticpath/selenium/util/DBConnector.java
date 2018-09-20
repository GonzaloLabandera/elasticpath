package com.elasticpath.selenium.util;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.elasticpath.cucumber.definitions.PerformanceDefinitions;
import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * DBConnector.
 */
public class DBConnector {
	private static final Logger LOGGER = Logger.getLogger(PerformanceDefinitions.class);
	private Statement statement;
	private ResultSet resultSet;
	private Connection connection;
	private PreparedStatement preparedStatement;
	private final PropertyManager propertyManager = PropertyManager.getInstance();

	/**
	 * Create connection.
	 *
	 * @return the connection
	 */
	private Connection getDBConnection() {
		String dbUrl = propertyManager.getProperty("db.connection.url");
		String dbClass = propertyManager.getProperty("db.connection.driver.class");
		String dbUser = propertyManager.getProperty("db.connection.username");
		String dbPwd = propertyManager.getProperty("db.connection.password");

		try {
			Class.forName(dbClass);
			connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
		} catch (ClassNotFoundException e) {
			LOGGER.error(e);
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return connection;
	}

	/**
	 * Execute a sql query.
	 *
	 * @param query the sql query
	 */
	public void executeUpdateQuery(final String query) {
		assert (!query.isEmpty());

		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			int result = statement.executeUpdate(query);
			assertTrue("Failed to update/insert/delete record in the database", result >= 0);

		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			this.closeAll();
		}
	}

	/**
	 * Return the maximum uidpk number as per condition.
	 *
	 * @param table     The table to query
	 * @param condition The condition - ie. "UIDPK < 200000"
	 * @return uidpk
	 */
	public int getMaxUidpk(final String table, final String condition) {
		int maxId = 0;
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT Max(UIDPK) FROM " + table + " WHERE " + condition);

			while (resultSet.next()) {
				maxId = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			this.closeAll();
		}
		return maxId;
	}

	/**
	 * Close connection.
	 */
	public void closeAll() {
		try {
			if (statement != null) {
				statement.close();
			}
			if (resultSet != null) {
				resultSet.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		}
	}
}
