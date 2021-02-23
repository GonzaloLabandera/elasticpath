/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package liquibase.ext.elasticpath;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
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
import org.apache.commons.lang3.StringUtils;

/**
 * The main purpose of this Liquibase extension is to set order ID to existing order SKUs.
 * There are 2 reasons for doing that:
 * <p>
 * 1. Allow proper operation of the cascade deletion when an order (e.g. failed - the task of PERF-333) is deleted because order bundle SKUs
 * do not have ORDER_SHIPMENT_UID, thus cascade deletion doesn't work for the FK referencing this field
 * <p>
 * 2. The ORDER_ID "flattens" the tree of order SKU bundles and will allow easier and faster retrieval of large orders
 * <p>
 * This extension handles different SKU types on the following way:
 * <p>
 * 1. Simple and bundle (i.e. only root) SKUs are retrieved in a batch of DEFAULT_BATCH_SIZE (1000) records
 * 2. If current SKU is simple, the update statement is batched;
 * If current SKU is a bundle root, then all bundle constituents are found, along with ORDER_ID (devised from any simple constituent) and a
 * single update is created and executed.
 * 3. On the end of the batch, all updates are committed.
 */
public class AddAndPopulateOrderUidOnOrderSku implements CustomSqlChange {

	private static final Logger LOG = LogFactory.getInstance().getLog();

	/* This query will be reused for both "plain" (non-bundle, non-bundle-constituent) and bundle constituent SKUs
	   to determine order id.
	 */
	private static final String SELECT_ORDER_SKUS_BASE = "SELECT osku.uidpk, oship.ORDER_UID FROM TORDERSKU osku "
			+ "LEFT OUTER JOIN TORDERSHIPMENT oship ON osku.ORDER_SHIPMENT_UID = oship.UIDPK ";

	// Select bundle constituents - looking for one with non-null ORDER_UID
	private PreparedStatement selectBundleConstituentOrderSkusStatement;
	private static final String SELECT_BUNDLE_CONSTITUENT_ORDER_SKUS = SELECT_ORDER_SKUS_BASE
			+ "WHERE osku.uidpk IN (SELECT CHILD_UID FROM TORDERSKUPARENT WHERE PARENT_UID = ?)";

	// Select both "plain" and bundle parent order SKUs
	private PreparedStatement selectOrderSkusStatement;
	private static final String SELECT_ORDER_SKUS = SELECT_ORDER_SKUS_BASE
			+ "WHERE osku.ORDER_UID IS NULL AND osku.BUNDLE_CONSTITUENT = 0";

	// Get the total number of order SKUs
	private PreparedStatement selectCountOfOrderSkusStatement;
	private static final String SELECT_COUNT_OF_ORDER_SKUS = "SELECT count(*) FROM TORDERSKU WHERE ORDER_UID IS NULL";

	// Update "plain" order SKU with order ID
	private PreparedStatement updatePlainOrderSkuStatement;
	private static final String UPDATE_ORDER_SKU = "UPDATE TORDERSKU SET ORDER_UID = ? WHERE UIDPK = ?";

	// Update bundle and its constituents with order ID
	private PreparedStatement updateBundleAndConstituentOrderSkusStatement;
	private static final String UPDATE_BUNDLE_AND_CONSTITUENT_ORDER_SKUS = "UPDATE TORDERSKU SET ORDER_UID = ? WHERE UIDPK IN (%s)";

	/* Because of ORACLE, the batch size limit is 1000 and there is no point setting a greater value because we'd need to split it in
	chunks of 1000 anyway.
	 */
	private static final int DEFAULT_BATCH_SIZE = 1000;

	private JdbcConnection connection;

	private long totalProcessedRecords;
	private Long bundleConstituentOrderId;

	//the list with SkuOrder DTOs
	private Set<SkuOrderDTO> bundleAndConstituentDTOs = new HashSet<>();

	//this list tracks processed bundle-constituent parents to avoid needless processing
	private Set<SkuOrderDTO> processedParentDTOs = Sets.newConcurrentHashSet();

