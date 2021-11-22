/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package liquibase.ext.elasticpath;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDuplicateOrdersTest {

	@Mock
	private Database database;

	@InjectMocks
	private UpdateDuplicateOrders updateDuplicateOrders;

	@Mock
	private JdbcConnection connection;

	@Mock
	private PreparedStatement preparedStatement;

	@Mock
	private ResultSet resultSet;

	@Mock
	private PreparedStatement getDuplicateCartOrderGuidsPreparedStatement;

	@Mock
	private ResultSet getDuplicateCartOrderGuidsResultSet;

	@Mock
	private PreparedStatement getOrderInfoPreparedStatement;

	@Mock
	private ResultSet getOrderInfoResultSet;

	@Mock
	private PreparedStatement updateOrderPreparedStatement;

	private static final String SELECT_DUPLICATE_ORDER_GUIDS =
			"SELECT CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IS NOT NULL GROUP BY CART_ORDER_GUID HAVING COUNT(*) > 1";

	@Before
	public void init() throws SQLException {
		when(database.getConnection()).thenReturn(connection);
	}

	@Test
	public void testUpdateMultipleDuplicateOrders() throws DatabaseException, SQLException, CustomChangeException {
		final int numberOfDuplicateCartOrderGuids = 5;
		final int batchSize = 10;
		final String uidpk = "uidpk";
		final String orderNumber = "order_number";
		final String cartOrderGuid = "cart_order_guid";

		setupMultipleDuplicateCartOrderGuidsResponse(numberOfDuplicateCartOrderGuids, cartOrderGuid, batchSize);
		setupMultipleOrderResponseForCartOrderGuids(uidpk, orderNumber, cartOrderGuid, numberOfDuplicateCartOrderGuids);
		setupSingleBatchAddAndClear();

		updateDuplicateOrders.setBatchSize(batchSize);
		updateDuplicateOrders.execute(database);

		verifyUpdateWasMadeForOrders(numberOfDuplicateCartOrderGuids, uidpk, orderNumber, cartOrderGuid);
		verify(updateOrderPreparedStatement, times(numberOfDuplicateCartOrderGuids)).addBatch();
		verify(updateOrderPreparedStatement, times(1)).executeBatch();
		verify(updateOrderPreparedStatement, times(1)).clearBatch();
	}

	@Test
	public void testUpdateMultipleDuplicateOrdersSizeGreaterThanBatchSize() throws DatabaseException, SQLException, CustomChangeException {
		final int numberOfDuplicateCartOrderGuids = 15;
		final int batchSize = 10;
		final String uidpk = "uidpk";
		final String orderNumber = "order_number";
		final String cartOrderGuid = "cart_order_guid";

		setupMultipleDuplicateCartOrderGuidsResponse(numberOfDuplicateCartOrderGuids, cartOrderGuid, batchSize);
		setupMultipleOrderResponseForCartOrderGuids(uidpk, orderNumber, cartOrderGuid, batchSize);
		setupSingleBatchAddAndClear();

		updateDuplicateOrders.setBatchSize(batchSize);
		updateDuplicateOrders.execute(database);

		verifyUpdateWasMadeForOrders(batchSize, uidpk, orderNumber, cartOrderGuid);
		verify(updateOrderPreparedStatement, times(batchSize)).addBatch();
		verify(updateOrderPreparedStatement, times(1)).executeBatch();
		verify(updateOrderPreparedStatement, times(1)).clearBatch();
	}

	@Test
	public void testUpdateSingleDuplicateOrder() throws DatabaseException, SQLException, CustomChangeException {
		final String uidpk = "uidpk";
		final String orderNumber = "order_number";
		final String cartOrderGuid = "cart_order_guid";
		setupSingleDuplicateOrdersResponse(cartOrderGuid);
		setupSingleOrderResponseForCartOrderGuid(uidpk, orderNumber, cartOrderGuid);
		setupSingleBatchAddAndClear();

		updateDuplicateOrders.setBatchSize(10);
		updateDuplicateOrders.execute(database);

		verifySingleOrderWasUpdated(cartOrderGuid, uidpk, orderNumber);
		verifySingleDuplicateOrdersCallInfo(cartOrderGuid);
		verifyNoMoreInteractions(getOrderInfoPreparedStatement);
	}

	@Test
	public void testBatchQueriesWithNoDuplicates() throws DatabaseException, SQLException, CustomChangeException {
		setupNoDuplicateOrdersResponse();

		updateDuplicateOrders.setBatchSize(10);
		updateDuplicateOrders.execute(database);

		verify(connection, times(1)).prepareStatement(SELECT_DUPLICATE_ORDER_GUIDS);
		verifyNoMoreInteractions(getOrderInfoPreparedStatement);
	}

	private void verifyUpdateWasMadeForOrders(final int times, final String uidPkPrefix, final String orderNumberPrefix,
			final String cartOrderGuidPrefix) throws SQLException {
		for (int i = 1; i <= times; i++) {
			verify(updateOrderPreparedStatement).setString(1, orderNumberPrefix + i + "-dup-" + cartOrderGuidPrefix + i);
			verify(updateOrderPreparedStatement).setString(2, uidPkPrefix + i);
		}
	}

	private void setupMultipleDuplicateCartOrderGuidsResponse(final int numberOfDuplicates, final String cartOrderGuidPrefix,
			final int batchSize) throws SQLException, DatabaseException {

		when(connection.prepareStatement(String.format(SELECT_DUPLICATE_ORDER_GUIDS)))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		//doNothing().when(getDuplicateCartOrderGuidsPreparedStatement).setMaxRows(anyInt());
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);

		int numberOfDuplicateGuidsAsPerBatchSize = (numberOfDuplicates > batchSize) ? batchSize : numberOfDuplicates;
		when(getDuplicateCartOrderGuidsResultSet.next()).thenAnswer(answerTrueNTimes(numberOfDuplicateGuidsAsPerBatchSize));
		when(getDuplicateCartOrderGuidsResultSet.getString(1)).thenAnswer(new Answer<String>() {
			private int count = 0;

			@Override
			public String answer(final InvocationOnMock invocationOnMock) throws Throwable {
				if (count >= numberOfDuplicates) {
					return cartOrderGuidPrefix + count;
				} else {
					count++;
					return cartOrderGuidPrefix + count;
				}
			}
		});
	}

	private void setupSingleBatchAddAndClear() throws DatabaseException, SQLException {
		when(connection.prepareStatement("UPDATE TORDER SET CART_ORDER_GUID = ? WHERE UIDPK = ?")).thenReturn(updateOrderPreparedStatement);
		doNothing().when(updateOrderPreparedStatement).addBatch();
		when(updateOrderPreparedStatement.executeBatch()).thenReturn(new int[1]);
		doNothing().when(updateOrderPreparedStatement).clearBatch();
	}

	private void setupSingleOrderResponseForCartOrderGuid(final String uidPk, final String orderNumber, final String cartOrderGuid)
			throws DatabaseException,
			SQLException {
		when(connection
				.prepareStatement("SELECT UIDPK,ORDER_NUMBER,CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IN ('" + cartOrderGuid + "')"))
				.thenReturn(getOrderInfoPreparedStatement);
		when(getOrderInfoPreparedStatement.executeQuery()).thenReturn(getOrderInfoResultSet);
		when(getOrderInfoResultSet.next()).thenReturn(true).thenReturn(false);
		when(getOrderInfoResultSet.getString(1)).thenReturn(uidPk);
		when(getOrderInfoResultSet.getString(2)).thenReturn(orderNumber);
		when(getOrderInfoResultSet.getString(3)).thenReturn(cartOrderGuid);
	}

	private void verifySingleDuplicateOrdersCallInfo(final String cartOrderGuid) throws DatabaseException, SQLException {
		verify(connection)
				.prepareStatement("SELECT UIDPK,ORDER_NUMBER,CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IN ('" + cartOrderGuid + "')");
		verify(getOrderInfoPreparedStatement).executeQuery();
		verify(getOrderInfoPreparedStatement).close();
	}

	private void verifySingleOrderWasUpdated(final String cartOrderGuid, final String uidpk, final String orderNumber) throws SQLException,
			DatabaseException {
		verify(updateOrderPreparedStatement).setString(1, orderNumber + "-dup-" + cartOrderGuid);
		verify(updateOrderPreparedStatement).setString(2, uidpk);
		verify(updateOrderPreparedStatement).addBatch();
		verify(updateOrderPreparedStatement).close();
		verify(connection).prepareStatement("UPDATE TORDER SET CART_ORDER_GUID = ? WHERE UIDPK = ?");
	}

	private void setupSingleDuplicateOrdersResponse(final String cartOrderGuid) throws SQLException,
			DatabaseException {
		when(connection.prepareStatement(contains(SELECT_DUPLICATE_ORDER_GUIDS)))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);
		when(getDuplicateCartOrderGuidsResultSet.next()).thenReturn(true).thenReturn(false);
		when(getDuplicateCartOrderGuidsResultSet.getString(1)).thenReturn(cartOrderGuid);
	}

	private void setupNoDuplicateOrdersResponse() throws SQLException, DatabaseException {
		when(connection.prepareStatement(
				contains(SELECT_DUPLICATE_ORDER_GUIDS)))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);
		when(getDuplicateCartOrderGuidsResultSet.next()).thenReturn(false);
	}

	private Answer<Boolean> answerTrueNTimes(final int times) {
		return new Answer<Boolean>() {
			private int count = 0;

			@Override
			public Boolean answer(final InvocationOnMock invocationOnMock) throws Throwable {
				if (count >= times) {
					return false;
				} else {
					count++;
					return true;
				}
			}
		};
	}

	private void setupMultipleOrderResponseForCartOrderGuids(final String uidpkPrefix,
			final String orderNumberPrefix, final String cartorderGuidPrefix, final int numberOfDuplicateCartOrderGuids) throws DatabaseException,
			SQLException {
		when(connection
				.prepareStatement(contains("SELECT UIDPK,ORDER_NUMBER,CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IN (")))
				.thenReturn(getOrderInfoPreparedStatement);
		when(getOrderInfoPreparedStatement.executeQuery()).thenReturn(getOrderInfoResultSet);
		when(getOrderInfoResultSet.next()).thenAnswer(answerTrueNTimes(numberOfDuplicateCartOrderGuids));

		when(getOrderInfoResultSet.getString(1)).thenAnswer(returnStringPrefix(uidpkPrefix));
		when(getOrderInfoResultSet.getString(2)).thenAnswer(returnStringPrefix(orderNumberPrefix));
		when(getOrderInfoResultSet.getString(3)).thenAnswer(returnStringPrefix(cartorderGuidPrefix));
	}

	private Answer<String> returnStringPrefix(final String prefix) {
		return new Answer<String>() {
			private int count = 0;

			@Override
			public String answer(final InvocationOnMock invocationOnMock) throws Throwable {
				count++;
				return prefix + count;
			}
		};
	}
}