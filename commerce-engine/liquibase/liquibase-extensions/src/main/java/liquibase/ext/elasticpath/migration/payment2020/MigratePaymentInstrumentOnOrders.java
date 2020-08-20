/**
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
public class MigratePaymentInstrumentOnOrders extends AbstractPaymentConverter {
	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final String COLUMN_UIDPK = "UIDPK";
	private static final String TABLE_PAYMENT_INSTRUMENT = "TPAYMENTINSTRUMENT";
	private static final String TABLE_ORDERPAYMENT_PAYMENTINSTRUMENT = "TORDERPAYMENTPAYMENTINSTRUMENT";
	private static final String PAYMENT_GATEWAY_GIFT_CERTIFICATE_TYPE = "paymentGatewayGiftCertificate";
	private static final String CONFIRMATION_MESSAGE = "Finished inserting data into " + TABLE_PAYMENT_INSTRUMENT;

	private static final String QUERY_RETRIEVE_PAYMENT_TRANSACTIONS =
			"SELECT op.DISPLAY_VALUE,"
					+ "       o.ORDER_BILLING_ADDRESS_UID,"
					+ "       ppc.UIDPK AS CONFIG_UID,"
					+ "       op.GIFTCERTIFICATE_UID,"
					+ "       gs.GIFT_CERTIFICATE_CODE,"
					+ "       o.ORDER_NUMBER,"
					+ "       op.UIDPK,"
					+ "       pg.TYPE"
					+ " FROM TORDERPAYMENT op"
					+ "       INNER JOIN TORDER o ON op.ORDER_UID = o.UIDPK"
					+ "       INNER JOIN TSTORE s ON s.STORECODE = o.STORECODE"
					+ "       INNER JOIN TSTOREPAYMENTGATEWAY spg ON s.UIDPK = spg.STORE_UID"
					+ "       INNER JOIN TPAYMENTPROVIDERCONFIG ppc ON ppc.UIDPK = spg.GATEWAY_UID"
					+ "       INNER JOIN TPAYMENTGATEWAY pg on spg.GATEWAY_UID = pg.UIDPK"
					+ "       LEFT JOIN TGIFTCERTIFICATE gs ON gs.UIDPK = op.GIFTCERTIFICATE_UID"
					+ " WHERE pg.TYPE NOT IN ('paymentGatewayExchange', 'paymentGatewayPaypalExpress', "
					+ " 'paymentGatewayPaypalDoDirect')";
	private static final String PAYMENT_INSTRUMENT_UIDPK = "paymentInstrumentUidpk";
	private static final String PAYMENT_PROVIDER_PLUGIN_TYPE = "paymentProviderPluginType";

	@Override
	protected List<String> getTargetTables() {
		return Arrays.asList(TABLE_PAYMENT_INSTRUMENT, TABLE_ORDERPAYMENT_PAYMENTINSTRUMENT);
	}

	@Override
	protected String getSelectDataQuery() {
		return QUERY_RETRIEVE_PAYMENT_TRANSACTIONS;
	}


	/**
	 * Prepares set of {@link InsertStatement} for TPAYMENTINSTRUMENT, TORDERPAYMENTPAYMENTINSTRUMENT tables.
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
					final Map<String, Object> gcPaymentInstrument = createGCPaymentInstrumentRecordSet(context);
					final Map<String, Object> orderPaymentPaymentInstrument = createOrderPaymentPaymentInstrumentRecordSet(context);

					addPaymentInstrumentStatement(database, paymentInstrument);
					addPaymentInstrumentStatement(database, gcPaymentInstrument);
					addOrderPaymentPaymentInstrumentStatement(database, orderPaymentPaymentInstrument);

					LOG.info("Prepared InsertSetStatements for " + String.join(" , ", getTargetTables()) + " for " + resultSet.getRow());
				}
			}
		}
	}

	private void addPaymentInstrumentStatement(final Database database, final Map<String, Object> paymentInstrument) {
		if (Objects.nonNull(paymentInstrument)) {
			final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
					database.getDefaultSchemaName(),
					TABLE_PAYMENT_INSTRUMENT,
					paymentInstrument);

			getInsertSetStatementMap().get(TABLE_PAYMENT_INSTRUMENT).addInsertStatement(statement);
		}
	}

	private void addOrderPaymentPaymentInstrumentStatement(final Database database, final Map<String, Object> paymentInstrument) {
		if (Objects.nonNull(paymentInstrument)) {
			final InsertStatement statement = createInsertStatementByRecordSet(database.getDefaultCatalogName(),
					database.getDefaultSchemaName(),
					TABLE_ORDERPAYMENT_PAYMENTINSTRUMENT,
					paymentInstrument);

			getInsertSetStatementMap().get(TABLE_ORDERPAYMENT_PAYMENTINSTRUMENT).addInsertStatement(statement);
		}
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
	 * <td> BILLING_ADDRESS_GUID </td> <td> TORDER.ORDER_BILLING_ADDRESS_UID</td>
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
		if (Objects.isNull(context.get("giftCertificateUid")) && !PAYMENT_GATEWAY_GIFT_CERTIFICATE_TYPE.equals(context.get(PAYMENT_PROVIDER_PLUGIN_TYPE))
				&& Objects.nonNull(context.get("displayValue"))) {
			final Map<String, Object> paymentInstrument = new HashMap<>();
			paymentInstrument.put(COLUMN_UIDPK, context.get(PAYMENT_INSTRUMENT_UIDPK));
			paymentInstrument.put("GUID", UUID.randomUUID().toString());
			paymentInstrument.put("NAME", context.get("displayValue"));
			paymentInstrument.put("PAYMENTPROVIDERCONFIG_UID", context.get("paymentProviderConfigUID"));
			paymentInstrument.put("IS_SUPPORTING_MULTI_CHARGES", false);
			paymentInstrument.put("IS_SINGLE_RESERVE_PER_PI", false);
			paymentInstrument.put("BILLING_ADDRESS_GUID", context.get("billingAddressGUID"));
			return paymentInstrument;
		}
		return null;
	}

	/**
	 * /**
	 * </p>Temporary table to link TORDERPAYMENT and TPAYMENTINSTRUMENT</p>
	 * <p>Create a new TPAYMENTINSTRUMENT record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> PAYMENTINSTRUMENT_UID </td> <td> Payment instrument UID</td>
	 * </tr>
	 * <tr>
	 * <td> ORDERPAYMENT_UID </td> <td> Order payment UID</td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TPAYMENTINSTRUMENT table .
	 */
	private Map<String, Object> createOrderPaymentPaymentInstrumentRecordSet(final Map<String, Object> context) {
		final Map<String, Object> data = new HashMap<>();
		data.put("PAYMENTINSTRUMENT_UID", context.get(PAYMENT_INSTRUMENT_UIDPK));
		data.put("ORDERPAYMENT_UID", context.get("orderPaymentUID"));
		return data;
	}

	/**
	 * /**
	 * <p></p>For each TCUSTOMERPAYMENTMETHOD record:</p>
	 * <p>
	 * Create a new TPAYMENTINSTRUMENT record with these values:
	 * </p>
	 * <table>
	 * <tr>
	 * <td> PAYMENTPROVIDERCONFIG_UID </td> <td> Use the Use the "Determine Gift Certificate PaymentProviderConfig for a store" logic using the
	 * store code</td>
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
	 * <td> NAME </td> <td> GIFT_CERTIFICATE_CODE FROM TGIFTCERTIFICATE</td>
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
	private Map<String, Object> createGCPaymentInstrumentRecordSet(final Map<String, Object> context) {
		if (Objects.nonNull(context.get("giftCertificateUid")) && PAYMENT_GATEWAY_GIFT_CERTIFICATE_TYPE.equals(context.get(PAYMENT_PROVIDER_PLUGIN_TYPE))) {
			final Map<String, Object> paymentInstrument = new HashMap<>();
			paymentInstrument.put(COLUMN_UIDPK, context.get(PAYMENT_INSTRUMENT_UIDPK));
			paymentInstrument.put("GUID", UUID.randomUUID().toString());
			paymentInstrument.put("NAME", context.get("certificateDisplayValue"));
			paymentInstrument.put("IS_SUPPORTING_MULTI_CHARGES", false);
			paymentInstrument.put("IS_SINGLE_RESERVE_PER_PI", false);
			paymentInstrument.put("PAYMENTPROVIDERCONFIG_UID", context.get("paymentProviderConfigUID"));
			paymentInstrument.put("BILLING_ADDRESS_GUID", null);

			return paymentInstrument;
		}
		return null;
	}

	@Override
	protected Map<String, Object> createRecordSetContext(final ResultSet resultSet) throws SQLException {
		final Map<String, Object> data = new HashMap<>();

		data.put("displayValue", resultSet.getString("DISPLAY_VALUE"));
		data.put("certificateDisplayValue", resultSet.getString("GIFT_CERTIFICATE_CODE"));
		data.put("paymentProviderConfigUID", resultSet.getString("CONFIG_UID"));
		data.put("giftCertificateUid", resultSet.getString("GIFTCERTIFICATE_UID"));
		data.put("billingAddressGUID", resultSet.getString("ORDER_BILLING_ADDRESS_UID"));
		data.put("orderPaymentUID", resultSet.getString(COLUMN_UIDPK));
		data.put(PAYMENT_PROVIDER_PLUGIN_TYPE, resultSet.getString("TYPE"));
		data.put(PAYMENT_INSTRUMENT_UIDPK, getUidpks().get(TABLE_PAYMENT_INSTRUMENT));

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
