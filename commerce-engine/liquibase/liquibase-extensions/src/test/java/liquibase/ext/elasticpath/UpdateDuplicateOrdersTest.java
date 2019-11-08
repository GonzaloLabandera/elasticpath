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
	private PreparedStatement highestPreparedStatement;

	@Mock
	private ResultSet highestResultSet;

	@Mock
	private PreparedStatement lowestPreparedStatement;

	@Mock
	private ResultSet lowestResultSet;

	@Mock
	private PreparedStatement updateOrderPreparedStatement;

	private static final String SELECT_DUPLICATE_ORDERS_STATEMENT =
			"SELECT CART_ORDER_GUID FROM TORDER WHERE UIDPK BETWEEN %s AND %s GROUP BY CART_ORDER_GUID HAVING COUNT(*) > 1";

	@Before
	public void init() throws SQLException {
		when(database.getConnection()).thenReturn(connection);
		when(preparedStatement.executeQuery()).thenReturn(resultSet);
	}

	@Test
	public void testUpdateMultipleDuplicateOrders() throws DatabaseException, SQLException, CustomChangeException {
		final long low = 1;
		final long high = 11;
		final int numberOfDuplicateCartOrderGuids = 5;
		final String uidpk = "uidpk";
		final String orderNumber = "order_number";
		final String cartOrderGuid = "cart_order_guid";

		setupNonEmptyTableResponse();
		setupLowestAndHighestResponse(low, high);
		setupMultipleDuplicateCartOrdersResponse(low, high, numberOfDuplicateCartOrderGuids, cartOrderGuid);
		setupMultipleOrderResponseForCartOrderGuids(uidpk, orderNumber, cartOrderGuid, numberOfDuplicateCartOrderGuids);
		setupSingleBatchAddAndClear();

		updateDuplicateOrders.setBatchSize(10);
		updateDuplicateOrders.setBatchOverlap(0);
		updateDuplicateOrders.execute(database);

		verifyEmptyTableCheck();
		verifyHighAndLowQuery();
		verifyGetDuplicateOrderIdsCallWithRange(1, 1, 11);
		verifyUpdateWasMadeForOrders(numberOfDuplicateCartOrderGuids, uidpk, orderNumber, cartOrderGuid);
		verify(updateOrderPreparedStatement, times(numberOfDuplicateCartOrderGuids)).addBatch();
		verify(updateOrderPreparedStatement, times(2)).executeBatch();
		verify(updateOrderPreparedStatement, times(2)).clearBatch();
	}

	@Test
	public void testUpdateSingleDuplicateOrder() throws DatabaseException, SQLException, CustomChangeException {
		final long low = 1;
		final long high = 11;
		final String uidpk = "uidpk";
		final String orderNumber = "order_number";
		final String cartOrderGuid = "cart_order_guid";
		setupNonEmptyTableResponse();
		setupLowestAndHighestResponse(low, high);
		setupSingleDuplicateOrdersResponse(low, high, cartOrderGuid);
		setupSingleOrderResponseForCartOrderGuid(uidpk, orderNumber, cartOrderGuid);
		setupSingleBatchAddAndClear();

		updateDuplicateOrders.setBatchSize(10);
		updateDuplicateOrders.setBatchOverlap(0);
		updateDuplicateOrders.execute(database);

		verifyEmptyTableCheck();
		verifyHighAndLowQuery();
		verifyGetDuplicateOrderIdsCallWithRange(1, 1, 11);
		verifySingleOrderWasUpdated(cartOrderGuid, uidpk, orderNumber);
		verifySingleDuplicateOrdersCallInfo(cartOrderGuid);
		verifyNoMoreInteractions(connection);
	}

	@Test
	public void testBatchQueriesWithNoDuplicates() throws DatabaseException, SQLException, CustomChangeException {
		final long low = 1;
		final long high = 50;
		setupNonEmptyTableResponse();
		setupLowestAndHighestResponse(low, high);
		setupNoDuplicateOrdersResponseForAnyRange();

		updateDuplicateOrders.setBatchSize(10);
		updateDuplicateOrders.setBatchOverlap(1);
		updateDuplicateOrders.execute(database);

		verifyEmptyTableCheck();
		verifyHighAndLowQuery();
		verifyGetDuplicateOrderIdsCallWithRange(5, 1, 12);
		verifyGetDuplicateOrderIdsCallWithRange(5, 11, 22);
		verifyGetDuplicateOrderIdsCallWithRange(5, 21, 32);
		verifyGetDuplicateOrderIdsCallWithRange(5, 31, 42);
		verifyGetDuplicateOrderIdsCallWithRange(5, 41, 52);
		verifyNoMoreInteractions(connection);
	}

	@Test
	public void testNoUpdatesAreMadeWithNoDuplicates() throws DatabaseException, SQLException, CustomChangeException {
		final long low = 1;
		final long high = 11;
		setupNonEmptyTableResponse();
		setupLowestAndHighestResponse(low, high);
		setupNoDuplicateOrdersResponse(low, high);

		updateDuplicateOrders.setBatchOverlap(0);
		updateDuplicateOrders.setBatchSize(10);
		updateDuplicateOrders.execute(database);

		verifyEmptyTableCheck();
		verifyHighAndLowQuery();
		verifyGetDuplicateOrderIdsCallWithRange(1, low, high);
		verifyNoMoreInteractions(connection);
	}

	@Test
	public void testNothingHappensWithAnEmptyDatabase() throws DatabaseException, SQLException, CustomChangeException {
		setupEmptyTableResponse();

		updateDuplicateOrders.execute(database);

		verifyEmptyTableCheck();
		verifyNoMoreInteractions(connection);
	}

	private void verifyUpdateWasMadeForOrders(final int times, final String uidPkPrefix, final String orderNumberPrefix,
			final String cartOrderGuidPrefix) throws SQLException {
		for (int i = 1; i <= times; i++) {
			verify(updateOrderPreparedStatement).setString(1, orderNumberPrefix + i + "-dup-" + cartOrderGuidPrefix + i);
			verify(updateOrderPreparedStatement).setString(2, uidPkPrefix + i);
		}
	}

	private void setupMultipleDuplicateCartOrdersResponse(final long low, final long high, final int numberOfDuplicates,
			final String cartOrderGuidPrefix) throws SQLException,
			DatabaseException {
		when(connection.prepareStatement(String.format(SELECT_DUPLICATE_ORDERS_STATEMENT, low, high)))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);
		when(getDuplicateCartOrderGuidsResultSet.next()).thenAnswer(answerTrueNTimes(numberOfDuplicates));
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
		when(connection.prepareStatement("UPDATE TORDER SET CART_ORDER_GUID = ? WHERE (UIDPK = ?)")).thenReturn(updateOrderPreparedStatement);
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

	private void verifyGetDuplicateOrderIdsCallWithRange(final int invocations, final long low, final long high)
			throws DatabaseException, SQLException {
		verify(connection).prepareStatement(String.format(SELECT_DUPLICATE_ORDERS_STATEMENT, low, high));
		verify(getDuplicateCartOrderGuidsPreparedStatement, times(invocations)).executeQuery();
		verify(getDuplicateCartOrderGuidsPreparedStatement, times(invocations)).close();
	}

	private void verifyEmptyTableCheck() throws DatabaseException, SQLException {
		verify(connection).prepareStatement("SELECT COUNT(*) FROM TORDER");
		verify(preparedStatement).executeQuery();
		verify(preparedStatement).close();
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
		verify(connection).prepareStatement("UPDATE TORDER SET CART_ORDER_GUID = ? WHERE (UIDPK = ?)");
	}

	private void verifyHighAndLowQuery() throws SQLException, DatabaseException {
		verify(connection).prepareStatement("SELECT MIN(UIDPK) AS UIDPK FROM TORDER");
		verify(connection).prepareStatement("SELECT MAX(UIDPK) AS UIDPK FROM TORDER");

		verify(highestPreparedStatement).executeQuery();
		verify(lowestPreparedStatement).executeQuery();

		verify(lowestResultSet).next();
		verify(highestResultSet).next();

		verify(lowestResultSet).getLong("UIDPK");
		verify(highestResultSet).getLong("UIDPK");

		verify(lowestResultSet).close();
		verify(highestResultSet).close();

		verify(highestPreparedStatement).close();
		verify(lowestPreparedStatement).close();

		verifyNoMoreInteractions(lowestResultSet);
		verifyNoMoreInteractions(highestResultSet);
		verifyNoMoreInteractions(lowestPreparedStatement);
		verifyNoMoreInteractions(highestPreparedStatement);
	}

	private void setupSingleDuplicateOrdersResponse(final long low, final long high, final String cartOrderGuid) throws SQLException,
			DatabaseException {
		when(connection.prepareStatement(String.format(SELECT_DUPLICATE_ORDERS_STATEMENT, low, high)))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);
		when(getDuplicateCartOrderGuidsResultSet.next()).thenReturn(true).thenReturn(false);
		when(getDuplicateCartOrderGuidsResultSet.getString(1)).thenReturn(cartOrderGuid);
	}

	private void setupNoDuplicateOrdersResponse(final long low, final long high) throws SQLException, DatabaseException {
		when(connection.prepareStatement(String.format(SELECT_DUPLICATE_ORDERS_STATEMENT, low, high)))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);
		when(getDuplicateCartOrderGuidsResultSet.next()).thenReturn(false);
	}

	private void setupNoDuplicateOrdersResponseForAnyRange() throws SQLException, DatabaseException {
		when(connection.prepareStatement(
				contains("SELECT CART_ORDER_GUID FROM TORDER WHERE UIDPK BETWEEN")))
				.thenReturn(getDuplicateCartOrderGuidsPreparedStatement);
		when(getDuplicateCartOrderGuidsPreparedStatement.executeQuery()).thenReturn(getDuplicateCartOrderGuidsResultSet);
		when(getDuplicateCartOrderGuidsResultSet.next()).thenReturn(false);
	}

	private void setupLowestAndHighestResponse(final long lowest, final long highest) throws DatabaseException, SQLException {
		when(connection.prepareStatement("SELECT MIN(UIDPK) AS UIDPK FROM TORDER")).thenReturn(lowestPreparedStatement);
		when(connection.prepareStatement("SELECT MAX(UIDPK) AS UIDPK FROM TORDER")).thenReturn(highestPreparedStatement);
		when(lowestPreparedStatement.executeQuery()).thenReturn(lowestResultSet);
		when(highestPreparedStatement.executeQuery()).thenReturn(highestResultSet);

		when(lowestResultSet.next()).thenReturn(true);
		when(lowestResultSet.getLong("UIDPK")).thenReturn(lowest);
		when(highestResultSet.next()).thenReturn(true);
		when(highestResultSet.getLong("UIDPK")).thenReturn(highest);
	}

	private void setupEmptyTableResponse() throws SQLException, DatabaseException {
		setupTableResponse();
		when(resultSet.next()).thenReturn(false);
		when(resultSet.getInt(1)).thenReturn(0);
	}

	private void setupNonEmptyTableResponse() throws SQLException, DatabaseException {
		setupTableResponse();
		when(resultSet.next()).thenReturn(true);
		when(resultSet.getInt(1)).thenReturn(1);
	}

	private void setupTableResponse() throws DatabaseException {
		when(connection.prepareStatement("SELECT COUNT(*) FROM TORDER")).thenReturn(preparedStatement);
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