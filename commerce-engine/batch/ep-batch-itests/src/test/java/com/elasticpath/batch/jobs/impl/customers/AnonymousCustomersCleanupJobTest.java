/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.customers;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.batch.jobs.util.BundlePersister;
import com.elasticpath.batch.jobs.util.FailingJpaPersistenceEngine;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.TestPersistenceEngine;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.impl.DatabaseTimestampsEntityListener;
import com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.settings.provider.TestSettingValueProvider;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

@SuppressWarnings({"rawtypes", "unchecked"})
@DirtiesDatabase
public class AnonymousCustomersCleanupJobTest extends DbTestCase {
	/*
	 The shopping cart tables, except TSHOPPINGCART, are not included because the purging of those are covered with Inactive/Abandoned cleanup
	 job tests it's enough to check here whether TSHOPPINGCART is purged according to the specified conditions.

	 Tables: TORDER, TCUSTOMERAUTHENTICATION, TCUSTDEFAULTPAYMENTINSTRUMENT and TCUSTOMERPAYMENTINSTRUMENT, although related to
	 the TCUTSOMER table, are not relevant for anonymous customers because they are populated only by registered ones.
	 */

	//The integer value represents a multiplier, used for calculation of the number of expected records per customer
	private static final Pair<String, Integer>[] TABLES_BY_CUSTOMER_GUID = new Pair[]{Pair.of("TSHOPPER", 1),
			Pair.of("TOAUTHACCESSTOKEN", 1),
			Pair.of("TCUSTOMERCONSENTHISTORY", 1),
			Pair.of("TCUSTOMERCONSENT", 1)};

	private static final Pair<String, Integer>[] TABLES_BY_SHOPPER_UID = new Pair[]{Pair.of("TWISHLIST", 1),
			Pair.of("TSHOPPINGCART", 1)};

	private static final Pair<String, Integer>[] TABLES_BY_CUSTOMER_UID = new Pair[]{Pair.of("TCUSTOMERPROFILEVALUE", 5),
			Pair.of("TADDRESS", 1),
			Pair.of("TGIFTCERTIFICATE", 1)};

	private static final String COUNT_CUSTOMER_GROUPS_SQL = "SELECT count(cgx.*) FROM TCUSTOMERGROUPX cgx "
			+ "INNER JOIN TCUSTOMERGROUP cg ON cgx.CUSTOMERGROUP_UID=cg.UIDPK "
			+ "INNER JOIN TCUSTOMERGROUPROLEX cgrx ON cg.UIDPK = cgrx.CUSTOMER_GROUP_UID %s";

	private static final int MAX_HISTORY = -61;
	private static final int THREE = 3;
	private static final int FIVE = 5;
	private static final int ONE_K = 1000;

	@Autowired
	private TestPersistenceEngine testPersistenceEngine;
	@Autowired
	private CustomerBuilder customerBuilder;
	@Autowired
	private JpaPersistenceEngineImpl batchPersistenceEngineTarget;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private DatabaseTimestampsEntityListener databaseTimestampsEntityListener;
	@Autowired
	private EntityManagerFactory batchEntityManagerFactory;
	@Autowired
	private PurgeAnonymousCustomersBatchProcessor purgeAnonymousCustomersBatchProcessor;

	@Autowired
	private AnonymousCustomersCleanupJob anonymousCustomersCleanupJob;

	private BundlePersister bundlePersister;
	private String storeCode;
	private Long storeUid;

	@Override
	public void setUpDb() {
		SimpleStoreScenario testScenario = getTac().useScenario(SimpleStoreScenario.class);
		Store store = testScenario.getStore();
		storeCode = store.getCode();
		storeUid = store.getUidPk();

		bundlePersister = new BundlePersister(persisterFactory, testScenario);
		bundlePersister.persistBundle();

		anonymousCustomersCleanupJob.setConfigBatchSize(new TestSettingValueProvider(1));
	}

	//1. case - purge only valid anonymous customers (last time edited more than preconfigured (MAX_HISTORY) number of days and without orders)
	@Test
	public void shouldPurgeOnlyExpiredAnonymousCustomersWithoutOrders() {
		Customer purgeableCustomer1 = createCustomer("Anonymous", "One", true, false);
		Customer purgeableCustomer2 = createCustomer("Anonymous", "Two", true, false);
		//anonymous customer not expired
		Customer nonPurgeableCustomer1 = createCustomer("Anonymous", "Three", false, false);
		//anonymous customer is expired but has orders
		Customer nonPurgeableCustomer2 = createCustomer("Anonymous", "Four", true, true);
		//anonymous customer not expired but has orders
		Customer nonPurgeableCustomer3 = createCustomer("Anonymous", "Five", false, true);

		preJobAssertions(FIVE);

		removeEntityLifecycleListener();

		anonymousCustomersCleanupJob.execute();

		// assert that only valid customers and relevant entities are purged
		postJobAssertions(0, purgeableCustomer1, purgeableCustomer2);

		// assert that non-expired customers with/without orders and relevant entities are NOT purged
		postJobAssertions(1, nonPurgeableCustomer1, nonPurgeableCustomer2, nonPurgeableCustomer3);
	}

