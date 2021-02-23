/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import static liquibase.ext.elasticpath.PopulateCustomerType.SELECT_ANONYMOUS_CUSTOMERS;
import static liquibase.ext.elasticpath.PopulateCustomerType.SELECT_REGISTERED_CUSTOMERS;
import static liquibase.ext.elasticpath.PopulateCustomerType.UPDATE_TO_REGISTERED_USER;
import static liquibase.ext.elasticpath.PopulateCustomerType.UPDATE_TO_SINGLE_SESSION_USER;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.ext.elasticpath.util.CustomTaskChangeTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PopulateCustomerTypeTest  {

	@InjectMocks
	private PopulateCustomerType populateCustomerType;

	@Mock
	private JdbcConnection connection;

	@Mock
	private PreparedStatement selectAnonymousCustomersPreparedStatement;
	@Mock
	private PreparedStatement updateToSingleSessionUserPreparedStatement;
	@Mock
	private ResultSet anonymousCustomersResultSet;

	@Mock
	private PreparedStatement selectRegisteredCustomersPreparedStatement;
	@Mock
	private PreparedStatement updateToRegisteredUserPreparedStatement;
	@Mock
	private ResultSet registeredCustomersResultSet;

	private CustomTaskChangeTestUtil taskChangeUtil;


	@Before
	public void populateCustomerTypeTestSetUp() {
		taskChangeUtil = new CustomTaskChangeTestUtil(connection);
	}

	@Test
	public void testPopulateAnonymousCustomerType() throws DatabaseException, SQLException, CustomChangeException {
		final int batchSize = 10;
		final int numberOfAnonymousCustomers = 5;
		final long anonymousCustomerUid = 10000;

		setupMigrateCustomers(selectAnonymousCustomersPreparedStatement, anonymousCustomersResultSet, SELECT_ANONYMOUS_CUSTOMERS);
		int customersToProcess = taskChangeUtil.getNumberOfRegistersToProcess(batchSize, numberOfAnonymousCustomers);
		taskChangeUtil.setupUpdateRegistersBatchResponse(anonymousCustomersResultSet, customersToProcess, anonymousCustomerUid);
		taskChangeUtil.setupUpdateBatchAddAndClear(UPDATE_TO_SINGLE_SESSION_USER, updateToSingleSessionUserPreparedStatement);

		populateCustomerType.setBatchSize(batchSize);
		populateCustomerType.migrateCustomers(connection, SELECT_ANONYMOUS_CUSTOMERS, UPDATE_TO_SINGLE_SESSION_USER);

		taskChangeUtil.verifyUpdateWasMadeForRegisters(updateToSingleSessionUserPreparedStatement, numberOfAnonymousCustomers, anonymousCustomerUid);
		verify(updateToSingleSessionUserPreparedStatement, times(numberOfAnonymousCustomers)).addBatch();
		verify(updateToSingleSessionUserPreparedStatement, times(1)).executeBatch();
		verify(updateToSingleSessionUserPreparedStatement, times(1)).clearBatch();
	}

	@Test
	public void testPopulateAnonymousCustomerTypeSizeGreaterThanBatchSize() throws DatabaseException, SQLException, CustomChangeException {
		final int batchSize = 10;
		final int numberOfAnonymousCustomers = 25;
		final long anonymousCustomerUid = 10000;

		int executedBatches = testPopulateCustomerTypeSizeGreaterThanBatchSize(batchSize, numberOfAnonymousCustomers, anonymousCustomerUid,
				selectAnonymousCustomersPreparedStatement, anonymousCustomersResultSet, SELECT_ANONYMOUS_CUSTOMERS,
				updateToSingleSessionUserPreparedStatement, UPDATE_TO_SINGLE_SESSION_USER);

		taskChangeUtil.verifyUpdateWasMadeForRegisters(updateToSingleSessionUserPreparedStatement, numberOfAnonymousCustomers, anonymousCustomerUid);
		verify(updateToSingleSessionUserPreparedStatement, times(numberOfAnonymousCustomers)).addBatch();
		verify(updateToSingleSessionUserPreparedStatement, times(executedBatches)).executeBatch();
		verify(updateToSingleSessionUserPreparedStatement, times(executedBatches)).clearBatch();
	}

	@Test
	public void testPopulateRegisteredCustomerType() throws DatabaseException, SQLException, CustomChangeException {
		final int batchSize = 10;
		final int numberOfRegisteredUsers = 5;
		final long registeredCustomerUid = 20000;

		setupMigrateCustomers(selectRegisteredCustomersPreparedStatement, registeredCustomersResultSet, SELECT_REGISTERED_CUSTOMERS);
		int customersToProcess = taskChangeUtil.getNumberOfRegistersToProcess(batchSize, numberOfRegisteredUsers);
		taskChangeUtil.setupUpdateRegistersBatchResponse(registeredCustomersResultSet,	customersToProcess, registeredCustomerUid);
		taskChangeUtil.setupUpdateBatchAddAndClear(UPDATE_TO_REGISTERED_USER, updateToRegisteredUserPreparedStatement);

		populateCustomerType.setBatchSize(batchSize);
		populateCustomerType.migrateCustomers(connection, SELECT_REGISTERED_CUSTOMERS, UPDATE_TO_REGISTERED_USER);

		taskChangeUtil.verifyUpdateWasMadeForRegisters(updateToRegisteredUserPreparedStatement, numberOfRegisteredUsers, registeredCustomerUid);
		verify(updateToRegisteredUserPreparedStatement, times(numberOfRegisteredUsers)).addBatch();
		verify(updateToRegisteredUserPreparedStatement, times(1)).executeBatch();
		verify(updateToRegisteredUserPreparedStatement, times(1)).clearBatch();
	}

	@Test
	public void testPopulateRegisteredCustomerTypeGreaterThanBatchSize() throws DatabaseException, SQLException, CustomChangeException {
		final int batchSize = 10;
		final int numberOfRegisteredCustomers = 25;
		final long registeredCustomerUid = 20000;

		int executedBatches = testPopulateCustomerTypeSizeGreaterThanBatchSize(batchSize, numberOfRegisteredCustomers, registeredCustomerUid,
				selectRegisteredCustomersPreparedStatement, registeredCustomersResultSet, SELECT_REGISTERED_CUSTOMERS,
				updateToRegisteredUserPreparedStatement, UPDATE_TO_REGISTERED_USER);

		taskChangeUtil.verifyUpdateWasMadeForRegisters(updateToRegisteredUserPreparedStatement, batchSize, registeredCustomerUid);
		verify(updateToRegisteredUserPreparedStatement, times(numberOfRegisteredCustomers)).addBatch();
		verify(updateToRegisteredUserPreparedStatement, times(executedBatches)).executeBatch();
		verify(updateToRegisteredUserPreparedStatement, times(executedBatches)).clearBatch();
	}

	private int testPopulateCustomerTypeSizeGreaterThanBatchSize(int batchSize, int pendingToProcess, long currentAnonymousCustomerUid,
			 final PreparedStatement selectStatement, final ResultSet customersResultSet, final String selectSQL,
			 final PreparedStatement updateStatement, final String updateSQL) throws DatabaseException, SQLException, CustomChangeException {

		if (pendingToProcess < batchSize) {
			fail("'pendingToProcess' must be grater than 'batchSize'");
		}

		int executedBatches = 0;
		while (pendingToProcess >= 0) {
			int customersToProcess = taskChangeUtil.getNumberOfRegistersToProcess(batchSize, pendingToProcess);

			setupMigrateCustomers(selectStatement, customersResultSet, selectSQL);
			taskChangeUtil.setupUpdateRegistersBatchResponse(customersResultSet, customersToProcess, currentAnonymousCustomerUid);
			taskChangeUtil.setupUpdateBatchAddAndClear(updateSQL, updateStatement);

			populateCustomerType.setBatchSize(batchSize);
			populateCustomerType.migrateCustomers(connection, selectSQL, updateSQL);

			pendingToProcess -= batchSize;
			currentAnonymousCustomerUid += customersToProcess;
			executedBatches++;
		}
		return executedBatches;
	}

	private void setupMigrateCustomers(final PreparedStatement selectStatement, final ResultSet customersResultSet, final String selectSQL)
			throws DatabaseException, SQLException {

		when(connection.prepareStatement(String.format(selectSQL))).thenReturn(selectStatement);
		when(selectStatement.executeQuery()).thenReturn(customersResultSet);
	}

}
