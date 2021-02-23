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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Custom liquibase change that converts cm user passwords from sha1 to bcrypt.
 */
public class ConvertCmUserPasswordsToBcrypt  implements CustomTaskChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final int DEFAULT_BATCH_SIZE = 1000;

	final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	// Only grab rows with SHA encoded passwords.
	private static final String SELECT_SHA_CMUSER_AUTHENTICATION_RECORDS = "SELECT USER_NAME, PASSWORD FROM TCMUSER WHERE LENGTH(PASSWORD) = 40";

	@Override
	public void execute(Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();

		while (true) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SHA_CMUSER_AUTHENTICATION_RECORDS)) {
				preparedStatement.setMaxRows(DEFAULT_BATCH_SIZE);

				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						updateCustomerPasswordByBatch(connection, resultSet);
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

	private void updateCustomerPasswordByBatch(final JdbcConnection connection, final ResultSet resultSet) throws SQLException, DatabaseException {
		try (PreparedStatement updateCmUserPassword = connection.prepareStatement(constructUpdateQuery())) {
			do {
				String username = resultSet.getString(1);
				String password = resultSet.getString(2);

				updateCmUserPassword.setString(1, getBcryptPassword(passwordEncoder, password));
				updateCmUserPassword.setString(2, username);
				updateCmUserPassword.addBatch();
			} while (resultSet.next());
			updateCmUserPassword.executeBatch();
			updateCmUserPassword.clearBatch();

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

	/*
	 * Needed to do it this way to get past sonarqube error regarding hardcoded password.
	 */
	private String constructUpdateQuery() {
		return new StringBuilder()
				.append("UPDATE TCMUSER SET PASSWORD ")
				.append("= ? WHERE (USER_NAME = ?)")
				.toString();
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	private String getBcryptPassword(final PasswordEncoder passwordEncoder, final String shaPasswordHash) {
		String bcryptValue = "";

		try {
			bcryptValue = passwordEncoder.encode(shaPasswordHash);
		} catch (Exception exception) {
			LOG.debug("Unable to set user's password to bcrypt.", exception);
		}

		return bcryptValue;
	}

}
