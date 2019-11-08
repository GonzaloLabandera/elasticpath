package com.elasticpath.selenium.util;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.elasticpath.selenium.framework.util.PropertyManager;

/**
 * DB Connector class for connecting to and querying the database.
 */
public class DBConnector {

	private static final Logger LOGGER = Logger.getLogger(DBConnector.class);
	private static final String TCATEGORYLDF_TABLE = "TCATEGORYLDF";
	private static final String TPRODUCTLDF_TABLE = "TPRODUCTLDF";
	private static final String INNER_JOIN = "INNER JOIN ";
	private static final String TSTORE = "TSTORE ts ";
	private static final String TSTORE_WHERE = "WHERE ts.STORECODE = '";
	private Statement statement;
	private ResultSet resultSet;
	private Connection connection;
	private PreparedStatement preparedStatement;
	private final PropertyManager propertyManager = PropertyManager.getInstance();

	String dbUrl;
	String dbClass;
	String dbUser;
	String dbPwd;

	/**
	 * Create connection.
	 *
	 * @return the connection
	 */
	public Connection getDBConnection() {
		dbUrl = propertyManager.getProperty("db.connection.url");
		dbClass = propertyManager.getProperty("db.connection.driver.class");
		dbUser = propertyManager.getProperty("db.connection.username");
		dbPwd = propertyManager.getProperty("db.connection.password");
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
	 * PB-7016 - Create import connection.
	 *
	 * @return the connection
	 */
	public Connection getImportDBConnection() {
		dbUrl = propertyManager.getProperty("import.db.connection.url");
		dbClass = propertyManager.getProperty("db.connection.driver.class");
		dbUser = propertyManager.getProperty("export.import.db.connection.username");
		dbPwd = propertyManager.getProperty("export.import.db.connection.password");

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
	 * Create export db connection.
	 *
	 * @return the connection
	 */
	public Connection getExportDBConnection() {
		dbUrl = propertyManager.getProperty("export.db.connection.url");
		dbClass = propertyManager.getProperty("db.connection.driver.class");
		dbUser = propertyManager.getProperty("export.import.db.connection.username");
		dbPwd = propertyManager.getProperty("export.import.db.connection.password");

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
			connection.setAutoCommit(true);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();
			int result = statement.executeUpdate(query);
			assertTrue("Failed to update/insert/delete record in the database", result >= 0);

		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				LOGGER.error(e1);
			}
			LOGGER.error(e);
			fail("Failed to update/insert/delete record in the database: " + e.toString());
		} finally {
			this.closeAll();
		}
	}

