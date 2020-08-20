/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath.migration.payment2020;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
 * Custom liquibase change that migrate saved payment instruments on customers.
 */
public class MigrateSavedPaymentInstruments extends AbstractPaymentConverter {
	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final String COLUMN_UIDPK = "UIDPK";
	private static final String TABLE_PAYMENT_INSTRUMENT = "TPAYMENTINSTRUMENT";
	private static final String TABLE_PAYMENT_INSTRUMENT_DATA = "TPAYMENTINSTRUMENTDATA";
	private static final String TABLE_CUSTOMER_PAYMENT_INSTRUMENT = "TCUSTOMERPAYMENTINSTRUMENT";
	private static final String TABLE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT = "TCUSTDEFAULTPAYMENTINSTRUMENT";
	private static final String CONFIRMATION_MESSAGE = "Finished inserting data into " + TABLE_PAYMENT_INSTRUMENT + " , "
			+ TABLE_PAYMENT_INSTRUMENT_DATA + " , " + TABLE_CUSTOMER_PAYMENT_INSTRUMENT + " , " + TABLE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT;

	private static final String QUERY_RETRIEVE_PAYMENT_INSTRUMENTS =
			" SELECT r.STORECODE,"
					+ "       m.CUSTOMER_UID,"
					+ "       r.DEFAULT_PAYMENT_METHOD_UID,"
					+ "       m.PAYMENT_METHOD_UID,"
					+ "       t.VALUE         AS TOKEN_VALUE,"
					+ "       t.DISPLAY_VALUE AS TOKEN_DISPLAY_VALUE,"
					+ "       g.UIDPK         AS  CONFIG_UID"
					+ " FROM TCUSTOMERPAYMENTMETHOD m"
					+ "       INNER JOIN TPAYMENTTOKEN t ON m.PAYMENT_METHOD_UID = t.UIDPK"
					+ "       INNER JOIN TCUSTOMER r ON r.UIDPK = m.CUSTOMER_UID"
					+ "       INNER JOIN TSTORE ts ON ts.STORECODE = r.STORECODE"
					+ "       INNER JOIN TSTOREPAYMENTGATEWAY w ON ts.UIDPK = w.STORE_UID"
					+ "       INNER JOIN TPAYMENTPROVIDERCONFIG g ON g.UIDPK = w.GATEWAY_UID"
					+ "       INNER JOIN TPAYMENTGATEWAY pg on w.GATEWAY_UID = pg.UIDPK"
					+ " WHERE pg.TYPE NOT IN"
					+ "      ('paymentGatewayExchange', 'paymentGatewayGiftCertificate', 'paymentGatewayPaypalExpress', "
					+ " 'paymentGatewayPaypalDoDirect') AND t.DISPLAY_VALUE IS NOT NULL";
	private static final String DISPLAY_VALUE = "displayValue";
	private static final String CONFIG_VALUE = "configValue";
	private static final String PAYMENT_PROVIDER_CONFIG_UID = "paymentProviderConfigUID";
	private static final String CUSTOMER_UID = "customerUid";
	private static final String DEFAULT_PAYMENT_METHOD_UID = "defaultPaymentMethodUid";
	private static final String PAYMENT_METHOD_UID = "paymentMethodUid";
	private static final String PAYMENT_INSTRUMENT_GUID = "paymentInstrumentGUID";
	private static final String PAYMENT_INSTRUMENT_UIDPK = "paymentInstrumentUidpk";
	private static final String CUSTOMER_PAYMENT_INSTRUMENT_UIDPK = "customerPaymentInstrumentUIDPK";

	@Override
	protected List<String> getTargetTables() {
		return Arrays.asList(TABLE_PAYMENT_INSTRUMENT, TABLE_PAYMENT_INSTRUMENT_DATA, TABLE_CUSTOMER_PAYMENT_INSTRUMENT,
				TABLE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT);
	}

	@Override
	protected String getSelectDataQuery() {
		return QUERY_RETRIEVE_PAYMENT_INSTRUMENTS;
	}


