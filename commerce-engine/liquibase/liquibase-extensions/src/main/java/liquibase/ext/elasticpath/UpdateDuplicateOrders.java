/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

/**
 * Updates the TORDER table to remove any duplicated Cart Order Guids as the Order may have been submitted more than once.
 * To remove the duplicated Cart Order Guid, the Order number is appended to the Guid in the form 'OrderNumber-dup-CartOrderGuid'
 * <p>
 * Example:
 * This:
 * TORDER
 * UIDPK		ORDER_NUMBER		CART_ORDER_GUID
 * 2			20002				12345
 * 3			20003				12345
 * <p>
 * Will become:
 * UIDPK		ORDER_NUMBER		CART_ORDER_GUID
 * 2			20002				20002-dup-12345
 * 3			20003				20003-dup-12345
 * <p>
 * It does this by first getting a list of all duplicated Cart Order Guids using:
 * SELECT CART_ORDER_GUID FROM TORDER WHERE UIDPK BETWEEN %s AND %s GROUP BY CART_ORDER_GUID HAVING COUNT(*) > 1
 * <p>
 * The associated UIDPKs are then gotten using:
 * SELECT UIDPK,ORDER_NUMBER,CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IN (%s);
 * <p>
 * The records are then batch updated using:
 * UPDATE TORDER SET CART_ORDER_GUID = ? WHERE (UIDPK = ?);
 */
