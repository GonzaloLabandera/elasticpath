/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.shoppingcart.ItemType;

/**
 * Custom liquibase change that updates newly added field CHILD_ITEM_CART_UID using parent SHOPPING_CART_UID.
 * It also sets a correct item type.
 *
 */
public class CartItemTableOptimizer implements CustomSqlChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	private static final String SELECT_ROOT_PARENT_ROWS =
		"SELECT UIDPK, SHOPPING_CART_UID FROM TCARTITEM WHERE PARENT_ITEM_UID IS NULL AND UIDPK > ? ORDER BY UIDPK";

	private static final String SELECT_CHILDREN_FOR_PARENT = "SELECT UIDPK, BUNDLE_CONSTITUENT FROM TCARTITEM WHERE PARENT_ITEM_UID = ?";
	private static final String VERIFY_NOT_UPDATED_PARENTS = "SELECT COUNT(*) FROM TCARTITEM WHERE PARENT_ITEM_UID IS NULL AND "
		+ "ITEM_TYPE IS NULL";
	private static final String UPDATE_CHILD_STATEMENT = "UPDATE TCARTITEM SET CHILD_ITEM_CART_UID = ?, ITEM_TYPE = ? WHERE UIDPK = ?";
	private static final String UPDATE_PARENT_STATEMENT = "UPDATE TCARTITEM SET ITEM_TYPE = ? WHERE UIDPK = ?";

	private static final int CHILD_ITEM_CART_UID_PARAM_IDX = 1;
	private static final int CHILD_ITEM_TYPE_PARAM_IDX = 2;
	private static final int CHILD_ITEM_UID_PARAM_IDX = 3;

	private static final int DEFAULT_MAX_ROOT_PARENT_ROWS = 1000;
	private int batchRootParentRows = DEFAULT_MAX_ROOT_PARENT_ROWS;

	private PreparedStatement countStatement;
	private PreparedStatement parentStatement;
	private PreparedStatement updateChildStatement;
	private PreparedStatement updateRootStatement;

	private final MutableLong numOfProcessedChildrenRecords = new MutableLong();

	public void setBatchRootParentRows(final String batchRootParentRows) {
		this.batchRootParentRows = Integer.parseInt(batchRootParentRows);
	}

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		long startTime = System.currentTimeMillis();

		LOG.info("Starting data migration of parent shopping cart UIDs....");

		updateInBatches((JdbcConnection) database.getConnection());

		LOG.info("Data migration is done in " + (System.currentTimeMillis() - startTime) + " ms");

		return new SqlStatement[0];
	}

	private void updateInBatches(final JdbcConnection connection) throws CustomChangeException {
		MutableInt batchCount = new MutableInt();
		MutableLong startParentUidPk = new MutableLong();

		createPreparedStatements(connection);

		try {
			while (true) {
				try (ResultSet countResultSet = countStatement.executeQuery()) {

					if (!hasMoreToProcess(countResultSet, batchCount)) {
						break;
					}
				}

				LOG.info("Processing next batch: " + (batchCount.intValue() + 1));

				processParents(startParentUidPk, connection);

				batchCount.increment();
			}

			connection.commit();

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

	private boolean hasMoreToProcess(final ResultSet countResultSet, final MutableInt batchCount)
		throws Exception {

		int nonUpdatedParentsCount = 0;

		while (countResultSet.next()) {
			nonUpdatedParentsCount = countResultSet.getInt(1);
		}

		if (nonUpdatedParentsCount == 0) {
			LOG.info("No more batches for processing. Exiting. Processed total of " + batchCount.intValue() + " batches and "
				+ numOfProcessedChildrenRecords.longValue() + " children records");
			return false;
		}

		return true;
	}

	private void processParents(final MutableLong startParentUidPk, final JdbcConnection connection) throws Exception {
		parentStatement.setMaxRows(batchRootParentRows);
		parentStatement.setLong(1, startParentUidPk.longValue());

		List<Pair<Long, Long>> parentPairs = new ArrayList<>();

		try (ResultSet resultSet = parentStatement.executeQuery()) {
			while (resultSet.next()) {
				Long parentUidPk = resultSet.getLong("UIDPK");
				Long parentShoppingCartUidPk = resultSet.getLong("SHOPPING_CART_UID");

				parentPairs.add(Pair.of(parentUidPk, parentShoppingCartUidPk));
			}
		}

		for (Pair<Long, Long> parentIdPair : parentPairs) {
			Long parentUidPk = parentIdPair.getFirst();
			Long parentShoppingCartUidPk = parentIdPair.getSecond();

			ItemType itemType = updateChildren(connection, parentUidPk, parentShoppingCartUidPk);

			if (itemType != ItemType.SIMPLE) {
				//update parent's children with shopping cart Uid
				updateChildStatement.executeBatch();
			}

			startParentUidPk.setValue(parentUidPk);
			//update parent with root type
			updateRootStatement.setLong(1, itemType.getOrdinal());
			updateRootStatement.setLong(2, parentUidPk);
			updateRootStatement.addBatch();
		}

		updateRootStatement.executeBatch();
	}

	private ItemType updateChildren(final JdbcConnection connection, final Long parentUidPk, final Long shoppingCartUid)
		throws Exception {

		ItemType rootitemType = ItemType.SIMPLE;

		PreparedStatement childrenStatement = connection.prepareStatement(SELECT_CHILDREN_FOR_PARENT);
		childrenStatement.setLong(1, parentUidPk);

		List<Pair<Long, Boolean>> childrenPairs = new ArrayList<>();

		try (ResultSet childrenResultSet = childrenStatement.executeQuery()) {
			while (childrenResultSet.next()) {
				Long childUidPk = childrenResultSet.getLong("UIDPK");
				boolean isBundleConstituent = childrenResultSet.getBoolean("BUNDLE_CONSTITUENT");

				childrenPairs.add(Pair.of(childUidPk, isBundleConstituent));
			}
		}

		closePreparedStatement(childrenStatement);

		for (Pair<Long, Boolean> childrenPair : childrenPairs) {
			Long childUidPk = childrenPair.getFirst();
			boolean isBundleConstituent = childrenPair.getSecond();

			updateChildStatement.setLong(CHILD_ITEM_CART_UID_PARAM_IDX, shoppingCartUid);

			if (isBundleConstituent) {
				rootitemType = ItemType.BUNDLE;
				updateChildStatement.setInt(CHILD_ITEM_TYPE_PARAM_IDX, ItemType.BUNDLE_CONSTITUENT_ORDINAL);
			} else {
				rootitemType = ItemType.SKU_WITH_DEPENDENTS;
				updateChildStatement.setInt(CHILD_ITEM_TYPE_PARAM_IDX, ItemType.DEPENDENT_ORDINAL);
			}

			updateChildStatement.setLong(CHILD_ITEM_UID_PARAM_IDX, childUidPk);

			updateChildStatement.addBatch();
			numOfProcessedChildrenRecords.increment();

			updateChildren(connection, childUidPk, shoppingCartUid);
		}

		return rootitemType;
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished updating table TCARTITEM";
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

	private void createPreparedStatements(final JdbcConnection connection) throws CustomChangeException {
		try {
			connection.setAutoCommit(false);
			countStatement = connection.prepareStatement(VERIFY_NOT_UPDATED_PARENTS);
			parentStatement = connection.prepareStatement(SELECT_ROOT_PARENT_ROWS);

			updateChildStatement = connection.prepareStatement(UPDATE_CHILD_STATEMENT);
			updateRootStatement = connection.prepareStatement(UPDATE_PARENT_STATEMENT);

		} catch (Exception e) {
			closePreparedStatements();

			throw new CustomChangeException(e);
		}
	}

	private void closePreparedStatements() {
		closePreparedStatement(countStatement);
		closePreparedStatement(parentStatement);
		closePreparedStatement(updateChildStatement);
		closePreparedStatement(updateRootStatement);
	}
}
