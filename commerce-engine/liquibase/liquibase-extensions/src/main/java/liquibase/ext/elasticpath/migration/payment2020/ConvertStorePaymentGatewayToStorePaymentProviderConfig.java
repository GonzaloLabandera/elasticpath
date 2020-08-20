/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath.migration.payment2020;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.core.InsertStatement;

/**
 * Custom liquibase change that converts TSTOREPAYMENTGATEWAY to TSTOREPAYMENTPROVIDERCONFIG.
 */
public class ConvertStorePaymentGatewayToStorePaymentProviderConfig extends AbstractPaymentConverter {
	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final String COLUMN_UIDPK = "UIDPK";
	private static final String TABLE_STORE_PAYMENT_PROVIDER_CONFIG = "TSTOREPAYMENTPROVIDERCONFIG";
	private static final String CONFIRMATION_MESSAGE = "Finished inserting data into TSTOREPAYMENTPROVIDERCONFIG.";

	private static final String QUERY_RETRIEVE_PAYMENT_GATEWAYS = "SELECT g.GUID, c.STORECODE FROM TSTOREPAYMENTGATEWAY w INNER JOIN TSTORE c "
			+ " ON w.STORE_UID = c.UIDPK INNER JOIN TPAYMENTPROVIDERCONFIG g ON w.GATEWAY_UID = g.UIDPK";
	private static final String COLUMN_PAYMENT_PROVIDER_CONFIG_GUID = "PAYMENT_PROVIDER_CONFIG_GUID";
	private static final String COLUMN_STORECODE = "STORECODE";
	private static final String COLUMN_GUID = "GUID";

	@Override
	protected List<String> getTargetTables() {
		return Collections.singletonList(TABLE_STORE_PAYMENT_PROVIDER_CONFIG);
	}

	@Override
	protected String getSelectDataQuery() {
		return QUERY_RETRIEVE_PAYMENT_GATEWAYS;
	}

	/**
	 * Prepares set of {@link InsertStatement} for TSTOREPAYMENTPROVIDERCONFIG table.
	 *
	 * @param database is current database.
	 * @return {@link InsertStatement} for TSTOREPAYMENTPROVIDERCONFIG table.
	 * @throws SQLException in case exception of extracting data from result set.
	 */
	@Override
	protected void prepareInsertStatements(final Database database) throws SQLException, DatabaseException {
		try (final PreparedStatement preparedStatement = ((JdbcConnection) database.getConnection())
				.prepareStatement(getSelectDataQuery(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					incrementUidPKForAllTargetTables();

					Map<String, Object> context = createRecordSetContext(resultSet);
					final Map<String, Object> recordSet = createStorePaymentProviderConfigTableRecordSet(context);
					final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
							database.getDefaultSchemaName(),
							TABLE_STORE_PAYMENT_PROVIDER_CONFIG,
							recordSet);

					getInsertSetStatementMap().get(TABLE_STORE_PAYMENT_PROVIDER_CONFIG).addInsertStatement(statement);
					LOG.info("Prepared InsertSetStatements for " + String.join(" , ", getTargetTables()) + " for " + resultSet.getRow());

				}
			}
		}
	}

	@Override
	protected Map<String, Object> createRecordSetContext(final ResultSet resultSet) throws SQLException {
		final Map<String, Object> data = new HashMap<>();

		final String paymentGatewayGuid = resultSet.getString(COLUMN_GUID);
		final String storeCode = resultSet.getString(COLUMN_STORECODE);
		data.put(COLUMN_UIDPK, getUidpks().get(TABLE_STORE_PAYMENT_PROVIDER_CONFIG));
		data.put(COLUMN_PAYMENT_PROVIDER_CONFIG_GUID, paymentGatewayGuid);
		data.put(COLUMN_STORECODE, storeCode);
		data.put(COLUMN_GUID, UUID.randomUUID().toString());
		return data;
	}

	/**
	 * <p>For each TSTOREPAYMENTGATEWAY record:</p>
	 * <p>Create a new TSTOREPAYMENTPROVIDERCONFIG record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> New Data Model </td> <td> Old Data Model</td>
	 * </tr>
	 * <tr>
	 * <td> UIDPK </td> <td> Generate sequential value</td>
	 * </tr>
	 * <tr>
	 * <td> PAYMENT_PROVIDER_CONFIG_GUID </td> <td> GUID from the TPAYMENTPROVIDERCONFIG table corresponding to GATEWAY_UID</td>
	 * </tr>
	 * <tr>
	 * <td> STORECODE </td> <td> STORECODE from the TSTORE corresponding to STORE_UID</td>
	 * </tr>
	 * <tr>
	 * <td> GUID </td> <td> Generate random GUID</td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TSTOREPAYMENTPROVIDERCONFIG table.
	 */
	private Map<String, Object> createStorePaymentProviderConfigTableRecordSet(final Map<String, Object> context) {
		final Map<String, Object> data = new HashMap<>();

		data.put(COLUMN_UIDPK, context.get(COLUMN_UIDPK));
		data.put(COLUMN_PAYMENT_PROVIDER_CONFIG_GUID, context.get(COLUMN_PAYMENT_PROVIDER_CONFIG_GUID));
		data.put(COLUMN_STORECODE, context.get(COLUMN_STORECODE));
		data.put(COLUMN_GUID, UUID.randomUUID().toString());

		return data;
	}

	@Override
	public String getConfirmationMessage() {
		return CONFIRMATION_MESSAGE;
	}

	@Override
	public void setUp() {
		// no setup
	}

	@Override
	public void setFileOpener(final ResourceAccessor resourceAccessor) {
		// not used
	}

	@Override
	public ValidationErrors validate(final Database database) {
		return null;
	}
}
