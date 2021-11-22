/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package liquibase.ext.elasticpath.migration.modifierfields.migrators;

import static com.elasticpath.persistence.openjpa.util.ModifierFieldsMapper.toJSON;
import static liquibase.ext.elasticpath.migration.modifierfields.ClobModifierFields.DEFAULT_BATCH_SIZE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import com.elasticpath.domain.misc.types.ModifierFieldsMapWrapper;

/**
 * Migrates data table rows to CLOBs.
 * E.g. TORDERDATA rows to TORDER.MODIFIER_FIELDS
 */
public class DataTableMigrator implements AutoCloseable {

	private static final Logger LOG = LogFactory.getInstance().getLog();
	private static final int DATA_ITEM_KEY = 3;
	private static final int DATA_ITEM_VALUE = 4;
	/** The update SQL statement. */
	public static final String UPDATE_SQL = "UPDATE %s SET HAS_MODIFIERS=1, MODIFIER_FIELDS = ? WHERE UIDPK = ?";
	/** The mapping between data and parent tables e.g. TORDERDATA -> TORDER */
	public static final Map<String, String> DATA_TABLE_PARENTS = new HashMap<>(4);
	/** The mapping between data tables and SELECT SQLs. */
	public static final Map<String, String> DATA_TABLE_QUERIES = new HashMap<>(4);

	static {
		DATA_TABLE_PARENTS.put("TORDERDATA", "TORDER");
		DATA_TABLE_PARENTS.put("TCARTDATA", "TSHOPPINGCART");
		DATA_TABLE_PARENTS.put("TORDERITEMDATA", "TORDERSKU");
		DATA_TABLE_PARENTS.put("TSHOPPINGITEMDATA", "TCARTITEM");

		DATA_TABLE_QUERIES.put("TORDERDATA", "SELECT UIDPK, ORDER_UID, ITEM_KEY, ITEM_VALUE FROM TORDERDATA "
				+ "WHERE UIDPK > ? ORDER BY ORDER_UID");
		DATA_TABLE_QUERIES.put("TCARTDATA", "SELECT UIDPK, SHOPPING_CART_UID, DATA_KEY, VALUE FROM TCARTDATA "
				+ "WHERE UIDPK > ? ORDER BY SHOPPING_CART_UID");
		DATA_TABLE_QUERIES.put("TORDERITEMDATA", "SELECT UIDPK, ORDERSKU_UID, ITEM_KEY, ITEM_VALUE FROM TORDERITEMDATA "
				+ "WHERE UIDPK > ? ORDER BY ORDERSKU_UID");
		DATA_TABLE_QUERIES.put("TSHOPPINGITEMDATA", "SELECT UIDPK, CARTITEM_UID, ITEM_KEY, ITEM_VALUE FROM TSHOPPINGITEMDATA "
				+ "WHERE UIDPK > ? ORDER BY CARTITEM_UID");
	}

	private final StopWatch stopWatch = new StopWatch();
	private final JdbcConnection connection;

	private PreparedStatement selectStatement;
	private PreparedStatement updateStatement;

	private final String dataTableName;
	private final String parentTableName;
	private int totalMigratedRecords;

	/**
	 * Custom constructor.
	 *
	 * @param connection the JDBC connection object.
	 * @param dataTableName the data table name.
	 */
	public DataTableMigrator(final JdbcConnection connection, final String dataTableName) {
		this.connection = connection;
		this.dataTableName = dataTableName;
		this.parentTableName = DATA_TABLE_PARENTS.get(this.dataTableName);
	}

	/**
	 * Migrate *DATA tables to corresponding MODIFIER_FIELDS field in TSHOPPINGCART, TORDER, TCARITEM and TORDERSKU tables.
	 * @throws DatabaseException the database exception.
	 * @throws SQLException the sql exception
	 * @throws CustomChangeException the custom change exception.
	 */
	public void migrate() throws DatabaseException, SQLException, CustomChangeException {
		stopWatch.start();

		validateParentTableName();

		prepareSelectForDataTableMigration();
		prepareUpdateStatement(parentTableName);

		long uidCursor = 0L;
		//must remember the last parent table uidPK because not all records may be fetched in a single batch
		long lastKnownParentUidPk = 0L;
		//map parent uidPk to key-value pairs
		Map<Long, ModifierFieldsMapWrapper> dataRows = new HashMap<>();

		boolean recordsFound;

		do {
			recordsFound = false;
			selectStatement.setLong(1, uidCursor);

			try (ResultSet resultSet = selectStatement.executeQuery()) {
				//process current batch
				while (resultSet.next()) {
					recordsFound = true;

					uidCursor = resultSet.getLong(1);
					long parentTableUidPk = resultSet.getLong(2);
					lastKnownParentUidPk = parentTableUidPk;

					convertResultsetToMap(resultSet, dataRows, parentTableUidPk);
				}
			}

			if (recordsFound) {
				//remove the last known records to process current batch
				ModifierFieldsMapWrapper lastKnownDataRecords = dataRows.remove(lastKnownParentUidPk);

				serializeAndSaveToDb(dataRows);

				dataRows.put(lastKnownParentUidPk, lastKnownDataRecords);
			}
		} while (recordsFound);

		//serialize and update the last set of fields
		serializeAndSaveToDb(dataRows);

		stopWatch.stop();
	}

