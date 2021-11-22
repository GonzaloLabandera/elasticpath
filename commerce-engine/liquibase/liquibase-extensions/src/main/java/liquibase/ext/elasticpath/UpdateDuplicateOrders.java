/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
 * UIDPK      ORDER_NUMBER      CART_ORDER_GUID
 * 2         20002           12345
 * 3         20003           12345
 * <p>
 * Will become:
 * UIDPK      ORDER_NUMBER      CART_ORDER_GUID
 * 2         20002           20002-dup-12345
 * 3         20003           20003-dup-12345
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

	private static final String NUMBER_SEPARATOR = "-dup-";
	private static final String CONFIRMATION_MESSAGE = "Updated %s Orders to remove duplicate Cart Order Guids";

	private static final String SELECT_DUPLICATE_ORDER_GUIDS =
			"SELECT CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IS NOT NULL GROUP BY CART_ORDER_GUID HAVING COUNT(*) > 1";

	private static final String SELECT_DUPLICATE_ORDERS = "SELECT UIDPK,ORDER_NUMBER,CART_ORDER_GUID FROM TORDER WHERE CART_ORDER_GUID IN (%s)";

	private static final String UPDATE_CART_ORDER_GUID_FOR_ORDER = "UPDATE TORDER SET CART_ORDER_GUID = ? WHERE UIDPK = ?";

	@Override
	public void execute(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		final JdbcConnection connection = (JdbcConnection) database.getConnection();
		try {
			LOG.info("Beginning TORDER update to append Order Number to duplicate Cart Order Guids");
			updateDuplicateCartOrderGuids(connection);
		} catch (DatabaseException | SQLException e) {
			throw new CustomChangeException("An error occurred when updating Order records", e);
		}
	}


	private void updateDuplicateCartOrderGuids(final JdbcConnection connection) throws DatabaseException, SQLException {
		List<String> duplicatedCartOrderGuids = getNextBatchOfDuplicatedCartOrderGuids(connection);
		while (!duplicatedCartOrderGuids.isEmpty()) {
			final List<OrderEntry> ordersWithDuplicateCartOrders = getOrdersForGivenOrderGuids(duplicatedCartOrderGuids, connection);
			updateOrdersByAppendingNumberToCartOrderGuid(ordersWithDuplicateCartOrders, connection);
			duplicatedCartOrderGuids = getNextBatchOfDuplicatedCartOrderGuids(connection);
		}
	}

	private List<OrderEntry> getOrdersForGivenOrderGuids(List<String> duplicatedCartOrderGuids, final JdbcConnection connection)
			throws DatabaseException, SQLException {

		List<OrderEntry> ordersWithDuplicatedCartOrderGuids = new ArrayList<>();
		if (!duplicatedCartOrderGuids.isEmpty()) {
			final String selectStatement = String.format(SELECT_DUPLICATE_ORDERS,
					duplicatedCartOrderGuids.stream().collect(Collectors.joining("','", "'", "'")));

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

	private List<String> getNextBatchOfDuplicatedCartOrderGuids(final JdbcConnection connection)
			throws DatabaseException, SQLException {
		final List<String> duplicatedCartOrderGuids = new ArrayList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_DUPLICATE_ORDER_GUIDS)) {
			preparedStatement.setMaxRows(batchSize);
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
			for (OrderEntry orderEntry : orderEntries) {
				statement.setString(1, orderEntry.getOrderNumber() + NUMBER_SEPARATOR + orderEntry.getCartOrderGuid());
				statement.setString(2, orderEntry.getUidPk());
				statement.addBatch();
			}

			final int[] statementRowCount = statement.executeBatch();
			statement.clearBatch();

			// Calculate the count of executed rows.
			updateCount = updateCount + Arrays.stream(statementRowCount).sum();
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
}
