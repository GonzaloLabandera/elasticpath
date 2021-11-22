/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package liquibase.ext.elasticpath;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;

/**
 * Custom liquibase change that converts attribute names to localized properties.
 */
public class ConvertAttributeNamesToLocalizedProperties extends AbstractEpCustomSqlChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();
	private static final String TABLE_LOCALIZED_PROPERTIES = "TLOCALIZEDPROPERTIES";
	private static final String TABLE_JPA_GENERATED_KEYS = "JPA_GENERATED_KEYS";
	private static final String COLUMN_LAST_VALUE = "LAST_VALUE";

	private static final String JPA_GENERATED_KEYS_NOT_FOUND = "No JPA_GENERATED_KEYS value for " + TABLE_LOCALIZED_PROPERTIES + " found.";
	private static final String GET_CURRENT_VALUE = "SELECT %s FROM %s WHERE ID = '%s'";

	private static final String TABLE_ATTRIBUTES = "TATTRIBUTE";
	private static final String GET_TATTRIBUTE_COUNT = "SELECT COUNT(*) FROM " + TABLE_ATTRIBUTES;
	private static final String VALUE_RETURNED_BY_DB_WITH_UNEXPECTED_TYPE = "Value returned by DB with unexpected type: ";
	private static final String QUERY_ATTRIBUTE = "SELECT TATTRIBUTE.UIDPK, TATTRIBUTE.NAME FROM TATTRIBUTE WHERE TATTRIBUTE.ATTRIBUTE_USAGE = 4";
	private static final String QUERY_ATTRIBUTE_AND_TCATALOG = "SELECT TATTRIBUTE.UIDPK, TATTRIBUTE.NAME, TCATALOG.DEFAULT_LOCALE FROM TATTRIBUTE"
			+ " INNER JOIN TCATALOG ON TCATALOG.UIDPK = TATTRIBUTE.CATALOG_UID";
	private static final String DEFAULT_LOCALE = "DEFAULT_LOCALE";
	private static final String COLUMN_UIDPK = "UIDPK";
	private static final String COLUMN_OBJECT_UID = "OBJECT_UID";
	private static final String COLUMN_LOCALIZED_PROPERTY_KEY = "LOCALIZED_PROPERTY_KEY";
	private static final String COLUMN_VALUE = "VALUE";
	private static final String COLUMN_TYPE = "TYPE";
	private static final String CONFIRMATION_MESSAGE = "Finished inserting attribute names into TLOCALIZEDPROPERTIES.";
	private static final String COLUMN_NAME = "NAME";
	private static final String COLUMN_TYPE_VALUE = "Attribute";
	private static final String COLUMN_LOCALIZED_PROPERTY_KEY_VALUE = "attributeDisplayName_";
	private static final String UPDATE_JPA_GENERATED_KEYS = "UPDATE %s SET %s = %d WHERE ID LIKE '%s'";

	private Long uidpkCurrentValue;

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		super.init(database);

		final String formattedQuery = String.format(GET_CURRENT_VALUE,
				quoteColumnNames(COLUMN_LAST_VALUE),
				quoteTableName(TABLE_JPA_GENERATED_KEYS), TABLE_LOCALIZED_PROPERTIES);

		final Object uidpkCurrentValueObject = getSingleValueFromDB(formattedQuery);
		if (uidpkCurrentValueObject == null) {
			throw new CustomChangeException(JPA_GENERATED_KEYS_NOT_FOUND);
		}
		uidpkCurrentValue = getLong(uidpkCurrentValueObject);
		final Long attributeCount = getLong(getSingleValueFromDB(GET_TATTRIBUTE_COUNT));
		final List<SqlStatement> results = new ArrayList<>();
		try {
			final long updatedKeyVal = uidpkCurrentValue + attributeCount;
			updateJPAKeyForTLocalizedProperties(updatedKeyVal);
			insertCatalogProductSkuAttributes(database, results);
			insertCustomerProfileAttributes(database, results);
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException(e);
		}
		return results.toArray(new SqlStatement[0]);
	}

	private void insertCustomerProfileAttributes(final Database database, final List<SqlStatement> results)
			throws SQLException, DatabaseException {
		try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet resultSet = statement.executeQuery(QUERY_ATTRIBUTE)) {
				final String locale = Locale.ENGLISH.toString();
				while (resultSet.next()) {
					final InsertStatement insertStatement = prepareInsertStatement(resultSet, database, locale);
					results.add(insertStatement);
					uidpkCurrentValue++;
					LOG.info("Insert customer profile into " + TABLE_LOCALIZED_PROPERTIES + " for " + resultSet.getString(COLUMN_NAME));
				}
			}
		}
	}

	private void insertCatalogProductSkuAttributes(final Database database, final List<SqlStatement> results)
			throws SQLException, DatabaseException {
		try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet resultSet = statement.executeQuery(QUERY_ATTRIBUTE_AND_TCATALOG)) {
				while (resultSet.next()) {
					final String locale = resultSet.getString(DEFAULT_LOCALE);
					final InsertStatement insertStatement = prepareInsertStatement(resultSet, database, locale);
					results.add(insertStatement);
					uidpkCurrentValue++;
					LOG.info("Insert catalog, product, sku attributes into " + TABLE_LOCALIZED_PROPERTIES + " for " + resultSet.getString(COLUMN_NAME));
				}
			}
		}
	}

	private void updateJPAKeyForTLocalizedProperties(final Long updatedKeyVal)
			throws SQLException, DatabaseException {
		try (Statement statement = connection.createStatement()) {
			String formattedUpdateQuery = String.format(UPDATE_JPA_GENERATED_KEYS,
					quoteTableName(TABLE_JPA_GENERATED_KEYS),
					quoteColumnNames(COLUMN_LAST_VALUE), updatedKeyVal, TABLE_LOCALIZED_PROPERTIES);

			final int resultSet = statement.executeUpdate(formattedUpdateQuery);
			if (resultSet == 1) {
				LOG.info("Updated JPA_GENERATED_KEYS LAST_VALUE column with updated count for TLOCALIZEDPROPERTIES entry.");
			}
		}
	}

	private InsertStatement prepareInsertStatement(final ResultSet resultSet, final Database database, final String locale) throws SQLException {
		final Long attributeUidPk = resultSet.getLong(COLUMN_UIDPK);
		final String attributeName = resultSet.getString(COLUMN_NAME);
		final InsertStatement insertStatement = new InsertStatement(database.getDefaultCatalogName(),
				database.getDefaultSchema().getSchemaName(), TABLE_LOCALIZED_PROPERTIES);
		insertStatement.addColumnValue(COLUMN_UIDPK, uidpkCurrentValue);
		insertStatement.addColumnValue(COLUMN_OBJECT_UID, attributeUidPk);
		insertStatement.addColumnValue(COLUMN_LOCALIZED_PROPERTY_KEY, COLUMN_LOCALIZED_PROPERTY_KEY_VALUE + locale);
		insertStatement.addColumnValue(COLUMN_VALUE, attributeName);
		insertStatement.addColumnValue(COLUMN_TYPE, COLUMN_TYPE_VALUE);
		return insertStatement;
	}

	private Object getSingleValueFromDB(final String sql) throws CustomChangeException {
		try (final Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet resultSet = statement.executeQuery(sql)) {
				if (resultSet.next()) {
					return resultSet.getObject(1);
				}
			}
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException(e);
		}
		return null;
	}

	private Long getLong(final Object object) {
		if (object instanceof Long) {
			return (Long) object;
		} else if (object instanceof BigDecimal) {
			return ((BigDecimal) object).longValue();
		} else {
			throw new ClassCastException(VALUE_RETURNED_BY_DB_WITH_UNEXPECTED_TYPE + object.getClass());
		}
	}

	@Override
	public String getConfirmationMessage() {
		return CONFIRMATION_MESSAGE;
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
		return null;
	}
}