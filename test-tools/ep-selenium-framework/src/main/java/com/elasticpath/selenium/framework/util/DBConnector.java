package com.elasticpath.selenium.framework.util;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * DBConnector class.
 */
public class DBConnector {

	Statement statement;
	ResultSet resultSet;
	Connection connection;
	PreparedStatement preparedStatement;
	PropertyManager propertyManager = PropertyManager.getInstance();
	private static final Logger LOGGER = Logger.getLogger(DBConnector.class);

	/**
	 * Creates connection.
	 *
	 * @return connection the connection
	 */
	public Connection getDBConnection() {
		String dbUrl = propertyManager.getProperty("db.connection.url");
		String dbClass = propertyManager.getProperty("db.connection.driver.class");
		String dbUser = propertyManager.getProperty("db.connection.username");
		String dbPwd = propertyManager.getProperty("db.connection.password");

		try {
			java.lang.Class.forName(dbClass);
			connection = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Error occurred", e);
		} catch (SQLException e) {
			LOGGER.error("Error occurred", e);
		}
		return connection;
	}

	/**
	 * Excute query.
	 *
	 * @param query the query
	 * @return result set or null
	 */
	public String executeQuery(final String query) {
		assert (!query.isEmpty());
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			resultSet.next();
			return resultSet.getString(1);
		} catch (SQLException e) {
			LOGGER.error("Error occurred:", e);
		} finally {
			this.closeAll();
		}
		return null;
	}

	/**
	 * Executes update query.
	 *
	 * @param query the query
	 */
	public void executeUpdateQuery(final String query) {
		assert (!query.isEmpty());

		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			int result = statement.executeUpdate(query);
			assertTrue("Failed to update/insert/delete record in the database", result >= 0);
		} catch (SQLException e) {
			LOGGER.error("Error occurred:", e);
		} finally {
			this.closeAll();
		}
	}

	/**
	 * Closes all.
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
			LOGGER.error("Error occurred:", e);
		}
	}
}