	/**
	 * Prepares set of {@link InsertStatement} for TPAYMENTINSTRUMENT, TPAYMENTINSTRUMENTDATA, TCUSTOMERPAYMENTINSTRUMENT,
	 * TCUSTDEFAULTPAYMENTINSTRUMENT tables.
	 *
	 * @param database is current database.
	 * @return {@link InsertStatement} for tables.
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

					final Map<String, Object> paymentInstrument = createPaymentInstrumentRecordSet(context);
					final Map<String, Object> paymentInstrumentData = createPaymentInstrumentDataRecordSet(context);
					final Map<String, Object> customerPaymentInstrument = createCustomerPaymentInstrumentRecordSet(context);
					final Map<String, Object> customerDefaultPaymentInstrument = createCustomerDefaultPaymentInstrumentRecordSet(context);

					addPaymentInstrumentStatement(database, paymentInstrument);
					addPaymentInstrumentDataStatement(database, paymentInstrumentData);
					addCustomerPaymentInstrumentStatement(database, customerPaymentInstrument);
					addCustomerDefaultPaymentInstrumentStatement(database, customerDefaultPaymentInstrument);

					LOG.info("Prepared InsertSetStatements for " + String.join(" , ", getTargetTables()) + " for " + resultSet.getRow());
				}
			}
		}
	}

	@Override
	protected Map<String, Object> createRecordSetContext(final ResultSet resultSet) throws SQLException {
		final Map<String, Object> data = new HashMap<>();

		data.put(DISPLAY_VALUE, resultSet.getString("TOKEN_DISPLAY_VALUE"));
		data.put(CONFIG_VALUE, resultSet.getString("TOKEN_VALUE"));
		data.put(PAYMENT_PROVIDER_CONFIG_UID, resultSet.getString("CONFIG_UID"));
		data.put(CUSTOMER_UID, resultSet.getString("CUSTOMER_UID"));
		data.put(DEFAULT_PAYMENT_METHOD_UID, resultSet.getString("DEFAULT_PAYMENT_METHOD_UID"));
		data.put(PAYMENT_METHOD_UID, resultSet.getString("PAYMENT_METHOD_UID"));
		data.put(PAYMENT_INSTRUMENT_GUID, UUID.randomUUID().toString());
		data.put(PAYMENT_INSTRUMENT_UIDPK, getUidpks().get(TABLE_PAYMENT_INSTRUMENT));
		data.put(CUSTOMER_PAYMENT_INSTRUMENT_UIDPK, getUidpks().get(TABLE_CUSTOMER_PAYMENT_INSTRUMENT));

		return data;
	}

	/**
	 * </p>For each TCUSTOMERPAYMENTMETHOD record:</p>
	 * <p>Create a new TCUSTOMERDEFAULTPAYMENTINSTRUMENT record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> UIDPK </td> <td> Generate sequential value</td>
	 * </tr>
	 * <tr>
	 * <td> CUSTOMER_UID </td> <td> CUSTOMER_UID</td>
	 * </tr>
	 * <tr>
	 * <td> CUSTOMER_PYMT_INSTRUMENT_UID </td> <td> The UIDPK of the TCUSTOMERPAYMENTINSTRUMENT record</td>
	 * </tr>
	 * <tr>
	 * <td> GUID </td> <td> Generate GUID</td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TCUSTOMERDEFAULTPAYMENTINSTRUMENT table .
	 */
	private Map<String, Object> createCustomerDefaultPaymentInstrumentRecordSet(final Map<String, Object> context) {
		final Optional<String> defaultPaymentMethodUid = Optional.ofNullable(context.get(DEFAULT_PAYMENT_METHOD_UID))
				.map(Object::toString);
		final Optional<String> paymentMethodUid = Optional.ofNullable(context.get(PAYMENT_METHOD_UID))
				.map(Object::toString);

		if (defaultPaymentMethodUid.isPresent() && defaultPaymentMethodUid.equals(paymentMethodUid)) {
			final Map<String, Object> customerDefaultPaymentInstrument = new HashMap<>();
			customerDefaultPaymentInstrument.put(COLUMN_UIDPK, getUidpks().get(TABLE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT));
			customerDefaultPaymentInstrument.put("CUSTOMER_UID", context.get(CUSTOMER_UID));
			customerDefaultPaymentInstrument.put("CUSTOMER_PYMT_INSTRUMENT_UID", context.get(CUSTOMER_PAYMENT_INSTRUMENT_UIDPK));
			return customerDefaultPaymentInstrument;
		}

		return null;
	}

	/**
	 * </p>For each TCUSTOMERPAYMENTMETHOD record:</p>
	 * <p>Create a new TCUSTOMERPAYMENTINSTRUMENT record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> UIDPK </td> <td> Generate sequential value</td>
	 * </tr>
	 * <tr>
	 * <td> CUSTOMER_UID </td> <td> CUSTOMER_UID</td>
	 * </tr>
	 * <tr>
	 * <td> PAYMENT_INSTRUMENT_GUID </td> <td> The GUID of the TPAYMENTINSTRUMENT record created</td>
	 * </tr>
	 * <tr>
	 * <td> GUID </td> <td> Generate GUID</td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TCUSTOMERPAYMENTINSTRUMENT table .
	 */
	private Map<String, Object> createCustomerPaymentInstrumentRecordSet(final Map<String, Object> context) {
		final Map<String, Object> customerPaymentInstrument = new HashMap<>();
		customerPaymentInstrument.put(COLUMN_UIDPK, context.get(CUSTOMER_PAYMENT_INSTRUMENT_UIDPK));
		customerPaymentInstrument.put("CUSTOMER_UID", context.get(CUSTOMER_UID));
		customerPaymentInstrument.put("GUID", UUID.randomUUID().toString());
		customerPaymentInstrument.put("PAYMENT_INSTRUMENT_GUID", context.get(PAYMENT_INSTRUMENT_GUID));
		return customerPaymentInstrument;
	}

