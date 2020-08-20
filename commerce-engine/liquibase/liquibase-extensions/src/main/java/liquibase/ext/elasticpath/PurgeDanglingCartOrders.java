/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import liquibase.change.custom.CustomSqlChange;
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
import liquibase.statement.SqlStatement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Purge dangling cart orders and related data (CO payment instruments and coupons).
 */
public class PurgeDanglingCartOrders implements CustomSqlChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final String SELECT_CART_ORDER_CART_GUIDS_QUERY = "SELECT UIDPK, SHOPPINGCART_GUID FROM TCARTORDER WHERE UIDPK > ?";
	private static final String SELECT_CART_GUIDS_QUERY = "SELECT GUID FROM TSHOPPINGCART WHERE GUID IN %s";
	private static final String DELETE_DANGLING_CART_ORDERS = "DELETE FROM TCARTORDER WHERE SHOPPINGCART_GUID IN %s";
	private static final String DELETE_DANGLING_CART_ORDER_PAYMENT_INSTRUMENTS = "DELETE FROM TCARTORDERPAYMENTINSTRUMENT WHERE CART_ORDER_UID IN %s";
	private static final String DELETE_DANGLING_CART_ORDER_COUPONS = "DELETE FROM TCARTORDERCOUPON WHERE CARTORDER_UID IN %s";
	private static final int MAX_IN_ELEMENTS = 998;

	private static final int DEFAULT_BATCH_SIZE = 1000;
	private int batchSize = DEFAULT_BATCH_SIZE;

	private PreparedStatement selectCartOrderCartGuidsStatement;
	private PreparedStatement selectCartGuidsStatement;
	private PreparedStatement deleteCartOrdersStatement;
	private PreparedStatement deleteCartOrderPaymentInstrumentsStatement;
	private PreparedStatement deleteCartOrderCouponsStatement;

	private long numOfDeletedCartOrders;

	public void setBatchSize(final String batchSize) {
		this.batchSize = Integer.parseInt(batchSize);
	}

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		long startTime = System.currentTimeMillis();

		LOG.info("Starting purging dangling cart orders in batches of " + batchSize);

		deleteCartOrdersInBatches((JdbcConnection) database.getConnection());

		LOG.info("Purging of dangling cart orders completed in " + (System.currentTimeMillis() - startTime) + " ms");
		LOG.info("Total number of deleted cart orders: " + numOfDeletedCartOrders);

		return new SqlStatement[0];
	}

	@SuppressWarnings("unchecked")
	private void deleteCartOrdersInBatches(final JdbcConnection connection) throws CustomChangeException {

		try {
			connection.setAutoCommit(false);

			selectCartOrderCartGuidsStatement = connection.prepareStatement(SELECT_CART_ORDER_CART_GUIDS_QUERY);
			selectCartOrderCartGuidsStatement.setMaxRows(batchSize);

			long lastCartUidPk = 0L;

			while (true) {
				Map<String, Long> cartOrderGuidToUidMap = new HashMap<>();

				selectCartOrderCartGuidsStatement.setLong(1, lastCartUidPk);

				try (ResultSet cartOrderCartGuidsResultSet = selectCartOrderCartGuidsStatement.executeQuery()) {
					while (cartOrderCartGuidsResultSet.next()) {
						long uidPk = cartOrderCartGuidsResultSet.getLong(1);
						if (uidPk > lastCartUidPk) {
							lastCartUidPk = uidPk;
						}
						cartOrderGuidToUidMap.put(cartOrderCartGuidsResultSet.getString(2), uidPk);
					}
				}

				if (cartOrderGuidToUidMap.isEmpty()) {
					break;
				}

				//handle Oracle limitation of 1000 IN elements
				List<List<String>> subListsOfGuids = Lists.partition(new ArrayList<>(cartOrderGuidToUidMap.keySet()), MAX_IN_ELEMENTS);

				for (List<String> subListOfGuids : subListsOfGuids) {
					String inParamValues = "('" + String.join("','", subListOfGuids) + "')";
					selectCartGuidsStatement = connection.prepareStatement(String.format(SELECT_CART_GUIDS_QUERY, inParamValues));

					List<String> existingCartGuids = new ArrayList<>();

					try (ResultSet existingCartGuidsResultSet = selectCartGuidsStatement.executeQuery()) {
						while (existingCartGuidsResultSet.next()) {
							existingCartGuids.add(existingCartGuidsResultSet.getString(1));
						}
					}

					//remove existing cart GUIDs from both, map and a sublist. If substractedList is not empty, it means we found dead COs
					cartOrderGuidToUidMap.keySet().removeAll(existingCartGuids);

					Collection<String> substractedList = CollectionUtils.subtract(subListOfGuids, existingCartGuids);

					if (!substractedList.isEmpty()) {
						inParamValues = "('" + StringUtils.join(getUidsByGuidsFromMap(cartOrderGuidToUidMap, substractedList), "','") + "')";
						deleteCartOrderCouponsStatement = connection.prepareStatement(String.format(DELETE_DANGLING_CART_ORDER_COUPONS, inParamValues));
						deleteCartOrderCouponsStatement.executeUpdate();

						deleteCartOrderPaymentInstrumentsStatement = connection
								.prepareStatement(String.format(DELETE_DANGLING_CART_ORDER_PAYMENT_INSTRUMENTS, inParamValues));
						deleteCartOrderPaymentInstrumentsStatement.executeUpdate();

						inParamValues = "('" + String.join("','", substractedList) + "')";
						deleteCartOrdersStatement = connection.prepareStatement(String.format(DELETE_DANGLING_CART_ORDERS, inParamValues));

						int numOfDeleted = deleteCartOrdersStatement.executeUpdate();

						if (numOfDeleted > 0) {
							numOfDeletedCartOrders += numOfDeleted;
							connection.commit();
						}
					}
				}

			}
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (DatabaseException dbexc) {
				LOG.severe("Can't rollback transaction", dbexc);
			}

			throw new CustomChangeException(e);
		} finally {
			closePreparedStatements();
		}
	}

	private List<Long> getUidsByGuidsFromMap(final Map<String, Long> cartOrderGuidToUidMap, final Collection<String> guids) {
		return cartOrderGuidToUidMap.entrySet().stream()
				.filter(entry -> guids.contains(entry.getKey()))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished purging dangling cart orders and associated entities";
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
		return null; // no validation needed
	}

	private void closePreparedStatement(final PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (Exception e) {
				//do nothing
			}
		}
	}

	private void closePreparedStatements() {
		closePreparedStatement(selectCartOrderCartGuidsStatement);
		closePreparedStatement(selectCartGuidsStatement);
		closePreparedStatement(deleteCartOrdersStatement);
		closePreparedStatement(deleteCartOrderPaymentInstrumentsStatement);
		closePreparedStatement(deleteCartOrderCouponsStatement);
	}
}
