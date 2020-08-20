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
import java.util.Objects;
import java.util.Optional;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.core.InsertStatement;

/**
 * Custom liquibase change that migrate order payment data.
 */
public class MigrateOrderPaymentData extends AbstractPaymentConverter {
	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final String COLUMN_UIDPK = "UIDPK";
	private static final String TABLE_ORDER_PAYMENT_DATA = "TORDERPAYMENTDATA";
	private static final String CONFIRMATION_MESSAGE = "Finished inserting data into TORDERPAYMENTDATA.";
	private static final String QUERY_RETRIEVE_ORDER_PAYMENT =
			"SELECT P.AUTHORIZATION_CODE,"
					+ "       P.REFERENCE_ID,"
					+ "       P.REQUEST_TOKEN,"
					+ "       P.CARD_TYPE,"
					+ "       P.DISPLAY_VALUE,"
					+ "       P.EMAIL,"
					+ "       TG.GIFT_CERTIFICATE_CODE,"
					+ "       T.SHIPMENT_NUMBER, P.UIDPK,"
	                + "       P.AUTHORIZATION_CODE as REQUEST_ID"
					+ " FROM TORDERPAYMENT P"
					+ "       JOIN TORDERPAYMENTS OP ON OP.UIDPK = P.UIDPK"
					+ "       LEFT JOIN TGIFTCERTIFICATE TG ON P.GIFTCERTIFICATE_UID = TG.UIDPK"
					+ " LEFT JOIN TORDERSHIPMENT T ON P.ORDERSHIPMENT_UID = T.UIDPK";
	private static final String ORDER_PAYMENT_UID = "ORDER_PAYMENT_UID";
	private static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
	private static final String CARD_TYPE = "CARD_TYPE";
	private static final String DISPLAY_VALUE = "DISPLAY_VALUE";
	private static final String EMAIL = "EMAIL";
	private static final String GIFT_CERTIFICATE_CODE = "GIFT_CERTIFICATE_CODE";
	private static final String REFERENCE_ID = "REFERENCE_ID";
	private static final String REQUEST_ID = "REQUEST_ID";
	private static final String REQUEST_TOKEN = "REQUEST_TOKEN";
	private static final String SHIPMENT_NUMBER = "SHIPMENT_NUMBER";

	@Override
	protected List<String> getTargetTables() {
		return Collections.singletonList(TABLE_ORDER_PAYMENT_DATA);
	}

	@Override
	protected String getSelectDataQuery() {
		return QUERY_RETRIEVE_ORDER_PAYMENT;
	}

	/**
	 * Prepares set of {@link InsertStatement} for TORDERPAYMENTDATA table.
	 *
	 * @param database is current database.
	 * @return {@link InsertStatement} for TORDERPAYMENTDATA table.
	 * @throws SQLException in case exception of extracting data from result set.
	 */
	@Override
	protected void prepareInsertStatements(final Database database) throws SQLException, DatabaseException {
		try (final PreparedStatement preparedStatement = ((JdbcConnection) database.getConnection())
				.prepareStatement(getSelectDataQuery(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					final Map<String, Object> context = createRecordSetContext(resultSet);
					addInsertStatement(database, context, REQUEST_ID);
					addInsertStatement(database, context, AUTHORIZATION_CODE);
					addInsertStatement(database, context, CARD_TYPE);
					addInsertStatement(database, context, DISPLAY_VALUE);
					addInsertStatement(database, context, EMAIL);
					addInsertStatement(database, context, GIFT_CERTIFICATE_CODE);
					addInsertStatement(database, context, REFERENCE_ID);
					addInsertStatement(database, context, REQUEST_TOKEN);
					addInsertStatement(database, context, SHIPMENT_NUMBER);

					LOG.info("Prepared AUTHORIZATION_CODE InsertSetStatements for "
							+ String.join(" , ", getTargetTables()) + " for " + resultSet.getRow());
				}
			}
		}
	}