	/* 2. batch processing consists of 2 stages - pre-processing and actual execution; The error may occur at any stage and the processing
	   should not stop as long as there are valid records

	   This job will find 3 valid anonymous customers, but it will process 2 because the exception will be thrown when processing the
	   "exceptionalCustomer"
	 */
	@Test
	public void shouldSkipABatchAndContinuePurgingWhenErrorOccursAtAnyProcessingStage() {
		Customer purgeableCustomer1 = createCustomer("Anonymous", "One", true, false);

		//an exception will be thrown when processing this customer in the "executeBulkOperations" method
		Customer exceptionalCustomer = createCustomer("Anonymous", "Exceptional", true, false);
		Customer purgeableCustomer2 = createCustomer("Anonymous", "Three", true, false);

		preJobAssertions(THREE);

		initJobWithPersistenceEngineThrowingException(exceptionalCustomer.getUidPk());

		removeEntityLifecycleListener();

		anonymousCustomersCleanupJob.execute();

		// assert that only valid customers and relevant entities are purged
		postJobAssertions(0, purgeableCustomer1, purgeableCustomer2);

		// assert that non-expired customers with/without orders and relevant entities are NOT purged
		postJobAssertions(1, exceptionalCustomer);
	}

	/* 3. batch processing consists of 2 stages - pre-processing and actual execution;
	   If both stages completed successfully, then the changes are committed to the db.
	   At that point, various issues may occur (network, intermittent connection loss etc) and the TX manager will throw an exception.
	   In such cases, the processing should not stop because the underlying issue may be fixed before processing the next batch.

	   In this test, the "exceptionalCustomer" will have an order assigned to it, although it shouldn't have it.
	   The existence of the order will trigger a db exception when transaction is about to be be committed.
	 */
	@Test
	public void shouldSkipABatchAndContinuePurgingWhenErrorOccursOnTransactionCommit() {
		Customer purgeableCustomer1 = createCustomer("Anonymous", "One", true, false);
		//an exception will be thrown on transaction commit due to existence of an order record
		Customer exceptionalCustomer = createCustomer("Anonymous", "Exceptional", true, false);
		createOrderForAnonymousCustomer(exceptionalCustomer.getUidPk());

		Customer purgeableCustomer2 = createCustomer("Anonymous", "Three", true, false);

		preJobAssertions(THREE);

		removeEntityLifecycleListener();

		anonymousCustomersCleanupJob.execute();

		// assert that only valid customers and relevant entities are purged
		postJobAssertions(0, purgeableCustomer1, purgeableCustomer2);

		// assert that non-expired customers with/without orders and relevant entities are NOT purged
		postJobAssertions(1, exceptionalCustomer);
	}

