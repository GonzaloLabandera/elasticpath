/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath.migration.payment2020;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertSetStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.ext.elasticpath.AbstractEpCustomSqlChange;

/**
 * Fills target tables from a given select query.
 * For example for each rows of a select query we need create {@link InsertSetStatement} for each of given target tables which have common context.
 * Lets we have "select column_a, column_b from table" select query. Target table_a uses column_a, target table_b uses column_b
 * and foreign key of table_b refers to table_a.
 */
public abstract class AbstractPaymentConverter extends AbstractEpCustomSqlChange {
	private static final String VALUE_RETURNED_BY_DB_WITH_UNEXPECTED_TYPE = "Value returned by DB with unexpected type: ";
	private static final String TABLE_JPA_GENERATED_KEYS = "JPA_GENERATED_KEYS";
	private static final String COLUMN_LAST_VALUE = "LAST_VALUE";
	private static final String QUERY_RETRIEVE_LATEST_UIDPK_FOR_TARGET_TABLE = "SELECT %s FROM %s WHERE ID = '%s'";
	private static final long DEFAULT_MIN_UIDPK_VALUE = 200000L;

	private Map<String, Long> uidpks = new HashMap<>();
	private Map<String, InsertSetStatement> insertSetStatementMap;

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		super.init(database);

		initInsertStatements(database.getDefaultCatalogName(), database.getDefaultSchema().getSchemaName());
		initUIDPKs();

		try {
			prepareInsertStatements(database);
			return getInsertSetStatementMap().values().stream().toArray(SqlStatement[]::new);
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException(e);
		}
	}

	protected Map<String, InsertSetStatement> getInsertSetStatementMap() {
		return insertSetStatementMap;
	}

	protected Map<String, Long> getUidpks() {
		return uidpks;
	}

	protected abstract List<String> getTargetTables();

	protected abstract String getSelectDataQuery();

	/**
	 * Creates empty {@link InsertSetStatement} for each target table.
	 *
	 * @param catalogName database default catalog name.
	 * @param schemaName  database default schema name.
	 */
	private void initInsertStatements(final String catalogName, final String schemaName) {
		insertSetStatementMap = getTargetTables().stream()
				.map(table -> new AbstractMap.SimpleEntry<>(table, new InsertSetStatement(catalogName, schemaName, table)))
				.collect(Collectors.toMap(Map.Entry::getKey,
						Map.Entry::getValue,
						(existing, replacement) -> {
							throw new IllegalStateException(String.format("Duplicate key %s", existing));
						},
						LinkedHashMap::new));
	}

	/**
	 * Generic util to convert data set to insert statement for sql execution.
	 *
	 * @param catalogName is database default catalog name.
	 * @param schemaName  is database default schema name.
	 * @param table       is database table.
	 * @param records
	 * @return
	 */
	protected InsertStatement createInsertStatementByRecordSet(final String catalogName,
															   final String schemaName,
															   final String table,
															   final Map<String, Object> records) {
		final InsertStatement insertStatement = new InsertStatement(catalogName, schemaName, table);
		records.forEach(insertStatement::addColumnValue);

		return insertStatement;
	}

	/**
	 * Collects/retrieves all information necessary for creating new tables record set.
	 *
	 * @param resultSet is result set of select query.
	 * @return map data to create {@link InsertSetStatement}.
	 * @throws SQLException in case no valid key of result set.
	 */
	protected abstract Map<String, Object> createRecordSetContext(ResultSet resultSet) throws SQLException;

	/**
	 * Fills {@link InsertSetStatement} for each target table.	 *
	 *
	 * @param database encapsulates database support.
	 * @throws SQLException      in case exception of executing select query.
	 * @throws DatabaseException in case exception of executing select query.
	 */
	protected abstract void prepareInsertStatements(Database database) throws SQLException, DatabaseException;

	/**
	 * Increments uid primary keys of target tables to use them for creating {@link InsertSetStatement}.
	 */
	protected void incrementUidPKForAllTargetTables() {
		uidpks.replaceAll((k, v) -> ++v);
	}

	private void initUIDPKs() throws CustomChangeException {
		for (final String table : getTargetTables()) {
			uidpks.put(table, getLatestUIDPKForTable(table));
		}
	}

	/**
	 * Sets last primary key value for target table by obtaining it from JPA_GENERATED_KEYS table.
	 * Sets value to default if there is not such value in the database.
	 *
	 * @param table    is target table.
	 * @return latest UIDPK for table
	 * @throws CustomChangeException in case exception during obtaining data from database.
	 */
	private Long getLatestUIDPKForTable(final String table) throws CustomChangeException {
		final String formattedQuery = String.format(QUERY_RETRIEVE_LATEST_UIDPK_FOR_TARGET_TABLE,
				quoteColumnNames(COLUMN_LAST_VALUE),
				quoteTableName(TABLE_JPA_GENERATED_KEYS), table);

		final Object uidpkCurrentValue = getSingleValueFromDB(formattedQuery);

		return Optional.ofNullable(uidpkCurrentValue)
				.map(this::getLong)
				.orElse(DEFAULT_MIN_UIDPK_VALUE);
	}

	private Object getSingleValueFromDB(final String sql) throws CustomChangeException {
		try (final PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
			try (final ResultSet resultSet = statement.executeQuery()) {
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

}
