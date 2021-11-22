/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.orders;

import static com.elasticpath.domain.order.OrderStatus.COMPLETED;
import static com.elasticpath.domain.order.OrderStatus.FAILED;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.batch.jobs.util.BundlePersister;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.TestPersistenceEngine;
import com.elasticpath.persistence.impl.DatabaseTimestampsEntityListener;
import com.elasticpath.settings.provider.TestSettingValueProvider;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

@SuppressWarnings({"rawtypes", "unchecked"})
@DirtiesDatabase
public class PurgeExpiredFailedOrdersJobTest extends DbTestCase {
	//The integer value represents a multiplier, used for calculation of the number of expected records per customer
	private static final TableDTO[] TABLES_BY_ORDER_NUMBER = new TableDTO[]{
			new TableDTO("TORDERPAYMENT"),
			new TableDTO("TORDERPAYMENTINSTRUMENT")};

	private static final TableDTO[] TABLES_BY_ORDER_UID = new TableDTO[]{
			new TableDTO("TORDERAUDIT"),
			new TableDTO("TAPPLIEDRULE"),
			new TableDTO("TORDERSKU", "", 3),
			new TableDTO("TORDERLOCK"),
			new TableDTO("TORDERRETURN"),
			new TableDTO("TORDERSHIPMENT"),
	};

	private static final TableDTO[] TABLES_BY_ORDER_PAYMENT_UID = new TableDTO[]{
			new TableDTO("TORDERPAYMENTDATA")};

	private static final TableDTO[] TABLES_BY_APPLIED_RULE_UID = new TableDTO[]{
			new TableDTO("TAPPLIEDRULECOUPONCODE")};

	private static final TableDTO[] TABLES_BY_ORDER_SHIPMENT_UID = new TableDTO[]{
			new TableDTO("TSHIPMENTTAX")};

	private static final TableDTO[] TABLES_BY_ORDER_RETURN_UID = new TableDTO[]{
			new TableDTO("TORDERRETURNSKU")
	};

	private static final TableDTO[] TABLES_BY_ORDER_SKU_UID = new TableDTO[]{
			new TableDTO("TORDERSKUPARENT", "CHILD_UID", 2),
			new TableDTO("TORDERSKUPARENT", "PARENT_UID", 2),
			new TableDTO("TSHOPPINGITEMRECURRINGPRICE", "ORDERSKU_UID")};

	private static final long USER_UIDPK = 200000L; //could be customer or CM user uid
	private static final int MAX_HISTORY = -61;
	private static final int THREE = 3;
	private static final long FOUR = 4L;
	private static final long ONE_K = 1000L;
	private static final long TWO_KS = 2000L;

	@Autowired
	private TestPersistenceEngine testPersistenceEngine;
	@Autowired
	private EntityManagerFactory batchEntityManagerFactory;
	@Autowired
	private DatabaseTimestampsEntityListener databaseTimestampsEntityListener;
	@Autowired
	private PurgeExpiredFailedOrdersJob purgeExpiredFailedOrdersJob;

	private BundlePersister bundlePersister;
	private String storeCode;
	private ProductSku productSku1;
	private ProductSku productSku2;
	private ProductSku productSku3;

	@Override
	public void setUpDb() {
		SimpleStoreScenario testScenario = getTac().useScenario(SimpleStoreScenario.class);
		Store store = testScenario.getStore();
		storeCode = store.getCode();

		bundlePersister = new BundlePersister(persisterFactory, testScenario);
		productSku1 = bundlePersister.persistSimpleProduct("testProductSku1");
		productSku2 = bundlePersister.persistSimpleProduct("testProductSku2");
		productSku3 = bundlePersister.persistSimpleProduct("testProductSku3");

		//create a customer
		String customerInsertQuery = format("INSERT INTO TCUSTOMER "
						+ "(UIDPK, GUID, SHARED_ID, CREATION_DATE, STATUS, CUSTOMER_TYPE) "
						+ "VALUES (%d, '%s', '%s', '2020-07-24 13:38:11', 1, 'REGISTERED_USER')",
				USER_UIDPK, UUID.randomUUID(), UUID.randomUUID());

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(customerInsertQuery));