	private Customer createCustomer(final String firstName, final String lastName, final boolean isExpired, final boolean hasOrders) {
		final Customer customer = customerBuilder
				.withCustomerType(CustomerType.SINGLE_SESSION_USER)
				.withFirstName(firstName)
				.withLastName(lastName)
				.withStoreCode(storeCode)
				.build();

		customerService.add(customer);

		long customerUid = customer.getUidPk();
		String customerGuid = customer.getGuid();

		if (isExpired) {
			Date lastEditDate = (DateUtils.addDays(new Date(), MAX_HISTORY));
			doInTransaction(status -> getPersistenceEngine().executeQuery("UPDATE CustomerImpl cust SET cust.lastEditDate = ?1  "
					+ "WHERE cust.uidPk = ?2", lastEditDate, customerUid));
		}

		if (hasOrders) {
			createOrderForAnonymousCustomer(customerUid);
		}

		//populate other tables

		//shopper
		String shopperInsertQuery = format("INSERT INTO TSHOPPER (UIDPK, GUID, CUSTOMER_GUID) VALUES (%d,'%s','%s')",
				customerUid, UUID.randomUUID(), customerGuid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(shopperInsertQuery));

		//oauth token
		String oauthTokenInsertQuery = format("INSERT INTO TOAUTHACCESSTOKEN "
						+ "(UIDPK, TOKEN_ID, EXPIRY_DATE, TOKEN_TYPE, STORECODE, CLIENT_ID ,CUSTOMER_GUID, CUSTOMER_ROLE) "
						+ "VALUES (%d,'%s','2020-01-24 13:38:11','bearer','%s','ep_client_id','%s','PUBLIC')",
				customerUid, UUID.randomUUID(), storeCode, customerGuid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(oauthTokenInsertQuery));

		//data policy
		String dataPolicyInsertQuery = format("INSERT INTO TDATAPOLICY "
				+ "(UIDPK, GUID, POLICY_NAME, RETENTION_PERIOD, RETENTION_TYPE, STATE, REFERENCE_KEY) "
				+ "VALUES (%d,'%s','Policy_%s',5000,1,1,'REfKey_%s')", customerUid, UUID.randomUUID(), customerUid, customerGuid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(dataPolicyInsertQuery));

		//customer consent
		String consentInsertQuery = format("INSERT INTO TCUSTOMERCONSENT "
				+ "(UIDPK, GUID, DATAPOLICY_UID, CUSTOMER_GUID, CONSENT_DATE,ACTION) "
				+ "VALUES (%d,'%s',%d,'%s','2020-01-24 13:38:11',1)", customerUid, UUID.randomUUID(), customerUid, customerGuid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(consentInsertQuery));

		//customer consent history
		String consentHistoryInsertQuery = format("INSERT INTO TCUSTOMERCONSENTHISTORY "
				+ "(UIDPK, GUID, DATAPOLICY_UID, CUSTOMER_GUID, CONSENT_DATE, ACTION) "
				+ "VALUES (%d,'%s',%d,'%s','2020-01-24 13:38:11', 1)", customerUid, UUID.randomUUID(), customerUid, customerGuid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(consentHistoryInsertQuery));

		//wishlist
		String wishListInsertQuery = format("INSERT INTO TWISHLIST "
				+ "(UIDPK, GUID, SHOPPER_UID) "
				+ "VALUES (%d,'%s',%d)", customerUid, UUID.randomUUID(), customerUid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(wishListInsertQuery));

		//shopping cart
		String cartInsertQuery = format("INSERT INTO TSHOPPINGCART "
				+ "(UIDPK, GUID, SHOPPER_UID, STORECODE) "
				+ "VALUES (%d,'%s',%d,'%s')", customerUid, UUID.randomUUID(), customerUid, storeCode);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(cartInsertQuery));

		//address
		String addressInsertQuery = format("INSERT INTO TADDRESS "
				+ "(UIDPK, GUID, CUSTOMER_UID) "
				+ "VALUES (%d,'%s',%d)", customerUid, UUID.randomUUID(), customerUid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(addressInsertQuery));

		//gift certificates
		String giftCertificateInsertQuery = format("INSERT INTO TGIFTCERTIFICATE "
				+ "(UIDPK, GUID, CUSTOMER_UID,CREATED_DATE,STORE_UID) "
				+ "VALUES (%d,'%s',%d,'2020-01-24 13:38:11',%d)", customerUid, UUID.randomUUID(), customerUid, storeUid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(giftCertificateInsertQuery));

