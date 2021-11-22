/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package liquibase.ext.elasticpath.migration.modifierfields;

import static liquibase.ext.elasticpath.migration.modifierfields.migrators.DataTableMigrator.DATA_TABLE_PARENTS;
import static liquibase.ext.elasticpath.migration.modifierfields.migrators.DataTableMigrator.DATA_TABLE_QUERIES;
import static liquibase.ext.elasticpath.migration.modifierfields.migrators.DataTableMigrator.UPDATE_SQL;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ClobModifierFieldsTest {
	private static final int DATA_ITEM_KEY = 3;
	private static final int DATA_ITEM_VALUE = 4;

	private static final Long UID_PK_1 = 1L;
	private static final Long UID_PK_2 = 2L;
	private static final Long PARENT_TABLE_UID_PK_1 = 100L;
	private static final Long PARENT_TABLE_UID_PK_2 = 200L;

	private static final String DATA_KEY_1 = "key1";
	private static final String DATA_VALUE_1 = "value1";
	private static final String DATA_KEY_2 = "key2";
	private static final String DATA_VALUE_2 = "value2";
	private static final String EXPECTED_JSON_1 = "{\"key1\":\"value1\"}";
	private static final String EXPECTED_JSON_2 = "{\"key2\":\"value2\"}";
	private static final String EXPECTED_TWO_PROPERTY_JSON = "{\"" + DATA_KEY_1 + "\":\"" + DATA_VALUE_1 + "\",\""
			+ DATA_KEY_2 + "\":\"" + DATA_VALUE_2 + "\"}";

	@Mock
	private JdbcConnection connection;
	@Mock
	private Database database;
	@Mock
	private final PreparedStatement selectStatement = mock(PreparedStatement.class);
	@Mock
	private final PreparedStatement updateStatement = mock(PreparedStatement.class);
	@Mock
	private final ResultSet resultSet = mock(ResultSet.class);

	private ClobModifierFields fixture = new ClobModifierFields();

	@Before
	public void before() throws SQLException {
		when(database.getConnection()).thenReturn(connection);
		when(selectStatement.executeQuery()).thenReturn(resultSet);
		when(updateStatement.executeBatch()).thenReturn(new int[]{1});
	}

	/*
		Testing all 4 cases when only data table name is provided.

		<customChange class="liquibase.ext.elasticpath.migration.modifierfields.ClobModifierFields">
			<param name="tableName">TSHOPPINGITEMDATA</param>
			<param name="customLOBModifierField"/>
			<param name="jsonConverterClassName"/>
		</customChange>
	 */
	@Test
	public void shouldMigrateDataTableRowsWhenDataTableNamesAreCorrect() throws CustomChangeException, DatabaseException, SQLException {
		List<String> dataTableNames = Lists.newArrayList("TCARTDATA", "TORDERDATA", "TORDERITEMDATA", "TSHOPPINGITEMDATA");

		for (String dataTableName : dataTableNames) {
			migrateDataRows(dataTableName);
			validateDataTableMigration();
		}
	}

	/*
		It's possible that more than ClobModifierFields#DEFAULT_BATCH_SIZE records exist for the same parent ID (e.g. ORDER_UID).
		The migrator must fetch all data before serializing to JSON.
	 */
	@Test
	public void shouldMigrateAllBatchesWhenMigratingDataTableForSameParentTableUid() throws SQLException, DatabaseException, CustomChangeException {
		String dataTableName = "TORDERDATA";

		when(connection.prepareStatement(DATA_TABLE_QUERIES.get(dataTableName))).thenReturn(selectStatement);
		String formattedUpdateStatement = String.format(UPDATE_SQL, DATA_TABLE_PARENTS.get(dataTableName));

		when(connection.prepareStatement(formattedUpdateStatement)).thenReturn(updateStatement);

		when(resultSet.next()).thenReturn(true,true, false);
		when(resultSet.getLong(1)).thenReturn(UID_PK_1, UID_PK_1);
		when(resultSet.getLong(2)).thenReturn(PARENT_TABLE_UID_PK_1, PARENT_TABLE_UID_PK_1);
		when(resultSet.getString(DATA_ITEM_KEY)).thenReturn(DATA_KEY_1, DATA_KEY_2);
		when(resultSet.getString(DATA_ITEM_VALUE)).thenReturn(DATA_VALUE_1, DATA_VALUE_2);

		fixture.setTableName(dataTableName);
		fixture.generateStatements(database);

		verify(updateStatement).setString(1, EXPECTED_TWO_PROPERTY_JSON);
		verify(updateStatement).setLong(2, PARENT_TABLE_UID_PK_1);
		verify(updateStatement).addBatch();
		verify(updateStatement).executeBatch();

		verify(connection).commit();
		verify(updateStatement).close();
	}

	@Test
	public void shouldThrowExceptionWhenMigratingDataTableAndWrongDataTableNameIsSpecified() {
		String dataTableName = "SOME_RANDOM_DATA_TABLE_NAME";

		fixture.setTableName(dataTableName);

		assertThatThrownBy(() -> fixture.generateStatements(database))
				.isInstanceOf(CustomChangeException.class);

		verifyZeroInteractions(selectStatement);
		verifyZeroInteractions(updateStatement);
	}

	private void migrateDataRows(final String dataTableName) throws DatabaseException, CustomChangeException, SQLException {
		when(connection.prepareStatement(DATA_TABLE_QUERIES.get(dataTableName))).thenReturn(selectStatement);
		String formattedUpdateStatement = String.format(UPDATE_SQL, DATA_TABLE_PARENTS.get(dataTableName));

		when(connection.prepareStatement(formattedUpdateStatement)).thenReturn(updateStatement);
		when(resultSet.next()).thenReturn(true,true, false);
		when(resultSet.getLong(1)).thenReturn(UID_PK_1, UID_PK_2);
		when(resultSet.getLong(2)).thenReturn(PARENT_TABLE_UID_PK_1, PARENT_TABLE_UID_PK_2);
		when(resultSet.getString(DATA_ITEM_KEY)).thenReturn(DATA_KEY_1, DATA_KEY_2);
		when(resultSet.getString(DATA_ITEM_VALUE)).thenReturn(DATA_VALUE_1, DATA_VALUE_2);

		fixture = new ClobModifierFields();
		fixture.setTableName(dataTableName);
		fixture.generateStatements(database);
	}

	private void validateDataTableMigration() throws SQLException, DatabaseException {
		verify(updateStatement).setString(1, EXPECTED_JSON_1);
		verify(updateStatement).setString(1, EXPECTED_JSON_2);
		verify(updateStatement).setLong(2, PARENT_TABLE_UID_PK_1);
		verify(updateStatement).setLong(2, PARENT_TABLE_UID_PK_2);
		verify(updateStatement, times(2)).addBatch();
		verify(updateStatement, times(2)).executeBatch();

		verify(connection, times(2)).commit();
		verify(updateStatement).close();

		Mockito.clearInvocations(updateStatement, connection);
	}
}