	private void addInsertStatement(final Database database, final Map<String, Object> context, final String dataKey) {
		createDataRecordSet(context, dataKey)
				.map(recordSet -> createInsertStatementByRecordSet(database.getDefaultCatalogName(),
						database.getDefaultSchemaName(),
						TABLE_ORDER_PAYMENT_DATA,
						recordSet))
				.ifPresent(statement -> getInsertSetStatementMap().get(TABLE_ORDER_PAYMENT_DATA).addInsertStatement(statement));
	}

	@Override
	protected Map<String, Object> createRecordSetContext(final ResultSet resultSet) throws SQLException {
		final Map<String, Object> data = new HashMap<>();

		final String authorizationCode = resultSet.getString(AUTHORIZATION_CODE);
		final String uid = resultSet.getString(COLUMN_UIDPK);
		final String cardType = resultSet.getString(CARD_TYPE);
		final String displayValue = resultSet.getString(DISPLAY_VALUE);
		final String email = resultSet.getString(EMAIL);
		final String code = resultSet.getString(GIFT_CERTIFICATE_CODE);
		final String shipment_number = resultSet.getString(SHIPMENT_NUMBER);
		final String referenceId = resultSet.getString(REFERENCE_ID);
		final String requestToken = resultSet.getString(REQUEST_TOKEN);
		final String requestId = resultSet.getString(REQUEST_ID);

		data.put(ORDER_PAYMENT_UID, uid);
		data.put(AUTHORIZATION_CODE, authorizationCode);
		data.put(CARD_TYPE, cardType);
		data.put(DISPLAY_VALUE, displayValue);
		data.put(EMAIL, email);
		data.put(GIFT_CERTIFICATE_CODE, code);
		data.put(SHIPMENT_NUMBER, shipment_number);
		data.put(REFERENCE_ID, referenceId);
		data.put(REQUEST_TOKEN, requestToken);
		data.put(REQUEST_ID, requestId);

		return data;
	}

	/**
	 * <p>For each TORDERPAYMENT record:</p>
	 * <p>Create a new TORDERPAYMENTDATA record with these values:</p>
	 * <table>
	 * <tr>
	 * <td> New Data Model </td> <td> Old Data Model</td>
	 * </tr>
	 * <tr>
	 * <td> UIDPK </td> <td> Generate sequential value</td>
	 * </tr>
	 * <tr>
	 * <td> ORDER_PAYMENT_UID </td> <td> The UIDPK of the TORDERPAYMENT record created above</td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "DATA_KEY" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> DATA_VALUE </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "CARD_TYPE" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> CARD_TYPE </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "DISPLAY_VALUE" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> DISPLAY_VALUE </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "EMAIL" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> EMAIL </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "GIFT_CERTIFICATE_CODE" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> GIFT_CERTIFICATE_CODE </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "SHIPMENT_NUMBER" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> SHIPMENT_NUMBER </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "REFERENCE_ID" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> REFERENCE_ID </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_KEY </td> <td> "REQUEST_TOKEN" </td>
	 * </tr>
	 * <tr>
	 * <td> DATA_VALUE </td> <td> REQUEST_TOKEN </td>
	 * </tr>
	 * </table>
	 *
	 * @param context is context of current mapping.
	 * @return {@link InsertStatement} for TORDERPAYMENTDATA table.
	 */
	private Optional<Map<String, Object>> createDataRecordSet(final Map<String, Object> context, final String dataKey) {
		final Map<String, Object> data = new HashMap<>();
		if (Objects.nonNull(context.get(dataKey))) {
			incrementUidPKForAllTargetTables();

			data.put(COLUMN_UIDPK, getUidpks().get(TABLE_ORDER_PAYMENT_DATA));
			data.put(ORDER_PAYMENT_UID, context.get(ORDER_PAYMENT_UID));
			data.put("DATA_KEY", dataKey);
			data.put("DATA_VALUE", context.get(dataKey));
		}
		return data.isEmpty()
				? Optional.empty()
				: Optional.of(data);
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
