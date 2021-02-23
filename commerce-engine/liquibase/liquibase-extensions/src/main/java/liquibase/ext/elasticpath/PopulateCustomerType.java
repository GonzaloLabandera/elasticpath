/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

/**
 * Populate customer type in customer table.
 */
public class PopulateCustomerType implements CustomTaskChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final int DEFAULT_BATCH_SIZE = 1000;

	protected static final String SELECT_ANONYMOUS_CUSTOMERS =
			" SELECT CUSTOMER_UID "
			+ "FROM TCUSTOMER TC "
			+ "INNER JOIN TCUSTOMERPROFILEVALUE TCP ON TC.UIDPK = TCP.CUSTOMER_UID "
			+ "AND TC.CUSTOMER_TYPE IS NULL "
			+ "AND TCP.LOCALIZED_ATTRIBUTE_KEY='CP_ANONYMOUS_CUST' "
			+ "AND TCP.BOOLEAN_VALUE=1";

	protected static final String SELECT_REGISTERED_CUSTOMERS =
			" SELECT CUSTOMER_UID "
			+ "FROM TCUSTOMER TC "
			+ "INNER JOIN TCUSTOMERPROFILEVALUE TCP ON TC.UIDPK = TCP.CUSTOMER_UID "
			+ "AND TC.CUSTOMER_TYPE IS NULL "
			+ "AND TCP.LOCALIZED_ATTRIBUTE_KEY='CP_ANONYMOUS_CUST' "
			+ "AND TCP.BOOLEAN_VALUE=0";

	protected static final String UPDATE_TO_SINGLE_SESSION_USER = "UPDATE TCUSTOMER SET CUSTOMER_TYPE='SINGLE_SESSION_USER' WHERE UIDPK = ?";
	protected static final String UPDATE_TO_REGISTERED_USER = "UPDATE TCUSTOMER SET CUSTOMER_TYPE='REGISTERED_USER' WHERE UIDPK = ?";

	private int batchSize;


	/**
	 * Constructor
	 */
	public PopulateCustomerType() {
		batchSize =	DEFAULT_BATCH_SIZE;
	}

	@Override
	public void execute(Database database) throws CustomChangeException {

		LOG.info("Starting customer migration for customer type...");

		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();

		long startTime = System.currentTimeMillis();

		migrateCustomers(connection, SELECT_ANONYMOUS_CUSTOMERS, UPDATE_TO_SINGLE_SESSION_USER);
		migrateCustomers(connection, SELECT_REGISTERED_CUSTOMERS, UPDATE_TO_REGISTERED_USER);

		long totalElapsedTime = System.currentTimeMillis() - startTime;
		LOG.info(String.format("Customer type migration completed. Total time: %d", totalElapsedTime));
	}

	protected void migrateCustomers(final JdbcConnection connection, final String selectSQL, final String updateSQL) throws CustomChangeException {
		while (true) {
			try (PreparedStatement selectCustomerStatement = connection.prepareStatement(selectSQL)) {
				selectCustomerStatement.setMaxRows(getBatchSize());

					try (ResultSet customersResultSet = selectCustomerStatement.executeQuery()) {
						if (customersResultSet.next()) {
							updateCustomersBatch(connection, customersResultSet, updateSQL);
						} else {
							break;
						}
					}
			} catch (Exception e) {
				try {
					connection.rollback();
				} catch (DatabaseException dbexc) {
					LOG.severe("Can't rollback transaction", dbexc);
				}
				throw new CustomChangeException("An error occurred when updating customer records", e);
			}
		}
	}

	@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
	private void updateCustomersBatch(final JdbcConnection connection, final ResultSet customersResultSet, final String updateSQL)
			throws SQLException, DatabaseException {
		try (PreparedStatement updateCustomerStatement = connection.prepareStatement(updateSQL)) {
			connection.setAutoCommit(false);
			do {
				long customerUid = customersResultSet.getLong(1);
				updateCustomerStatement.setLong(1, customerUid);
				updateCustomerStatement.addBatch();
			} while (customersResultSet.next());
			updateCustomerStatement.executeBatch();
			updateCustomerStatement.clearBatch();
			connection.commit();
		}
	}

	@Override
	public String getConfirmationMessage() {
		return null;
	}

	@Override
	public void setUp() throws SetupException {
		// None required.
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not used.
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	private int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

}