	public ResultSet executeQuery(final String query) {
		assertThat(query).isNotEmpty();

		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			assertThat(resultSet).isNotNull();

		} catch (SQLException e) {
			LOGGER.error(e);
			fail(" " + e.toString());
			this.closeAll();
		}
		return resultSet;
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
	 * Return the name of all stores.
	 *
	 * @return stores
	 */
	public List<String> getAllStores() {
		List<String> stores = new ArrayList<>();
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT NAME FROM TSTORE WHERE STORE_STATE != 0 ORDER BY NAME");

			while (resultSet.next()) {
				stores.add(resultSet.getString("NAME"));
			}

		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			this.closeAll();
		}
		return stores;
	}

	/**
	 * Return UID of a category.
	 *
	 * @param categoryName a category name
	 * @return Uid of a given category
	 */
	public int getCategoryUid(final String categoryName) {
		return getUid(categoryName, TCATEGORYLDF_TABLE, "CATEGORY_UID");
	}

	/**
	 * Return UID of a product.
	 *
	 * @param productName a product name
	 * @return Uid of a given product
	 */
	public int getProductUid(final String productName) {
		return getUid(productName, TPRODUCTLDF_TABLE, "PRODUCT_UID");
	}

	/**
	 * PB-7016 - Return true if product exists else false.
	 *
	 * @param productCode the product code
	 * @return boolean true or false
	 */
	public boolean productExists(final String productCode) {
		boolean productExists = false;
		try {
			connection = this.getImportDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery("SELECT * FROM TPRODUCT WHERE CODE='" + productCode + "'");

			if (resultSet.next()) {
				productExists = true;
			}
		} catch (SQLException e) {
			LOGGER.error(e);
			fail("Failed to get product by code: " + productCode + " :" + e.toString());
		} finally {
			this.closeAll();
		}
		return productExists;
	}

	/**
	 * Return UID of a given entity.
	 *
	 * @param entityName  an entity name
	 * @param table       table for search
	 * @param columnLabel column label where uid is stored
	 * @return uid of a given entity or <code>0<code/> if the entity was not found
	 */
	private int getUid(final String entityName, final String table, final String columnLabel) {
		int uid = 0;
		String query = "SELECT " + columnLabel + " FROM " + table + " WHERE DISPLAY_NAME = '" + entityName + "';";
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				uid = resultSet.getInt(columnLabel);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
			fail("Failed to get " + columnLabel + " :" + e.toString());
		} finally {
			this.closeAll();
		}
		return uid;
	}

	/**
	 * Deletes a store.
	 *
	 * @param storeCode a store code of a store to be deleted
	 */
	public void deleteStore(final String storeCode) {

		executeUpdateQuery(
				"DELETE tsppc "
						+ "FROM TSTOREPAYMENTPROVIDERCONFIG tsppc "
						+ INNER_JOIN + TSTORE
						+ "ON tsppc.STORE_UID = ts.UIDPK "
						+ TSTORE_WHERE + storeCode + "';"
		);

		executeUpdateQuery(
				"DELETE tsw "
						+ "FROM TSTOREWAREHOUSE tsw "
						+ INNER_JOIN + TSTORE
						+ "ON tsw.STORE_UID = ts.UIDPK "
						+ TSTORE_WHERE + storeCode + "';"
		);

		executeUpdateQuery(
				"DELETE tssl "
						+ "FROM TSTORESUPPORTEDLOCALE tssl "
						+ INNER_JOIN + TSTORE
						+ "ON tssl.STORE_UID = ts.UIDPK "
						+ TSTORE_WHERE + storeCode + "';"
		);

		executeUpdateQuery(
				"DELETE tssc "
						+ "FROM TSTORESUPPORTEDCURRENCY tssc "
						+ INNER_JOIN + TSTORE
						+ "ON tssc.STORE_UID = ts.UIDPK "
						+ TSTORE_WHERE + storeCode + "';"
		);

		executeUpdateQuery("DELETE FROM TSTORE WHERE STORECODE = '" + storeCode + "';");
	}

	/**
	 * Saves an Enable enableDate and updates Last modified date to trigger reindexing.
	 *
	 * @param productCode product code for an entity under update
	 * @param enableDate  a product's new enable enableDate
	 */
	public void updateProductEnableDate(final String productCode, final String enableDate, final String lastUpdate) {
		executeUpdateQuery("UPDATE TPRODUCT SET START_DATE='" + enableDate + "', LAST_MODIFIED_DATE = '" + lastUpdate
				+ "' WHERE CODE = '" + productCode + "';");
	}


	public void updateChangeSetFalg(){
		connection = getImportDBConnection();
		executeImportExportUpdateQuery(connection,
				"UPDATE TSETTINGDEFINITION SET DEFAULT_VALUE='true' WHERE PATH='COMMERCE/SYSTEM/CHANGESETS/enable'");
	}

	public void updateSearchUrl(){
		connection = getExportDBConnection();
		executeImportExportUpdateQuery(connection,
				"UPDATE TSETTINGDEFINITION SET DEFAULT_VALUE='" + propertyManager.getProperty("search.host.url") + "' WHERE PATH='COMMERCE/SYSTEM/SEARCH/searchHost'");
	}

	/**
	 * Execute a sql query.
	 *
	 * @param query the sql query
	 */
	public void executeImportExportUpdateQuery(final Connection connection, final String query) {
		assert (!query.isEmpty());

		try {
			connection.setAutoCommit(true);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();
			int result = statement.executeUpdate(query);
			assertTrue("Failed to update/insert/delete record in the database", result >= 0);

		} catch (SQLException e) {
			try {
				if(connection != null) {
					connection.rollback();
				}
			} catch (SQLException e1) {
				LOGGER.error(e1);
			}
			LOGGER.error(e);
			fail("Failed to update/insert/delete record in the database: " + e.toString());
		} finally {
			this.closeAll();
		}
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

	protected static Logger getLOGGER() {
		return LOGGER;
	}

	protected Statement getStatement() {
		return statement;
	}

	protected void setStatement(final Statement statement) {
		this.statement = statement;
	}

	protected ResultSet getResultSet() {
		return resultSet;
	}

	protected void setResultSet(final ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	protected Connection getConnection() {
		return connection;
	}

	protected void setConnection(final Connection connection) {
		this.connection = connection;
	}

	protected PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}

	protected void setPreparedStatement(final PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	protected PropertyManager getPropertyManager() {
		return propertyManager;
	}
}