	private boolean isPlainOrderProcessed;

	@Override
	public SqlStatement[] generateStatements(final Database database) throws CustomChangeException {
		if (!(database.getConnection() instanceof JdbcConnection)) {
			throw new UnexpectedLiquibaseException("Unable to get connection from database");
		}

		connection = (JdbcConnection) database.getConnection();

		long startTime = System.currentTimeMillis();

		LOG.info("Updating order SKUs with order ID ....");

		long totalOrderSkus;
		try {
			createPreparedStatements();

			totalOrderSkus = getTotalOrderSkus();

			updateOrderSkusInBatches();
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

		LOG.info("Updating order SKUs with order ID completed in " + (System.currentTimeMillis() - startTime) + " ms");
		LOG.info("Total number of processed records: [" + totalProcessedRecords + "] out of total order SKUs [" + totalOrderSkus + "]");

		return new SqlStatement[0];
	}

	private long getTotalOrderSkus() throws SQLException {
		try (ResultSet resultSet = selectCountOfOrderSkusStatement.executeQuery()) {
			if (resultSet.next()) {
				return resultSet.getLong(1);
			}
		}

		return 0L;
	}

	private void updateOrderSkusInBatches() throws DatabaseException, SQLException {
		boolean isExistMore;
		do {
			isExistMore = false;
			//get a batch of order SKUs
			try (ResultSet resultSet = selectOrderSkusStatement.executeQuery()) {
				isPlainOrderProcessed = false;

				while (resultSet.next()) {
					isExistMore = true;
					final SkuOrderDTO skuOrderDTO = convertRowToDTO(resultSet);

					if (Objects.isNull(skuOrderDTO.getOrderID())) { //update bundles
						updateBundleAndConstituentOrderSKUs(skuOrderDTO.getOrderSkuID());
					} else { //update plain SKUs
						updatePlainOrderSKU(skuOrderDTO);
					}
				}

				executeBatches();
				connection.commit();
			}

		} while (isExistMore);
	}

	private void executeBatches() throws SQLException {
		if (isPlainOrderProcessed) {
			final int[] updatedRecords = updatePlainOrderSkuStatement.executeBatch();
			totalProcessedRecords += updatedRecords.length;
		}
	}

	//plain SKUs are updated with a simple update
	private void updatePlainOrderSKU(final SkuOrderDTO skuOrderDTO) throws SQLException {
		isPlainOrderProcessed = true;
		updatePlainOrderSkuStatement.setObject(1, skuOrderDTO.getOrderID());
		updatePlainOrderSkuStatement.setObject(2, skuOrderDTO.getOrderSkuID());
		updatePlainOrderSkuStatement.addBatch();
	}

	//bundle SKUs are updated via recursion to ensure that all constituents have ORDER_UID set
	//this is an entry metho
	private void updateBundleAndConstituentOrderSKUs(final long bundleParentOrderSkuId) throws DatabaseException, SQLException {
		bundleAndConstituentDTOs.clear();
		processedParentDTOs.clear();

		//adding bundle root
		bundleAndConstituentDTOs.add(new SkuOrderDTO(bundleParentOrderSkuId, null));

		//recursively find all bundle constituents - the list will contain bundle root and all constituents
		findAllBundleConstituents(bundleAndConstituentDTOs);

		//create a single UPDATE statement per bundle root because the same ORDER_UID will be set to all found SKUs
		// UPDATE TORDERSKU SET ORDER_UID=bundleConstituentOrderId WHERE UIDPK IN (..........)
		String formattedUpdateStatement = String.format(UPDATE_BUNDLE_AND_CONSTITUENT_ORDER_SKUS, getSKUIdsAsString(bundleAndConstituentDTOs));

		updateBundleAndConstituentOrderSkusStatement = connection.prepareStatement(formattedUpdateStatement);
		updateBundleAndConstituentOrderSkusStatement.setLong(1, bundleConstituentOrderId);

		//and execute the statement
		totalProcessedRecords += updateBundleAndConstituentOrderSkusStatement.executeUpdate();

		closePreparedStatement(updateBundleAndConstituentOrderSkusStatement);
	}

	//recursive function
	private void findAllBundleConstituents(final Set<SkuOrderDTO> curentBundleConstituentDTOs) {
		Set<SkuOrderDTO> internalBundleConstituentDTOs = new HashSet<>();

		//traverse recursively the TORDERSKUPARENT table and store all constituents in the set
		curentBundleConstituentDTOs.forEach(dto -> {
			if (!processedParentDTOs.contains(dto)) { //do not process already processed parents
				processedParentDTOs.add(dto);

				try {
					selectBundleConstituentOrderSkusStatement.setLong(1, dto.getOrderSkuID());

					boolean hasMoreToProcess = false;

					try (ResultSet resultSet = selectBundleConstituentOrderSkusStatement.executeQuery()) {
						while (resultSet.next()) {
							hasMoreToProcess = true;
							SkuOrderDTO skuOrderDTO = convertRowToDTO(resultSet);
							bundleConstituentOrderId = skuOrderDTO.getOrderID();
							internalBundleConstituentDTOs.add(skuOrderDTO);
						}
					}

					if (hasMoreToProcess) {
						bundleAndConstituentDTOs.addAll(internalBundleConstituentDTOs);
						//recursion
						findAllBundleConstituents(internalBundleConstituentDTOs);
					}

				} catch (SQLException sqlException) {
					throw new RuntimeException(sqlException);
				}
			}
		});
	}

	private String getSKUIdsAsString(final Set<SkuOrderDTO> bundleConstituentDTOs) {
		return StringUtils.join(bundleConstituentDTOs, ',');
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished updating TORDERSKU table";
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

	private void createPreparedStatements() throws DatabaseException, SQLException {
		connection.setAutoCommit(false);

		updatePlainOrderSkuStatement = connection.prepareStatement(UPDATE_ORDER_SKU);

		selectOrderSkusStatement = connection.prepareStatement(SELECT_ORDER_SKUS);
		selectOrderSkusStatement.setMaxRows(DEFAULT_BATCH_SIZE);

		selectBundleConstituentOrderSkusStatement = connection.prepareStatement(SELECT_BUNDLE_CONSTITUENT_ORDER_SKUS);
		selectCountOfOrderSkusStatement = connection.prepareStatement(SELECT_COUNT_OF_ORDER_SKUS);
	}

	private void closePreparedStatements() {
		closePreparedStatement(selectOrderSkusStatement);
		closePreparedStatement(selectBundleConstituentOrderSkusStatement);
		closePreparedStatement(selectCountOfOrderSkusStatement);
		closePreparedStatement(updateBundleAndConstituentOrderSkusStatement);
		closePreparedStatement(updatePlainOrderSkuStatement);
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

	private SkuOrderDTO convertRowToDTO(final ResultSet resultSet) throws SQLException {
		return new SkuOrderDTO(convertToLongObject(resultSet, 1), convertToLongObject(resultSet, 2));
	}

	private Long convertToLongObject(final ResultSet resultSet, final int index) throws SQLException {
		return Objects.nonNull(resultSet.getObject(index))
				? resultSet.getLong(index)
				: null;
	}

	private class SkuOrderDTO {
		private final Long orderSkuID;
		private final Long orderID;

		SkuOrderDTO(final Long orderSkuID, final Long orderID) {
			this.orderSkuID = orderSkuID;
			this.orderID = orderID;
		}

		public Long getOrderSkuID() {
			return orderSkuID;
		}

		public Long getOrderID() {
			return orderID;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}

			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final SkuOrderDTO otherSkuOrderDTO = (SkuOrderDTO) o;
			return orderSkuID.equals(otherSkuOrderDTO.orderSkuID);
		}

		@Override
		public int hashCode() {
			return Objects.hash(orderSkuID);
		}

		@Override
		public String toString() {
			return String.valueOf(orderSkuID);
		}
	}
}
