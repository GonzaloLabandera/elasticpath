/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
 * Custom liquibase change that adds guid into the rows of TCATALOGPROJECTIONS table.
 */
public class AddGuidToProjectionTable implements CustomTaskChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final int BATCH_SIZE = 1000;

	private static final int TYPE_INDEX = 1;
	private static final int STORE_INDEX = 2;
	private static final int CODE_INDEX = 3;

	private static final int GUID_INDEX = 1;

	// Only grab rows with null values.
	private static final String SELECT_ROWS_WITH_EMPTY_GUID =
			"SELECT TYPE, STORE, CODE FROM TCATALOGPROJECTIONS WHERE GUID IS NULL";

	private static final String UPDATE_GUID_STATEMENT = "UPDATE TCATALOGPROJECTIONS SET GUID = ? WHERE TYPE = ? AND STORE = ? AND CODE = ?";

	@Override
	public void execute(Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();

		while (true) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ROWS_WITH_EMPTY_GUID)) {
				preparedStatement.setMaxRows(BATCH_SIZE);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						addGuidByBatch(connection, resultSet);
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

				throw new CustomChangeException("An error occurred while updating TCATALOGPROJECTIONS records", e);
			}
		}
	}

	private void addGuidByBatch(final JdbcConnection connection, final ResultSet resultSet) throws SQLException, DatabaseException {
		try (PreparedStatement addGuid = connection.prepareStatement(UPDATE_GUID_STATEMENT)) {
			do {
				String type = resultSet.getString(TYPE_INDEX);
				String store = resultSet.getString(STORE_INDEX);
				String code = resultSet.getString(CODE_INDEX);

				addGuid.setString(GUID_INDEX, UUID.randomUUID().toString());
				addGuid.setString(TYPE_INDEX + GUID_INDEX, type);
				addGuid.setString(STORE_INDEX + GUID_INDEX, store);
				addGuid.setString(CODE_INDEX + GUID_INDEX, code);
				addGuid.addBatch();
			} while (resultSet.next());
			addGuid.executeBatch();
			addGuid.clearBatch();

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
}
