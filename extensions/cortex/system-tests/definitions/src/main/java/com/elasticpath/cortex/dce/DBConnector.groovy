package com.elasticpath.cortex.dce

import static org.junit.Assert.assertTrue

import java.sql.*

import org.apache.log4j.Logger

/**
 * DB Connector class for connecting to and querying the database.
 */
class DBConnector {

	private static final Logger LOGGER = Logger.getLogger(DBConnector.class);
	private Statement statement;
	private ResultSet resultSet;
	private Connection connection;
	private PreparedStatement preparedStatement;
	private final PropertyManager propertyManager = PropertyManager.getInstance();
	private final String dbClass = propertyManager.getProperty("db.connection.driver.class"); ;

	/**
	 * Create connection.
	 *
	 * @return the connection
	 */
	Connection getDBConnection() {
		String dbUrl = propertyManager.getProperty("db.connection.url");
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
	 * @param table the table to query
	 * @return uidpk    the uidpk
	 */
	def getMaxUidpk(final String table) {
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT Max(UIDPK) FROM " + table + " WHERE UIDPK < 10000");

			while (resultSet.next()) {
				return resultSet.getInt(1);
			}

		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			this.closeAll();
		}
		return null
	}

	def getWarehouseUidpk(final String warehouseName) {

		try {
			connection = this.getDBConnection();
			Statement stmt = connection.createStatement()
			resultSet = stmt.executeQuery("SELECT UIDPK FROM TWAREHOUSE WHERE NAME = '" + warehouseName + "'")

			while (resultSet.next()) {
				return resultSet.getInt(1)
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.closeAll();
		}

		return null
	}

	/**
	 * Returns the quantity on hand.
	 *
	 * @parm skuCode            the skucCode
	 * @return quantityOnHand    the quantity on hand
	 */
	def getQuantityOnHand(final String skuCode, final int warehouseUidpk) {
		def quantityOnHand = null
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT SUM(QUANTITY_ON_HAND_DELTA) - SUM(ALLOCATED_QUANTITY_DELTA) FROM TINVENTORYJOURNAL WHERE SKUCODE='"
					+ skuCode + "' AND WAREHOUSE_UID=" + warehouseUidpk);

			while (resultSet.next()) {
				quantityOnHand = resultSet.getInt(1)
			}

		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			this.closeAll();
		}

		return quantityOnHand
	}

	/**
	 * Sets available quantity.
	 *
	 * @param skuCode the sku code
	 * @param warehouseName the warehouse name
	 * @param expectedQuantity the expected quantity
	 */
	void setAvailableQuantity(final String skuCode, final String warehouseName, int expectedQuantity) {
		if (!dbClass.contains("h2")) {
			def warehouseUidpk = getWarehouseUidpk(warehouseName)
			def qtyOnHand = getQuantityOnHand(skuCode, warehouseUidpk)

			if (qtyOnHand != expectedQuantity) {
				LOGGER.info("resetting inventory for sku: " + skuCode)

				//adjusts the quantity to the expected value, it may be positive for adding or negative for removing inventory
				def adjustedQuantity = expectedQuantity - qtyOnHand

				def uidpk = getMaxUidpk("TINVENTORYJOURNAL")

				if (uidpk == null || uidpk == 0) {
					uidpk = 100
				} else {
					uidpk = uidpk + 1
				}
				connection = this.getDBConnection();
				try {
					preparedStatement = connection.prepareStatement(
							"INSERT INTO TINVENTORYJOURNAL (UIDPK, ALLOCATED_QUANTITY_DELTA, QUANTITY_ON_HAND_DELTA, SKUCODE, WAREHOUSE_UID)"
									+ " VALUES (?, ?, ?, ?, ?)"
					);
					preparedStatement.setInt(1, uidpk)
					preparedStatement.setInt(2, 0)
					preparedStatement.setInt(3, adjustedQuantity)
					preparedStatement.setString(4, skuCode)
					preparedStatement.setInt(5, warehouseUidpk)
					preparedStatement.executeUpdate()
				} catch (SQLException e) {
					LOGGER.error(e);
				} finally {
					this.closeAll();
				}
			}
		}
	}

	/**
	 * Close connection.
	 */
	void closeAll() {
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
