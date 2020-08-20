/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import java.math.BigDecimal;
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
	private static final int PARAM_SIZE = 10;

	private static final String SELECT_ALL_ANONYMOUS_USERS = "SELECT CUSTOMER_UID FROM TCUSTOMERPROFILEVALUE "
			+ "WHERE LOCALIZED_ATTRIBUTE_KEY='CP_ANONYMOUS_CUST' AND BOOLEAN_VALUE=1";

	private static final String SELECT_ALL_REGISTERED_USERS = "SELECT CUSTOMER_UID FROM TCUSTOMERPROFILEVALUE "
			+ "WHERE LOCALIZED_ATTRIBUTE_KEY='CP_ANONYMOUS_CUST' AND BOOLEAN_VALUE=0";


	private static final String UPDATE_TO_SINGLE_SESSION_USER = "UPDATE TCUSTOMER SET CUSTOMER_TYPE='SINGLE_SESSION_USER' WHERE UIDPK IN  (%s)";
	private static final String UPDATE_TO_REGISTERED_USER = "UPDATE TCUSTOMER SET CUSTOMER_TYPE='REGISTERED_USER' WHERE UIDPK IN  (%s)";

	@Override
	public void execute(Database database) throws CustomChangeException {

		LOG.info("Starting customer migration for customer type...");

		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();

		long startTime = System.currentTimeMillis();
		executeBatchUpdate(connection, SELECT_ALL_ANONYMOUS_USERS, UPDATE_TO_SINGLE_SESSION_USER);
		executeBatchUpdate(connection, SELECT_ALL_REGISTERED_USERS, UPDATE_TO_REGISTERED_USER);

		long totalElapsedTime = System.currentTimeMillis() - startTime;
		LOG.info(String.format("Customer type migration completed. Total time: %d", totalElapsedTime));
	}

	private void executeBatchUpdate(final JdbcConnection connection, final String selectSQL, final String updateSQL) throws CustomChangeException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				String updateStatementWithParamPlaceholder = getUpdateStatementWithParameterPlaceholder(updateSQL);

				PreparedStatement updateCustomerType = connection.prepareStatement(updateStatementWithParamPlaceholder);
				int runner = 0;
				int paramRunner = 0;
				while (resultSet.next()) {

					paramRunner++;
					BigDecimal customerUid = resultSet.getBigDecimal(1);
					updateCustomerType.setBigDecimal(paramRunner, customerUid);

					if (paramRunner % PARAM_SIZE == 0) {
						paramRunner = 0;
						updateCustomerType.addBatch();
					}

					runner++;
					if (runner % DEFAULT_BATCH_SIZE == 0) {
						updateCustomerType.executeBatch();
						updateCustomerType.clearBatch();
					}
				}

				if (paramRunner % PARAM_SIZE != 0) {
					updateCustomerType.addBatch();
				}

				// Make sure all updates are executed.
				if (runner % DEFAULT_BATCH_SIZE != 0) {
					updateCustomerType.executeBatch();
				}
			}
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException("An error occurred when updating customer records", e);
		}
	}

	private String getUpdateStatementWithParameterPlaceholder(final String updateStatement) {
		StringBuilder withParams = new StringBuilder("?");

		for (int i = 1; i < PARAM_SIZE; i++) {
			withParams.append(", ?");
		}

		return String.format(updateStatement, withParams.toString());
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
}