	private void validateParentTableName() throws CustomChangeException {
		if (parentTableName == null) {
			throw new CustomChangeException("Unsupported data table [" + this.dataTableName + "]. Supported tables are: "
					+ DATA_TABLE_PARENTS.keySet());
		}
		LOG.info("Migrating data from [" + this.dataTableName + "] to [" + parentTableName + ".MODIFIER_FIELDS CLOB] ...");
	}

	private void prepareSelectForDataTableMigration() throws DatabaseException, SQLException {
		selectStatement = connection.prepareStatement(DATA_TABLE_QUERIES.get(this.dataTableName));
		selectStatement.setMaxRows(DEFAULT_BATCH_SIZE);
	}

	private void prepareUpdateStatement(final String tableName) throws DatabaseException {
		updateStatement = connection
				.prepareStatement(String.format(UPDATE_SQL, tableName));
	}

	private void serializeAndSaveToDb(final Map<Long, ModifierFieldsMapWrapper> dataRows)
			throws SQLException, DatabaseException, CustomChangeException {

		Pair<Integer, Integer> recordStats = jsonDataRowsAndPrepareBatch(dataRows);
		updateParentTableAndDeleteOldDataRows(recordStats);
		dataRows.clear();
	}

	private void convertResultsetToMap(final ResultSet resultSet, final Map<Long, ModifierFieldsMapWrapper> dataRows, final long parentTableUidPk)
			throws SQLException {

		ModifierFieldsMapWrapper modifierFields = dataRows.computeIfAbsent(parentTableUidPk, key -> new ModifierFieldsMapWrapper());
		String key = resultSet.getString(DATA_ITEM_KEY);
		String value = resultSet.getString(DATA_ITEM_VALUE);

		modifierFields.put(key, value);
	}

	//serialize modifier fields to JSON and prepare a batch of updates
	//returns a pair of stats: the number of old data records and the number of records to update in the parent table
	private Pair<Integer, Integer> jsonDataRowsAndPrepareBatch(final Map<Long, ModifierFieldsMapWrapper> dataRows) throws SQLException {

		int numOfDataRecords = 0;
		int numOfParentRecordsToUpdate = 0;

		for (Map.Entry<Long, ModifierFieldsMapWrapper> entry : dataRows.entrySet()) {

			Long parentUidPk = entry.getKey();

			ModifierFieldsMapWrapper modifierFields = entry.getValue();
			String json = toJSON(modifierFields);

			updateStatement.setString(1, json);
			updateStatement.setLong(2, parentUidPk);
			updateStatement.addBatch();

			numOfDataRecords += modifierFields.getMap().size();
			numOfParentRecordsToUpdate++;
		}

		return Pair.of(numOfDataRecords, numOfParentRecordsToUpdate);
	}

	//execute the batch and delete old data rows
	private void updateParentTableAndDeleteOldDataRows(final Pair<Integer, Integer> pairOfNumOfRecords)
			throws SQLException, DatabaseException, CustomChangeException {

		int numOfDataRecords = pairOfNumOfRecords.getLeft();

		if (numOfDataRecords > 0) {
			int expectedNumOfParentRecordsToUpdate = pairOfNumOfRecords.getRight();

			int[] updatedRecords = updateStatement.executeBatch();
			int actualNumberOfUpdatedParentRecords = updatedRecords.length;

			if (updatedRecords.length != expectedNumOfParentRecordsToUpdate) {
				throw new CustomChangeException("The actual number of updated parent table [" + parentTableName
						+ "] records [" + actualNumberOfUpdatedParentRecords + "] doesn't match the expected one ["
						+ expectedNumOfParentRecordsToUpdate + "]");
			}

			connection.commit();

			totalMigratedRecords += numOfDataRecords;
		}
	}

	/**
	 * Prints the migration statistics.
	 */
	public void printStats() {
		LOG.info("The total of [" + this.totalMigratedRecords + "] records from [" + this.dataTableName
				+ "] table have been migrated and completed in [" + stopWatch.getTime() + "] ms");
	}

	private void closePreparedStatement(final PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (final Exception exception) {
				LOG.warning("Exception during closing statement", exception);
			}
		}
	}

	@Override
	public void close() {
		closePreparedStatement(selectStatement);
		closePreparedStatement(updateStatement);
	}
}