		//bundle item
		String bundleItemInsertQuery = format("INSERT INTO TCARTITEM "
				+ "(UIDPK, GUID, QUANTITY, PARENT_ITEM_UID, SHOPPING_CART_UID, SKU_GUID, CHILD_ITEM_CART_UID, ITEM_TYPE) "
				+ "VALUES (%d,'%s',1, null, %d, '%s', null, 1)", customerUid, UUID.randomUUID(), customerUid,
				bundlePersister.getBundleProductSku().getGuid());

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(bundleItemInsertQuery));

		//bundle constituent
		String bundleConstituentItemInsertQuery = format("INSERT INTO TCARTITEM "
				+ "(UIDPK, GUID, QUANTITY, PARENT_ITEM_UID, SHOPPING_CART_UID, SKU_GUID, CHILD_ITEM_CART_UID, ITEM_TYPE) "
				+ "VALUES (%d,'%s',1, %d, null, '%s', %d, 4)", customerUid + ONE_K, UUID.randomUUID(), customerUid,
				bundlePersister.getMainSimpleConstituentSku().getGuid(),	customerUid);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(bundleConstituentItemInsertQuery));


		return customer;
	}

	/* This method is used to trigger an error on transaction commit.

	   An order will be artificially created for an anonymous customer, that normally doesn't have any orders and will appear in search results
	   i.e. a batch. The job will try to purge such customer but because of foreign constraint between TORDER and TCUSTOMER tables, the transaction
	   will fail. This is one of the possible ways to trigger such kind of exceptions.
	 */
	private void createOrderForAnonymousCustomer(final Long customerUidPk) {
		String orderInsertQuery = format("INSERT INTO TORDER "
				+ "(UIDPK, LAST_MODIFIED_DATE, CREATED_DATE, ORDER_NUMBER, CUSTOMER_UID, STORECODE, LOCALE) "
				+ "VALUES (%d, '2020-01-24 13:38:11', '2020-01-24 13:38:11','%s',%d,'%s','en')",
				customerUidPk, UUID.randomUUID(), customerUidPk, storeCode);

		doInTransaction(status -> testPersistenceEngine.executeNativeQuery(orderInsertQuery));
	}

	private void preJobAssertions(final int expectedNumberOfRecords) {
		assertCountOfRecords(null, null, expectedNumberOfRecords);
	}

	private void postJobAssertions(final int expectedNumberOfRecords, final Customer... customers) {
		//ensure that both L1 and L2 caches are not queried
		getPersistenceEngine().clearCache();

		for (Customer customer : customers) {
			assertCountOfRecords(customer.getUidPk(), customer.getGuid(), expectedNumberOfRecords);
		}
	}

	private void assertCountOfRecords(final Long customerUidPk, final String customerGuid, final int expectedNumberOfRecords) {
		//assert number of records in tables in TCUSTOMER
		String whereSQL = getFormattedWhereSQL("WHERE UIDPK = '%s'", customerUidPk);
		assertExpectedNumber(expectedNumberOfRecords, Pair.of("TCUSTOMER", 1), whereSQL);

		//assert number of records in tables with CUSTOMER_UID FK
		Arrays.stream(TABLES_BY_CUSTOMER_UID)
				.forEach(pair -> {
					String lambdaWhereSQL = getFormattedWhereSQL("WHERE CUSTOMER_UID = '%s'", customerUidPk);
					assertExpectedNumber(expectedNumberOfRecords, pair, lambdaWhereSQL);
				});

		//assert number of records in tables with CUSTOMER_GUID FK
		Arrays.stream(TABLES_BY_CUSTOMER_GUID)
				.forEach(pair -> {
					String lambdaWhereSQL = getFormattedWhereSQL("WHERE CUSTOMER_GUID = '%s'", customerGuid);
					assertExpectedNumber(expectedNumberOfRecords, pair, lambdaWhereSQL);
				});

		//assert number of records in tables with SHOPPER_UID FK
		Arrays.stream(TABLES_BY_SHOPPER_UID)
				.forEach(pair -> {
					StringBuilder whereSQLBuffer = new StringBuilder("target INNER JOIN TSHOPPER shopper ")
							.append("ON target.SHOPPER_UID = shopper.UIDPK ")
							.append("WHERE shopper.CUSTOMER_GUID = '%s'");

					String lambdaWhereSQL = getFormattedWhereSQL(whereSQLBuffer.toString(), customerGuid);
					assertExpectedNumber(expectedNumberOfRecords, pair, lambdaWhereSQL);
				});

		//assert number of records in TCUSTOMERGROUP, TCUSTOMERGROUPX and TCUSTOMERGROUPROLEX tables
		whereSQL = getFormattedWhereSQL("WHERE cgx.CUSTOMER_UID = '%s'", customerUidPk);
		assertThat(getCountForQuery(format(COUNT_CUSTOMER_GROUPS_SQL, whereSQL)))
				.as(format("The number of records is not %d", expectedNumberOfRecords))
				.isEqualTo(expectedNumberOfRecords);
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

	private void assertExpectedNumber(final int expectedNumberOfRecords, final Pair<String, Integer> pairTableNameAndMultiplier,
									  final String formattedWhereSQL) {
		int multipliedExpectedNumberOfRecords = expectedNumberOfRecords * pairTableNameAndMultiplier.getRight();

		assertThat(getCountForQuery(format("SELECT COUNT(*) FROM %s %s", pairTableNameAndMultiplier.getLeft(), formattedWhereSQL)))
				.as(format("The number of records is not %d", multipliedExpectedNumberOfRecords))
				.isEqualTo(multipliedExpectedNumberOfRecords);
	}

	private void initJobWithPersistenceEngineThrowingException(final long customerUidToFailFor) {
		PersistenceEngine failingJpaPersistenceEngine = new FailingJpaPersistenceEngine(batchPersistenceEngineTarget, customerUidToFailFor,
				"DELETE_CUSTOMERS_BY_UIDS");
		purgeAnonymousCustomersBatchProcessor.setPersistenceEngine(failingJpaPersistenceEngine);
	}

	private void removeEntityLifecycleListener() {
		OpenJPAEntityManagerFactorySPI spiEMFactory = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(batchEntityManagerFactory);
		spiEMFactory.removeLifecycleListener(databaseTimestampsEntityListener);
	}
}