public class UpdateDuplicateOrders implements CustomTaskChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final int BATCH_SIZE = 1000;

	private int updateCount;
	private int batchSize = BATCH_SIZE;
	private int batchOverlap = 1; //Overlap so orders are not missed on batch perimeters

	private static final String NUMBER_SEPARATOR = "-dup-";
	private static final String CONFIRMATION_MESSAGE = "Updated %s Orders to remove duplicate Cart Order Guids";

	private static final String SELECT_DUPLICATE_GUIDS_BETWEEN = "SELECT CART_ORDER_GUID FROM TORDER WHERE UIDPK BETWEEN %s AND %s GROUP BY "
			+ "CART_ORDER_GUID HAVING COUNT(*) > 1";
	private static final String SELECT_DUPLICATE_ORDERS = "SELECT UIDPK,ORDER_NUMBER,CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IN (%s)";
	private static final String UPDATE_CART_ORDER_GUID_FOR_ORDER = "UPDATE TORDER SET CART_ORDER_GUID = ? WHERE (UIDPK = ?)";

	private static final String TABLE_IS_NOT_EMPTY = "SELECT COUNT(*) FROM TORDER";
	private static final String SELECT_LOWEST_UIDPK = "SELECT MIN(UIDPK) AS UIDPK FROM TORDER";
	private static final String SELECT_HIGHEST_UIDPK = "SELECT MAX(UIDPK) AS UIDPK FROM TORDER";

	@Override
	public void execute(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();
		try {
			LOG.info("Beginning TORDER update to append Order Number to duplicate Cart Order Guids");
			if (thereAreExistingOrderEntries(connection)) {
				updateDuplicateCartOrderGuids(connection);
			} else {
				LOG.info("No TORDER records to update");
			}
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException("An error occurred when updating Order records", e);
		}
	}

	private boolean thereAreExistingOrderEntries(final JdbcConnection connection) throws SQLException, DatabaseException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(TABLE_IS_NOT_EMPTY)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1) > 0;
			}
		}
	}

	private void updateDuplicateCartOrderGuids(final JdbcConnection connection) throws DatabaseException, SQLException {
		final List<OrderEntry> ordersWithDuplicateCartOrders = getOrdersWithDuplicateCartOrders(connection);
		if (!ordersWithDuplicateCartOrders.isEmpty()) {
			updateOrdersByAppendingNumberToCartOrderGuid(ordersWithDuplicateCartOrders, connection);
		}
	}

	private List<OrderEntry> getOrdersWithDuplicateCartOrders(final JdbcConnection connection)
			throws DatabaseException, SQLException {
		final List<OrderEntry> ordersWithDuplicatedCartOrderGuids = new ArrayList<>();
		final List<String> duplicatedCartOrderGuids = getListOfDuplicatedCartOrderGuids(connection);
		duplicatedCartOrderGuids.replaceAll(string -> "'" + string + "'");

		if (!duplicatedCartOrderGuids.isEmpty()) {
			final String selectStatement = String.format(SELECT_DUPLICATE_ORDERS, String.join(",", duplicatedCartOrderGuids));

			try (PreparedStatement preparedStatement = connection.prepareStatement(selectStatement)) {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while (resultSet.next()) {
						ordersWithDuplicatedCartOrderGuids.add(createOrderEntry(resultSet));
					}
				}
			}
		}
		return ordersWithDuplicatedCartOrderGuids;
	}

	private OrderEntry createOrderEntry(final ResultSet resultSet) throws SQLException {
		final String uidPk = resultSet.getString(1);
		final String orderNumber = resultSet.getString(2);
		final String cartOrderGuid = resultSet.getString(3);
		return new OrderEntry(uidPk, orderNumber, cartOrderGuid);
	}

	private List<String> getListOfDuplicatedCartOrderGuids(final JdbcConnection connection) throws DatabaseException, SQLException {
		final List<String> duplicatedCartOrderGuids = new ArrayList<>();

		long lowestUidPk = getLowestUidPk(connection);
		final long highestUidPk = getHighestUidPk(connection);

		while (lowestUidPk < highestUidPk) {
			final List<String> batchedListOfDuplicatedGuids = getListOfDuplicatedCartOrderGuidsBetween(lowestUidPk,
					lowestUidPk + batchSize + batchOverlap, connection);
			duplicatedCartOrderGuids.addAll(batchedListOfDuplicatedGuids);
			lowestUidPk = lowestUidPk + batchSize;
		}

		return duplicatedCartOrderGuids;
	}

	private List<String> getListOfDuplicatedCartOrderGuidsBetween(final long low, final long high, final JdbcConnection connection)
			throws DatabaseException, SQLException {
		final List<String> duplicatedCartOrderGuids = new ArrayList<>();
		final String selectStatement = String.format(SELECT_DUPLICATE_GUIDS_BETWEEN, low, high);

		try (PreparedStatement preparedStatement = connection.prepareStatement(selectStatement)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					duplicatedCartOrderGuids.add(resultSet.getString(1));
				}
			}
		}
		return duplicatedCartOrderGuids;
	}

	private void updateOrdersByAppendingNumberToCartOrderGuid(final List<OrderEntry> orderEntries, final JdbcConnection connection)
			throws DatabaseException, SQLException {
		try (PreparedStatement statement = connection.prepareStatement(UPDATE_CART_ORDER_GUID_FOR_ORDER)) {
			for (int i = 0; i < orderEntries.size(); i++) {
				final OrderEntry orderEntry = orderEntries.get(i);
				if (Objects.isNull(orderEntry.getCartOrderGuid()) || orderEntry.getCartOrderGuid().contains(NUMBER_SEPARATOR)) {
					continue; //Has no guid or already had Order number appended to guid
				}
				statement.setString(1, orderEntry.getOrderNumber() + NUMBER_SEPARATOR + orderEntry.getCartOrderGuid());
				statement.setString(2, orderEntry.getUidPk());
				statement.addBatch();

				if (i % batchSize == 0 || i == orderEntries.size() - 1) { //Reached batch size or end of list
					final int[] statementRowCount = statement.executeBatch();
					statement.clearBatch();
					for (int rowCount : statementRowCount) {
						//For each update executed, count the number of affected rows
						updateCount = updateCount + rowCount;
					}
				}
			}
		}
	}

	private Long getLowestUidPk(final JdbcConnection connection) throws SQLException, DatabaseException {
		return runHighestLowestPreparedStatement(connection, SELECT_LOWEST_UIDPK);
	}

	private Long getHighestUidPk(final JdbcConnection connection) throws SQLException, DatabaseException {
		return runHighestLowestPreparedStatement(connection, SELECT_HIGHEST_UIDPK);
	}

	private long runHighestLowestPreparedStatement(final JdbcConnection connection, final String uidpk) throws SQLException, DatabaseException {
		try (PreparedStatement preparedStatement = connection.prepareStatement(uidpk)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				resultSet.next();
				return resultSet.getLong("UIDPK");
			}
		}
	}

	/**
	 * Private class for holding Order information.
	 */
	private static class OrderEntry {
		private final String uidPk;
		private final String orderNumber;
		private final String cartOrderGuid;

		OrderEntry(final String uidPk, final String orderNumber, final String cartOrderGuid) {
			this.uidPk = uidPk;
			this.orderNumber = orderNumber;
			this.cartOrderGuid = cartOrderGuid;
		}

		protected String getUidPk() {
			return uidPk;
		}

		protected String getOrderNumber() {
			return orderNumber;
		}

		protected String getCartOrderGuid() {
			return cartOrderGuid;
		}
	}

	@Override
	public String getConfirmationMessage() {
		return String.format(CONFIRMATION_MESSAGE, updateCount);
	}

	@Override
	public void setUp() throws SetupException {
		//None Required
	}

	@Override
	public void setFileOpener(final ResourceAccessor resourceAccessor) {
		//Not Used
	}

	@Override
	public ValidationErrors validate(final Database database) {
		return null;
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public void setBatchOverlap(final int overlap) {
		this.batchOverlap = overlap;
	}
}
