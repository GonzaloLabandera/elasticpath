/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import liquibase.change.custom.CustomSqlChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;

import com.elasticpath.commons.util.Pair;

/**
 * Custom liquibase change that migrates customers for userIdMode removal.
 */
public class MigrateCustomersForUserIdModeRemoval implements CustomSqlChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final int DEFAULT_CUSTOMER_BATCH_SIZE = 1000;
	private int customerBatchSize = DEFAULT_CUSTOMER_BATCH_SIZE;

	private Boolean emailUserIdMode;

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		migrateCustomers((JdbcConnection) database.getConnection());

		return new SqlStatement[0];
	}

	@SuppressWarnings("try")
	private void migrateCustomers(final JdbcConnection connection) throws CustomChangeException {
		LOG.info("Starting customer migration for user id mode removal...");

		try (PreparedStatements statements = new PreparedStatements(connection, customerBatchSize);
			 ConnectionModifications connMods = new ConnectionModifications(connection)) {

			boolean emailUserIdMode = isEmailUserIdMode(connection);
			long customersTotal = countCustomersTotal(connection);
			long customersProcessedLastBatch = 0L;
			long customersProcessedTotal = 0L;
			long startCustomerUid = 0L;
			long startTime = System.currentTimeMillis();
			long startTimeForBatch = startTime;

			while (true) {
				long processTimeLastBatch = System.currentTimeMillis() - startTimeForBatch;
				LOG.info(String.format(
						"Processing next batch starting at uid=%d Completed: %d of %d (%d customers/sec)",
						startCustomerUid, customersProcessedTotal, customersTotal,
						calculateItemsProcessedPerSecond(customersProcessedLastBatch, processTimeLastBatch)));

				startTimeForBatch = System.currentTimeMillis();

				List<CustomerResult> customerResults =
						statements.executeSelectCustomers(startCustomerUid, customerBatchSize);

				if (customerResults.isEmpty()) {
					long totalElapsedTime = System.currentTimeMillis() - startTime;
					LOG.info(String.format(
							"Customer migration completed. Processed %d customers in %d seconds (%d customers/sec)",
							customersProcessedTotal, totalElapsedTime / 1000,
							calculateItemsProcessedPerSecond(customersProcessedTotal, totalElapsedTime)));
					return;
				}

				List<Long> allCustomerUidsInBatch = customerResults.stream()
						.map(customer -> customer.customerUid)
						.collect(Collectors.toList());

				// customers that should not be able to authenticate using cortex's OAuth2 resource
				List<CustomerResult> customersForAuthRemoval;
				if (emailUserIdMode) {
					// filter only anonymous customers, registered customers should still be able to auth
					customersForAuthRemoval = customerResults.stream()
							.filter(customer -> customer.isAnonymous)
							.collect(Collectors.toList());
				} else {
					// all customer authentication is handled by CMS when in trusted-header-mode
					customersForAuthRemoval = customerResults;
				}

				statements.executeNullifyCustAuth(customersForAuthRemoval.stream()
						.map(result -> result.customerUid)
						.collect(Collectors.toList()));

				statements.executeDeleteAuth(customersForAuthRemoval.stream()
						.map(result -> result.authenticationUid)
						.collect(Collectors.toList()));

				// Delete CP_EMAIL customer profile value from customers who did not provide an email
				statements.executeDeleteDummyEmail(allCustomerUidsInBatch);

				// User id is managed by CMS in trusted-header-mode and should not be changed
				if (emailUserIdMode) {
					// All anonymous users used to be assigned the user id "public@ep-cortex.com" if the customer has
					// not specified an email address. This value gets updated to their email once they've specified one.
					// We need to set these user ids to something unique so that when we look up a user id, we would
					// only get a single customer back.
					statements.executeUpdateCustUserId(customerResults.stream()
							.filter(customer -> customer.isAnonymous)
							.map(customer -> customer.customerUid)
							.collect(Collectors.toList()));
				}

				connection.commit();

				customersProcessedLastBatch = customerResults.size();
				customersProcessedTotal += customersProcessedLastBatch;
				startCustomerUid = allCustomerUidsInBatch.get(allCustomerUidsInBatch.size() - 1) + 1;
			}
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}
	}

	private static long calculateItemsProcessedPerSecond(final long itemsProcessed, final long periodMillis) {
		long period = periodMillis > 0 ? periodMillis : 1;
		return itemsProcessed * 1000 / period;
	}

	private class PreparedStatements implements AutoCloseable {
		private PreparedStatement selectCustStatement;
		private PreparedStatement selectAnonCpvStatement;
		private PreparedStatement nullifyCustAuthStatement;
		private PreparedStatement deleteAuthStatement;
		private PreparedStatement deleteDummyEmailStatement;
		private PreparedStatement updateCustUserIdStatement;

		private PreparedStatements(
				final JdbcConnection connection, final int maxBatchSize)
				throws CustomChangeException {

			try {
				selectCustStatement = connection.prepareStatement(
						"SELECT UIDPK, AUTHENTICATION_UID FROM TCUSTOMER WHERE UIDPK >= ?");

				selectAnonCpvStatement = connection.prepareStatement(
						"SELECT CUSTOMER_UID, BOOLEAN_VALUE FROM TCUSTOMERPROFILEVALUE "
								+ "WHERE ATTRIBUTE_UID = ? "
								+ "AND CUSTOMER_UID IN " + buildMultiArgsParam(maxBatchSize));
				selectAnonCpvStatement.setLong(1, getAnonCustAttributeUid(connection));

				nullifyCustAuthStatement = connection.prepareStatement(
						"UPDATE TCUSTOMER set AUTHENTICATION_UID=NULL "
								+ "WHERE UIDPK IN " + buildMultiArgsParam(maxBatchSize));

				deleteAuthStatement = connection.prepareStatement(
						"DELETE FROM TCUSTOMERAUTHENTICATION "
								+ "WHERE UIDPK IN " + buildMultiArgsParam(maxBatchSize));

				deleteDummyEmailStatement = connection.prepareStatement(
						"DELETE FROM TCUSTOMERPROFILEVALUE "
								+ "WHERE ATTRIBUTE_UID = ? "
								+ "AND SHORT_TEXT_VALUE = 'public@ep-cortex.com' "
								+ "AND CUSTOMER_UID IN " + buildMultiArgsParam(maxBatchSize));
				deleteDummyEmailStatement.setLong(1, getEmailCustAttributeUid(connection));

				updateCustUserIdStatement = connection.prepareStatement(
						"UPDATE TCUSTOMER SET USER_ID = GUID "
								+ "WHERE UIDPK IN " + buildMultiArgsParam(maxBatchSize));

			} catch (Exception e) {
				this.close();
				throw new CustomChangeException(e);
			}
		}

		private Long getAnonCustAttributeUid(final JdbcConnection connection) throws CustomChangeException {
			return querySingle(connection,
					"select UIDPK from TATTRIBUTE where ATTRIBUTE_KEY = 'CP_ANONYMOUS_CUST'",
					ResultSet::getLong);
		}

		private Long getEmailCustAttributeUid(final JdbcConnection connection) throws CustomChangeException {
			return querySingle(connection,
					"select UIDPK from TATTRIBUTE where ATTRIBUTE_KEY = 'CP_EMAIL'",
					ResultSet::getLong);
		}

		private String buildMultiArgsParam(final int size) {
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			for (int i = 0; i < size; i++) {
				if (i > 0) {
					builder.append(",");
				}
				builder.append("?");
			}
			builder.append(")");
			return builder.toString();
		}

		List<CustomerResult> executeSelectCustomers(
				final long startCustUid, final int batchSize)
				throws SQLException {

			selectCustStatement.setLong(1, startCustUid);
			selectCustStatement.setMaxRows(batchSize);

			try (ResultSet resultSet = selectCustStatement.executeQuery()) {
				Map<Long, Long> customerAuthMap = new HashMap<>();

				List<Long> customerUids = extractResultsAsList(resultSet, rs -> {
					long customerUid = rs.getLong(1);
					long authenticationUid = rs.getLong(2);
					customerAuthMap.put(customerUid, authenticationUid);
					return customerUid;
				});

				Map<Long, Boolean> customerAnonMap = executeSelectAnonCpv(customerUids);

				return customerUids.stream()
						.map(custUid -> new CustomerResult(
								custUid,
								customerAuthMap.get(custUid),
								customerAnonMap.get(custUid)))
						.collect(Collectors.toList());
			}
		}

		private Map<Long, Boolean> executeSelectAnonCpv(final List<Long> custUids) throws SQLException {
			setMultiArgs(selectAnonCpvStatement::setLong, 0L, 2, custUids);
			try (ResultSet resultSet = selectAnonCpvStatement.executeQuery()) {
				List<Pair<Long, Boolean>> results = extractResultsAsList(resultSet, rs -> {
					long custUid = rs.getLong(1);
					boolean isAnonymous = rs.getBoolean(2);
					return Pair.of(custUid, isAnonymous);
				});
				return results.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
			}
		}

		void executeNullifyCustAuth(final List<Long> custUids) throws SQLException {
			setMultiArgs(nullifyCustAuthStatement::setLong, 0L, 1, custUids);
			nullifyCustAuthStatement.executeUpdate();
		}

		void executeDeleteAuth(final List<Long> authUids) throws SQLException {
			setMultiArgs(deleteAuthStatement::setLong, 0L, 1, authUids);
			deleteAuthStatement.executeUpdate();
		}

		void executeDeleteDummyEmail(final List<Long> custUids) throws SQLException {
			setMultiArgs(deleteDummyEmailStatement::setLong, 0L, 2, custUids);
			deleteDummyEmailStatement.executeUpdate();
		}

		void executeUpdateCustUserId(final List<Long> custUids) throws SQLException {
			setMultiArgs(updateCustUserIdStatement::setLong, 0L, 1, custUids);
			updateCustUserIdStatement.executeUpdate();
		}

		private <T> void setMultiArgs(
				final ThrowingBiConsumer<Integer, T, SQLException> setter,
				final T defaultValue, final int startPos, final List<T> values) throws SQLException {

			for (int i = 0; i < customerBatchSize; i++) {
				T item = i < values.size() ? values.get(i) : defaultValue;
				setter.accept(startPos + i, item);
			}
		}

		@Override
		public void close() {
			closePreparedStatement(selectCustStatement);
			closePreparedStatement(selectAnonCpvStatement);
			closePreparedStatement(nullifyCustAuthStatement);
			closePreparedStatement(deleteAuthStatement);
			closePreparedStatement(updateCustUserIdStatement);
		}

		private void closePreparedStatement(final PreparedStatement preparedStatement) {
			if (preparedStatement != null) {
				doIgnoreError(preparedStatement::close);
			}
		}
	}

	private class ConnectionModifications implements AutoCloseable {
		private JdbcConnection connection;
		private final boolean origAutoCommit;

		private ConnectionModifications(final JdbcConnection connection) throws CustomChangeException {
			this.connection = connection;
			try {
				origAutoCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);
			} catch (Exception e) {
				this.close();
				throw new CustomChangeException(e);
			}
		}

		@Override
		public void close() {
			doIgnoreError(() -> connection.setAutoCommit(origAutoCommit));
		}
	}

	private Long countCustomersTotal(final JdbcConnection connection) throws CustomChangeException {
		return querySingle(connection, "select count(UIDPK) from TCUSTOMER", ResultSet::getLong);
	}

	private boolean isEmailUserIdMode(final JdbcConnection connection) throws CustomChangeException {
		if (emailUserIdMode == null) {
			Pair<String, String> result = queryPair(connection,
					"select sd.DEFAULT_VALUE, sv.CONTEXT_VALUE "
							+ "from TSETTINGDEFINITION sd "
							+ "left join TSETTINGVALUE sv on sd.UIDPK = sv.SETTING_DEFINITION_UID "
							+ "where PATH = 'COMMERCE/SYSTEM/userIdMode'",
					ResultSet::getString, ResultSet::getString);

			String defaultValue = result.getFirst();
			String overrideValue = result.getSecond();

			if (overrideValue != null) {
				emailUserIdMode = Integer.parseInt(overrideValue) == 1;
			} else {
				emailUserIdMode = Integer.parseInt(defaultValue) == 1;
			}
		}

		return emailUserIdMode;
	}

	private void doIgnoreError(final ThrowingRunnable<? extends Exception> action) {
		try {
			action.run();
		} catch (Exception e) {
			//do nothing
		}
	}

	private <T> List<T> extractResultsAsList(
			final ResultSet resultSet,
			final ThrowingFunction<ResultSet, T, SQLException> extractSingle)
			throws SQLException {

		List<T> resultsList = new ArrayList<>();
		while (resultSet.next()) {
			resultsList.add(extractSingle.apply(resultSet));
		}
		return resultsList;
	}

	private <T> T querySingle(
			final JdbcConnection connection, final String sql,
			final ThrowingBiFunction<ResultSet, Integer, T, SQLException> extract)
			throws CustomChangeException {

		return querySimple(connection, sql, resultSet -> extract.apply(resultSet, 1));
	}

	private <T, U> Pair<T, U> queryPair(
			final JdbcConnection connection, final String sql,
			final ThrowingBiFunction<ResultSet, Integer, T, SQLException> extractFirst,
			final ThrowingBiFunction<ResultSet, Integer, U, SQLException> extractSecond)
			throws CustomChangeException {

		return querySimple(connection, sql, resultSet -> {
			T first = extractFirst.apply(resultSet, 1);
			U second = extractSecond.apply(resultSet, 2);
			return Pair.of(first, second);
		});
	}

	private <T> T querySimple(
			final JdbcConnection connection, final String sql,
			final ThrowingFunction<ResultSet, T, SQLException> extract)
			throws CustomChangeException {

		try {
			try (PreparedStatement statement = connection.prepareStatement(sql);
				 ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return extract.apply(resultSet);
				}
			}
		} catch (Exception e) {
			throw new CustomChangeException(e);
		}
		throw new CustomChangeException("Unable to determine user id mode");
	}

	@FunctionalInterface
	private interface ThrowingRunnable<E extends Throwable> {
		void run() throws E;
	}

	@FunctionalInterface
	private interface ThrowingBiConsumer<T, U, E extends Throwable> {
		void accept(T t, U u) throws E;
	}

	@FunctionalInterface
	private interface ThrowingFunction<T, R, E extends Throwable> {
		R apply(T t) throws E;
	}

	@FunctionalInterface
	private interface ThrowingBiFunction<T, U, R, E extends Throwable> {
		R apply(T t, U u) throws E;
	}

	private class CustomerResult {
		private long customerUid;
		private long authenticationUid;
		private boolean isAnonymous = false;

		private CustomerResult(final long customerUid, final long authenticationUid, final Boolean isAnonymous) {
			this.customerUid = customerUid;
			this.authenticationUid = authenticationUid;
			if (isAnonymous != null) {
				this.isAnonymous = isAnonymous;
			}
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished migrating customers for user id mode removal";
	}

	@Override
	public void setUp() throws SetupException {
		// no setup
	}

	@Override
	public void setFileOpener(final ResourceAccessor resourceAccessor) {
		// not used
	}

	@Override
	public ValidationErrors validate(final Database database) {
		return null; // no validation needed
	}

	public void setCustomerBatchSize(final String customerBatchSize) {
		this.customerBatchSize = Integer.parseInt(customerBatchSize);
	}
}