		purgeExpiredFailedOrdersJob.setConfigBatchSize(new TestSettingValueProvider(1));
	}

	/*
		The test covers all possible cases:

		1. order expired & FAILED - valid for purging
		2. order expired & NON-FAILED - invalid for purging
		3. order non-expired & ANY status - invalid for purging

		The exchange orders are not of interest because they can't be created for failed orders.
	 */
	@Test
	public void shouldPurgeOnlyExpiredAnonymousCustomersWithoutOrders() {
		Order purgeableOrder1 = createOrder(1L, true, FAILED);
		//testing first part of SELECT condition - EXPIRED orders only
		Order nonPurgeableOrder1 = createOrder(THREE, false, COMPLETED);
		//testing second part of SELECT condition - FAILED orders only
		List<Order> nonPurgeableOrders = new ArrayList<>();

		MutableLong uidPkCount = new MutableLong(FOUR);

		OrderStatus.values().forEach(status -> {
					if (status != FAILED) {
						nonPurgeableOrders.add(createOrder(uidPkCount.longValue(), true, status));
						uidPkCount.increment();
					}
				});

		Order purgeableOrder2 = createOrder(2L, true, FAILED);

		preJobAssertions(THREE + nonPurgeableOrders.size());

		removeEntityLifecycleListener();

		purgeExpiredFailedOrdersJob.execute();

		// assert that only expired failed orders and relevant entities are purged
		postJobAssertions(0, purgeableOrder1, purgeableOrder2);

		nonPurgeableOrders.add(nonPurgeableOrder1);
		// assert that non-expired/non-failed orders and relevant entities are NOT purged
		postJobAssertions(1, nonPurgeableOrders.toArray(new Order[]{}));
	}

	@SuppressWarnings("checkstyle:methodlength")
	private Order createOrder(final long uidPk, final boolean isExpired, final OrderStatus orderStatus) {
		final Date now = new Date();
		final String orderNumber = UUID.randomUUID().toString();
		final OrderImpl order = new OrderImpl();
		order.setUidPk(uidPk);
		order.setOrderNumber(orderNumber);

		//create an order
		String orderInsertQueryToFormat = "INSERT INTO TORDER "
				+ "(UIDPK, CREATED_DATE, ORDER_NUMBER, CUSTOMER_UID, STORECODE, LOCALE, STATUS) "
				+ "VALUES (%d,'%tF %tT','%s',%d,'%s','en','%s')";

		String orderInsertQuery;

		if (isExpired) {
			Date expiredCreatedDate = (DateUtils.addDays(now, MAX_HISTORY));
			orderInsertQuery = format(orderInsertQueryToFormat, uidPk, expiredCreatedDate, expiredCreatedDate, orderNumber, USER_UIDPK, storeCode,
					orderStatus.getName());
		} else {
			orderInsertQuery = format(orderInsertQueryToFormat, uidPk, now, now, orderNumber, USER_UIDPK, storeCode, orderStatus.getName());
		}

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderInsertQuery));

		//populate other tables

		//order payment instrument
		String paymentInstrumentGuid = UUID.randomUUID().toString();

		String orderPaymentInstrumentInsertQuery = format("INSERT INTO TORDERPAYMENTINSTRUMENT "
						+ "(UIDPK, GUID, PAYMENT_INSTRUMENT_GUID, ORDER_NUMBER) "
						+ "VALUES (%d, '%s', '%s', '%s')",
				uidPk, UUID.randomUUID(), paymentInstrumentGuid, orderNumber);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderPaymentInstrumentInsertQuery));

		//order payment
		String orderPaymentInsertQuery = format("INSERT INTO TORDERPAYMENT "
						+ "(UIDPK, GUID, TYPE, STATUS, AMOUNT, CURRENCY_CODE, PAYMENT_INSTRUMENT_GUID, ORDER_NUMBER) "
						+ "VALUES (%d, '%s', 'CREDIT', 'APPROVED', 1.20, 'CAD', '%s', '%s')",
				uidPk, UUID.randomUUID(), paymentInstrumentGuid, orderNumber);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderPaymentInsertQuery));

		//order audit
		String orderAuditInsertQuery = format("INSERT INTO TORDERAUDIT "
						+ "(UIDPK, CREATED_DATE, ORIGINATOR_TYPE, TITLE, ORDER_UID) "
						+ "VALUES (%d, '2020-01-24 13:38:11', 'CUSTOMER', 'Order Placed', %d)",
				uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderAuditInsertQuery));

		//applied rule
		String appliedRuleInsertQuery = format("INSERT INTO TAPPLIEDRULE "
						+ "(UIDPK, ORDER_UID, RULE_UID, RULE_NAME, RULE_CODE) "
						+ "VALUES (%d, %d, 1, 'AC/DC rules', 'AC/DC')",
				uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(appliedRuleInsertQuery));

		//CM USER
		String cmUserInsertQuery = format("INSERT INTO TCMUSER "
						+ "(UIDPK, GUID, USER_NAME, EMAIL, PASSWORD, CREATION_DATE) "
						+ "VALUES (%d, '%s', '%s', '%s', 'password', '2020-01-24 13:38:11')",
				uidPk, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(cmUserInsertQuery));

		//order lock - locks are created/released via CM whenever an order is modified (e.g. change shipping option) /saved
		String orderLockInsertQuery = format("INSERT INTO TORDERLOCK "
						+ "(UIDPK, ORDER_UID, USER_UID, CREATED_DATE)"
						+ "VALUES (%d, %d, %d, %d)",
				uidPk, uidPk, uidPk, now.getTime());

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderLockInsertQuery));

		//order return
		String orderReturnInsertQuery = format("INSERT INTO TORDERRETURN "
						+ "(UIDPK, ORDER_UID, CREATED_DATE) "
						+ "VALUES (%d, %d, '2020-01-24 13:38:11')",
				uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderReturnInsertQuery));

		//order shipment
		String orderShipmentInsertQuery = format("INSERT INTO TORDERSHIPMENT "
						+ "(UIDPK, CREATED_DATE, TYPE, SHIPMENT_NUMBER, ORDER_UID) "
						+ "VALUES (%d, '2020-01-24 13:38:11', 'PHYSICAL', '%s', %d)",
				uidPk, UUID.randomUUID(), uidPk);
		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderShipmentInsertQuery));

		//order payment data
		String orderPaymentDataInsertQuery = format("INSERT INTO TORDERPAYMENTDATA "
						+ "(UIDPK, ORDER_PAYMENT_UID, DATA_KEY, DATA_VALUE) "
						+ "VALUES (%d, %d, 'data key','data value')",
				uidPk, uidPk);
		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderPaymentDataInsertQuery));

		//applied rule coupon code
		String appliedRuleCouponCodeInsertQuery = format("INSERT INTO TAPPLIEDRULECOUPONCODE "
						+ "(UIDPK, APPLIED_RULE_UID, COUPONCODE) "
						+ "VALUES (%d, %d, 'coupon code')",
				uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(appliedRuleCouponCodeInsertQuery));

		//create multi-level order SKU tree - SKU1 (bundle) -> SKU2 (bundle, as constituent) -> SKU3 (simple SKU)
		//order sku 1
		long sku1UidPk = uidPk;
		String orderSku1InsertQuery = format("INSERT INTO TORDERSKU "
						+ "(UIDPK, GUID, CREATED_DATE, SKUCODE, TAXCODE, DISPLAY_NAME, SKU_GUID, BUNDLE_CONSTITUENT, ORDER_UID) "
						+ "VALUES (%d, '%s', '2020-01-24 13:38:11', 'SKU code', 'TAX code', 'Display name', '%s', 0, %d)",
				sku1UidPk, UUID.randomUUID(), productSku1.getGuid(), uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderSku1InsertQuery));

		//order sku 2
		long sku2UidPk = uidPk + ONE_K;
		String orderSku2InsertQuery = format("INSERT INTO TORDERSKU "
						+ "(UIDPK, GUID, CREATED_DATE, SKUCODE, TAXCODE, DISPLAY_NAME, SKU_GUID, BUNDLE_CONSTITUENT, ORDER_UID) "
						+ "VALUES (%d, '%s', '2020-01-24 13:38:11', 'SKU code', 'TAX code', 'Display name', '%s', 1, %d)",
				sku2UidPk, UUID.randomUUID(), productSku2.getGuid(), uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderSku2InsertQuery));

		//order sku 3
		long sku3UidPk = uidPk + TWO_KS;
		String orderSku3InsertQuery = format("INSERT INTO TORDERSKU "
						+ "(UIDPK, GUID, CREATED_DATE, SKUCODE, TAXCODE, DISPLAY_NAME, SKU_GUID, ORDER_SHIPMENT_UID, BUNDLE_CONSTITUENT, ORDER_UID) "
						+ "VALUES (%d, '%s', '2020-01-24 13:38:11', 'SKU code', 'TAX code', 'Display name', '%s', %d, 1, %d)",
				sku3UidPk, UUID.randomUUID(), productSku3.getGuid(), uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderSku3InsertQuery));

		//shopping item recurring price
		String shopItemRecurringPriceInsertQuery = format("INSERT INTO TSHOPPINGITEMRECURRINGPRICE "
						+ "(UIDPK, GUID, PAYMENT_SCHEDULE_NAME, FREQ_AMOUNT, FREQ_UNIT,ORDERSKU_UID) "
						+ "VALUES (%d, '%s', 'schedule', '1.0', 'day', %d)",
				uidPk, UUID.randomUUID(), sku3UidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(shopItemRecurringPriceInsertQuery));

		//shipment tax //ORDER_RETURN_UID
		String shipmentTaxInsertQuery = format("INSERT INTO TSHIPMENTTAX "
						+ "(UIDPK, TAX_CATEGORY_NAME, TAX_CATEGORY_DISPLAY_NAME, ORDER_SHIPMENT_UID) "
						+ "VALUES (%d, 'tax category name', 'tax category display', %d)",
				uidPk, uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(shipmentTaxInsertQuery));

		//order return sku
		String orderReturnSkuInsertQuery = format("INSERT INTO TORDERRETURNSKU "
						+ "(UIDPK, GUID, ORDER_SKU_UID, ORDER_RETURN_UID) "
						+ "VALUES (%d, '%s', %d, %d)",
				uidPk, UUID.randomUUID(), uidPk, uidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderReturnSkuInsertQuery));

		//create 3-level structure => parent -> child (parent) -> child

		//order sku parent
		String orderParentLevel1InsertQuery = format("INSERT INTO TORDERSKUPARENT "
						+ "(PARENT_UID, CHILD_UID) "
						+ "VALUES (%d, %d)",
				sku2UidPk, sku3UidPk);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderParentLevel1InsertQuery));

		//order sku child (and parent to the next order SKU
		String orderChildParentLevel2InsertQuery = format("INSERT INTO TORDERSKUPARENT "
						+ "(PARENT_UID, CHILD_UID) "
						+ "VALUES (%d, %d)",
				sku1UidPk, sku2UidPk);


		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderChildParentLevel2InsertQuery));

		return order;
	}

	private void preJobAssertions(final int expectedNumberOfRecords) {
		assertCountOfRecords(null, null, expectedNumberOfRecords);
	}

	private void postJobAssertions(final int expectedNumberOfRecords, final Order... orders) {
		//ensure that both L1 and L2 caches are not queried
		getPersistenceEngine().clearCache();

		for (Order order : orders) {
			assertCountOfRecords(order.getUidPk(), order.getOrderNumber(), expectedNumberOfRecords);
		}
	}

	private void assertCountOfRecords(final Long orderUidPk, final String orderNumber, final int expectedNumberOfRecords) {
		//assert number of records in tables in TORDER
		String whereSQL = getFormattedWhereSQL("WHERE UIDPK = %d", orderUidPk);
		assertExpectedNumber(expectedNumberOfRecords, new TableDTO("TORDER"), whereSQL);

		//assert number of records in tables with CUSTOMER_UID FK
		Arrays.stream(TABLES_BY_ORDER_UID)
				.forEach(tableDTO -> {
					String lambdaWhereSQL = getFormattedWhereSQL("WHERE ORDER_UID = %d", orderUidPk);
					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});

		//assert number of records in tables with ORDER_NUMBER FK
		Arrays.stream(TABLES_BY_ORDER_NUMBER)
				.forEach(tableDTO -> {
					String lambdaWhereSQL = getFormattedWhereSQL("WHERE ORDER_NUMBER = '%s'", orderNumber);

					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});

		//assert number of records in tables with ORDER_PAYMENT_UID FK
		Arrays.stream(TABLES_BY_ORDER_PAYMENT_UID)
				.forEach(tableDTO -> {
					StringBuilder whereSQLBuffer = new StringBuilder("target INNER JOIN TORDERPAYMENT joined ")
							.append("ON target.ORDER_PAYMENT_UID = joined.UIDPK ")
							.append("WHERE joined.ORDER_NUMBER = '%s'");

					String lambdaWhereSQL = getFormattedWhereSQL(whereSQLBuffer.toString(), orderNumber);
					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});

		//assert number of records in tables with APPLIED_RULE_UID FK
		Arrays.stream(TABLES_BY_APPLIED_RULE_UID)
				.forEach(tableDTO -> {
					StringBuilder whereSQLBuffer = new StringBuilder("target INNER JOIN TAPPLIEDRULE joined ")
							.append("ON target.APPLIED_RULE_UID = joined.UIDPK ")
							.append("WHERE joined.ORDER_UID = %d");

					String lambdaWhereSQL = getFormattedWhereSQL(whereSQLBuffer.toString(), orderUidPk);
					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});

		//assert number of records in tables with ORDER_SHIPMENT_UID FK
		Arrays.stream(TABLES_BY_ORDER_SHIPMENT_UID)
				.forEach(tableDTO -> {
					StringBuilder whereSQLBuffer = new StringBuilder("target INNER JOIN TORDERSHIPMENT joined ")
							.append("ON target.ORDER_SHIPMENT_UID = joined.UIDPK ")
							.append("WHERE joined.ORDER_UID = %d");

					String lambdaWhereSQL = getFormattedWhereSQL(whereSQLBuffer.toString(), orderUidPk);
					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});

		//assert number of records in tables with ORDER_RETURN_UID FK
		Arrays.stream(TABLES_BY_ORDER_RETURN_UID)
				.forEach(tableDTO -> {
					StringBuilder whereSQLBuffer = new StringBuilder("target INNER JOIN TORDERRETURN joined ")
							.append("ON target.ORDER_RETURN_UID = joined.UIDPK ")
							.append("WHERE joined.ORDER_UID = %d");

					String lambdaWhereSQL = getFormattedWhereSQL(whereSQLBuffer.toString(), orderUidPk);
					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});

		//assert number of records in tables with ORDER_SKU_UID FK
		Arrays.stream(TABLES_BY_ORDER_SKU_UID)
				.forEach(tableDTO -> {
					StringBuilder whereSQLBuffer = new StringBuilder("target INNER JOIN TORDERSKU joined ")
							.append("ON target.").append(tableDTO.getFkFieldName()).append(" = joined.UIDPK ")
							.append("WHERE joined.ORDER_UID = %d");

					String lambdaWhereSQL = getFormattedWhereSQL(whereSQLBuffer.toString(), orderUidPk);
					assertExpectedNumber(expectedNumberOfRecords, tableDTO, lambdaWhereSQL);
				});
	}

	private String getFormattedWhereSQL(final String whereSQL, final Object customerIdentifer) {
		return customerIdentifer == null
				? ""
				: format(whereSQL, customerIdentifer);
	}
	private long getCountForQuery(final String sqlQuery) {
		return testPersistenceEngine.<Long>retrieveNative(sqlQuery)
				.get(0);
	}

	private void assertExpectedNumber(final int expectedNumberOfRecords, final TableDTO tableDTO,
									  final String formattedWhereSQL) {
		int multipliedExpectedNumberOfRecords = expectedNumberOfRecords * tableDTO.getMultiPlier();

		assertThat(getCountForQuery(format("SELECT COUNT(*) FROM %s %s", tableDTO.getTableName(), formattedWhereSQL)))
				.as(format("The number of records is not %d", multipliedExpectedNumberOfRecords))
				.isEqualTo(multipliedExpectedNumberOfRecords);
	}

	private static class TableDTO {
		private final String tableName;
		private String fkFieldName = "";
		private int multiPlier = 1;

		TableDTO(final String tableName) {
			this(tableName, "");
		}

		TableDTO(final String tableName, final String fkFieldName) {
			this(tableName, fkFieldName, 1);
		}

		TableDTO(final String tableName, final String fkFieldName, final int multiPlier) {
			this.tableName = tableName;
			this.fkFieldName = fkFieldName;
			this.multiPlier = multiPlier;
		}

		public String getTableName() {
			return tableName;
		}

		public String getFkFieldName() {
			return fkFieldName;
		}

		public int getMultiPlier() {
			return multiPlier;
		}
	}

	private void removeEntityLifecycleListener() {
		OpenJPAEntityManagerFactorySPI spiEMFactory = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(batchEntityManagerFactory);
		spiEMFactory.removeLifecycleListener(databaseTimestampsEntityListener);
	}
}