	/**
	 * <p>For each TCUSTOMERPAYMENTMETHOD record:</p>
	 * <p>Create a new TPAYMENTINSTRUMENTDATA record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> UIDPK </td> <td> Generate sequential value</td>
	 * </tr>
	 * <tr>
	 * <td> PAYMENT_INSTRUMENT_UID </td> <td> UIDPK of the TPAYMENTINSTRUMENT record created</td>
	 * </tr>
	 * <tr>
	 * <td> CONFIG_KEY </td> <td> payment token value</td>
	 * </tr>
	 * <tr>
	 * <td> CONFIG_VALUE </td> <td> VALUE from TPAYMENTTOKEN corresponding to PAYMENT_METHOD_UID</td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TPAYMENTINSTRUMENTDATA table.
	 */
	private Map<String, Object> createPaymentInstrumentDataRecordSet(final Map<String, Object> context) {
		final Map<String, Object> paymentInstrumentData = new HashMap<>();
		paymentInstrumentData.put(COLUMN_UIDPK, getUidpks().get(TABLE_PAYMENT_INSTRUMENT_DATA));
		paymentInstrumentData.put("CONFIG_KEY", "token");
		paymentInstrumentData.put("PAYMENTINSTRUMENT_UID", context.get(PAYMENT_INSTRUMENT_UIDPK));
		paymentInstrumentData.put("CONFIG_DATA", context.get(CONFIG_VALUE));
		return paymentInstrumentData;
	}

	/**
	 * /**
	 * <p>For each TCUSTOMERPAYMENTMETHOD record:</p>
	 * <p>Create a new TPAYMENTINSTRUMENT record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> PAYMENTPROVIDERCONFIG_UID </td> <td> Use the "Determine Credit Card PaymentProviderConfig for a store" logic using the store code </td>
	 * </tr>
	 * <tr>
	 * <td> BILLING_ADDRESS_GUID </td> <td> null</td>
	 * </tr>
	 * <tr>
	 * <td> UIDPK </td> <td> Generate sequential value</td>
	 * </tr>
	 * <tr>
	 * <td> GUID </td> <td> Generate GUID</td>
	 * </tr>
	 * <tr>
	 * <td> NAME </td> <td> DISPLAY_VALUE from TPAYMENTTOKEN corresponding to PAYMENT_METHOD_UID</td>
	 * </tr>
	 * <tr>
	 * <td> IS_SUPPORTING_MULTI_CHARGES </td> <td> false </td>
	 * </tr>
	 * <tr>
	 * <td> IS_SINGLE_RESERVE_PER_PI </td> <td> false </td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TPAYMENTINSTRUMENT table .
	 */
	private Map<String, Object> createPaymentInstrumentRecordSet(final Map<String, Object> context) {
		final Map<String, Object> paymentInstrument = new HashMap<>();
		paymentInstrument.put(COLUMN_UIDPK, context.get(PAYMENT_INSTRUMENT_UIDPK));
		paymentInstrument.put("GUID", context.get(PAYMENT_INSTRUMENT_GUID));
		paymentInstrument.put("PAYMENTPROVIDERCONFIG_UID", context.get(PAYMENT_PROVIDER_CONFIG_UID));
		paymentInstrument.put("NAME", context.get(DISPLAY_VALUE));
		paymentInstrument.put("IS_SUPPORTING_MULTI_CHARGES", false);
		paymentInstrument.put("IS_SINGLE_RESERVE_PER_PI", false);
		return paymentInstrument;
	}

	private void addCustomerDefaultPaymentInstrumentStatement(final Database database, final Map<String, Object> customerDefaultPaymentInstrument) {
		if (Objects.nonNull(customerDefaultPaymentInstrument)) {
			final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
					database.getDefaultSchemaName(),
					TABLE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT,
					customerDefaultPaymentInstrument);

			getInsertSetStatementMap().get(TABLE_CUSTOMER_DEFAULT_PAYMENT_INSTRUMENT).addInsertStatement(statement);
		}
	}

	private void addCustomerPaymentInstrumentStatement(final Database database, final Map<String, Object> customerPaymentInstrument) {
		final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
				database.getDefaultSchemaName(),
				TABLE_CUSTOMER_PAYMENT_INSTRUMENT,
				customerPaymentInstrument);

		getInsertSetStatementMap().get(TABLE_CUSTOMER_PAYMENT_INSTRUMENT).addInsertStatement(statement);
	}


	private void addPaymentInstrumentDataStatement(final Database database, final Map<String, Object> paymentInstrumentData) {
		final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
				database.getDefaultSchemaName(),
				TABLE_PAYMENT_INSTRUMENT_DATA,
				paymentInstrumentData);

		getInsertSetStatementMap().get(TABLE_PAYMENT_INSTRUMENT_DATA).addInsertStatement(statement);
	}

	private void addPaymentInstrumentStatement(final Database database, final Map<String, Object> paymentInstrument) {
		final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
				database.getDefaultSchemaName(),
				TABLE_PAYMENT_INSTRUMENT,
				paymentInstrument);

		getInsertSetStatementMap().get(TABLE_PAYMENT_INSTRUMENT).addInsertStatement(statement);
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
