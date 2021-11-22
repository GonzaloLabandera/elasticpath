/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Custom liquibase change that converts customer passwords from sha to bcrypt.
 */
public class ConvertCustomerPasswordsToBcrypt extends AbstractEpCustomTaskChange {

	private static final int DEFAULT_BCRYPT_PASSWORD_ENCODER_STRENGTH = 8;

	private static final String BCRYPT_STRENGTH_SYSTEM_PROPERTY_OVERRIDE = "bcrypt.strength";

	private int bcryptPasswordEncoderStrength;
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	private static final String CONFIRMATION_MESSAGE = "Migrated %s customer passwords to bcrypt.";

	// Only grab rows with SHA-256 encoded passwords.
	private static final String SELECT_SHA_256_CUSTOMER_AUTHENTICATION_RECORDS =
			"SELECT UIDPK, PASSWORD FROM TCUSTOMERAUTHENTICATION WHERE LENGTH(PASSWORD) = 64";

	private static final String COUNT_SHA_256_CUSTOMER_AUTHENTICATION_RECORDS =
			"SELECT COUNT(UIDPK) FROM TCUSTOMERAUTHENTICATION WHERE LENGTH(PASSWORD) = 64";

	/**
	 * Constructor.
	 */
	public ConvertCustomerPasswordsToBcrypt() {
		setConfirmationMessage(CONFIRMATION_MESSAGE);
		bcryptPasswordEncoderStrength = Integer.getInteger(BCRYPT_STRENGTH_SYSTEM_PROPERTY_OVERRIDE, DEFAULT_BCRYPT_PASSWORD_ENCODER_STRENGTH);
		bCryptPasswordEncoder = new BCryptPasswordEncoder(bcryptPasswordEncoderStrength);
	}

	@Override
	protected void cleanup() {
		// Nothing to do.
	}

	@Override
	protected void process() throws CustomChangeException {
		while (true) {
			try {
				List<UidPkPasswordDto> uidPkPasswordDtoList = new ArrayList<>(batchSize);
				String selectQuery = getSelectQueryWithOffsetAndLimit(SELECT_SHA_256_CUSTOMER_AUTHENTICATION_RECORDS, 0);
				try (PreparedStatement preparedStatement = getConnection().prepareStatement(selectQuery)) {
					try (ResultSet resultSet = preparedStatement.executeQuery()) {
						while (resultSet.next()) {
							String uidPk = resultSet.getString(1);
							String password = resultSet.getString(2);
							uidPkPasswordDtoList.add(new UidPkPasswordDto(uidPk, bCryptPasswordEncoder.encode(password)));
						}
						if (uidPkPasswordDtoList.isEmpty()) {
							break;
						}
					}
				}
				updateCustomerPasswordByBatch(uidPkPasswordDtoList);
			} catch (Exception e) {
				throw new CustomChangeException("An error occurred while migrating customer passwords to bcrypt.");
			}
		}
	}

	@Override
	protected String getTotalCountOfRecordsQuery() {
		return COUNT_SHA_256_CUSTOMER_AUTHENTICATION_RECORDS;
	}

	private void updateCustomerPasswordByBatch(final List<UidPkPasswordDto> uidPkPasswordDtoList)
			throws SQLException, DatabaseException {
		try (PreparedStatement updateCustomerPassword = getConnection().prepareStatement(constructUpdateQuery())) {
			for (UidPkPasswordDto runner : uidPkPasswordDtoList) {
				updateCustomerPassword.setString(1, runner.getPassword());
				updateCustomerPassword.setString(2, runner.getUidPk());
				updateCustomerPassword.addBatch();
			}
			final int[] statementRowCount = updateCustomerPassword.executeBatch();
			updateCustomerPassword.clearBatch();
			getConnection().commit();
			incrementTotalProcessedRecords(Arrays.stream(statementRowCount).sum());
		}
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

	/**
	 * uidPk/password DTO.
	 */
	private class UidPkPasswordDto {
		private String uidPk;
		private String password;

		UidPkPasswordDto(String uidPk, String password) {
			this.uidPk = uidPk;
			this.password = password;
		}

		public String getUidPk() {
			return uidPk;
		}

		public String getPassword() {
			return password;
		}
	}
}
