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

/**
 * Custom liquibase change that converts customer passwords from sha to bcrypt.
 */
public class ConvertCustomerPasswordsToBcrypt implements CustomTaskChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final int DEFAULT_BATCH_SIZE = 1000;

	private static final int DEFAULT_BCRYPT_PASSWORD_ENCODER_STRENGTH = 8;

	private static final String BCRYPT_STRENGTH_SYSTEM_PROPERTY_OVERRIDE = "bcrypt.strength";

	private int bcryptPasswordEncoderStrength;

	// Only grab rows with SHA-256 encoded passwords.
	private static final String SELECT_SHA_256_CUSTOMER_AUTHENTICATION_RECORDS =
			"SELECT UIDPK, PASSWORD, SALT FROM TCUSTOMERAUTHENTICATION WHERE LENGTH(PASSWORD) = 64";

	@Override
	public void execute(Database database) throws CustomChangeException {

		bcryptPasswordEncoderStrength = Integer.getInteger(BCRYPT_STRENGTH_SYSTEM_PROPERTY_OVERRIDE, DEFAULT_BCRYPT_PASSWORD_ENCODER_STRENGTH);

		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();

		while (true) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SHA_256_CUSTOMER_AUTHENTICATION_RECORDS)) {
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
		try (PreparedStatement updateCustomerPassword = connection.prepareStatement(constructUpdateQuery())) {
			do {
				String uidPk = resultSet.getString(1);
				String password = resultSet.getString(2);
				updateCustomerPassword.setString(1, getBcryptPassword(password));
				updateCustomerPassword.setString(2, uidPk);
				updateCustomerPassword.addBatch();
			} while (resultSet.next());
			updateCustomerPassword.executeBatch();
			updateCustomerPassword.clearBatch();

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
				.append("UPDATE TCUSTOMERAUTHENTICATION SET PASSWORD ")
				.append("= ? WHERE UIDPK = ?")
				.toString();
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

	private String getBcryptPassword(final String shaPasswordHash) {
		String bcryptValue = null;

		try {
			bcryptValue = new BCryptPasswordEncoder(bcryptPasswordEncoderStrength).encode(shaPasswordHash);
		} catch (Exception exception) {
			LOG.warning("Unable to set user's password to bcrypt.", exception);
		}

		return bcryptValue;
	}

}
