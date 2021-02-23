/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs.impl.carts;

import static com.elasticpath.domain.customer.Customer.STATUS_ACTIVE;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.batch.jobs.util.BundlePersister;
import com.elasticpath.batch.jobs.util.FailingJpaPersistenceEngine;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.cartorder.impl.CartOrderImpl;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.impl.AbstractShoppingItemImpl;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.CartOrderPaymentInstrumentImpl;
import com.elasticpath.domain.quantity.Quantity;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsageType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.shopper.impl.ShopperImpl;
import com.elasticpath.domain.shopper.impl.ShopperMementoImpl;
import com.elasticpath.domain.shoppingcart.ItemType;
import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemRecurringPrice;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartMementoImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartStatus;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemRecurringPriceImpl;
import com.elasticpath.domain.shoppingcart.impl.ShoppingItemSimplePrice;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.tax.TaxCode;
import com.elasticpath.money.Money;
import com.elasticpath.persistence.TestPersistenceEngine;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.impl.DatabaseTimestampsEntityListener;
import com.elasticpath.persistence.openjpa.impl.JpaPersistenceEngineImpl;
import com.elasticpath.sellingchannel.impl.ShoppingItemRecurringPriceAssemblerImpl;
import com.elasticpath.service.pricing.impl.PaymentScheduleHelperImpl;
import com.elasticpath.settings.provider.TestSettingValueProvider;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

public abstract class AbstractCleanupCartsJobTest<ENT> extends DbTestCase {

	private static final String COUNT_CARTS_SQL = "SELECT count(*) FROM TSHOPPINGCART %s";
	private static final String COUNT_CART_ORDERS_SQL = "SELECT count(*) FROM TCARTORDER %s";
	private static final String COUNT_CART_ORDER_PAYMENT_INSTRUMENTS_SQL = "SELECT count(*) FROM TCARTORDERPAYMENTINSTRUMENT %s";
	private static final String COUNT_SHOPPING_ITEM_RECURRING_PRICES_SQL = "SELECT count(*) FROM TSHOPPINGITEMRECURRINGPRICE %s";
	private static final String COUNT_SHOPPING_ITEMS_SQL = "SELECT count(*) FROM TCARTITEM %s";
	private static final String COUNT_SHOPPING_ITEM_DATA_SQL = "SELECT count(*) FROM TSHOPPINGITEMDATA %s";
	private static final String COUNT_CART_DATA_SQL = "SELECT count(*) FROM TCARTDATA %s";
	private static final String COUNT_CART_ORDER_COUPONS_SQL = "SELECT count(*) FROM TCARTORDERCOUPON %s";

	private static final Currency CAD_CURRENCY = Currency.getInstance(Locale.CANADA);
	private static final Quantity MONTHLY_QTY = new Quantity(1, "month");
	private static final String PROMO_1_CODE = "PROMO1";
	private static final String PROMO_2_CODE = "PROMO2";

	private static final int MULTIPLIER_FIVE = 5;
	private static final int MULTIPLIER_TWO = 2;

	protected abstract AbstractBatchJob<ENT> getBatchJob();
	protected abstract Date getDesiredInvalidLastModifiedDate();
	protected abstract Date getDesiredValidLastModifiedDate();
	protected abstract ShoppingCartStatus getDesiredInvalidCartStatus();
	protected abstract ShoppingCartStatus getDesiredValidCartStatus();

	@Autowired
	private TestPersistenceEngine testPersistenceEngine;
	@Autowired
	private ShoppingItemRecurringPriceAssemblerImpl shoppingItemRecurringPriceAssembler;
	@Autowired
	private JpaPersistenceEngineImpl batchPersistenceEngineTarget;
	@Autowired
	private PaymentScheduleHelperImpl paymentScheduleHelper;
	@Autowired
	private DatabaseTimestampsEntityListener databaseTimestampsEntityListener;
	@Autowired
	private EntityManagerFactory batchEntityManagerFactory;
	@Autowired
	private PurgeCartsBatchProcessor purgeCartsBatchProcessor;

	private Store store;
	private Shopper shopper;

	private ProductSku simpleProductSku;

	private BundlePersister bundlePersister;
	private TaxCode taxCode;

	@Override
	public void setUpDb() {
		shoppingItemRecurringPriceAssembler.setPaymentScheduleHelper(paymentScheduleHelper);

		SimpleStoreScenario testScenario = getTac().useScenario(SimpleStoreScenario.class);

		store = testScenario.getStore();

		persistCoupon("Promotion One", PROMO_1_CODE, "COUPON1");
		persistCoupon("Promotion Two", PROMO_2_CODE, "COUPON2");

		bundlePersister = new BundlePersister(persisterFactory, testScenario);
		bundlePersister.persistBundle();

		simpleProductSku = bundlePersister.persistSimpleProduct("SimpleCleanupJob");

		persistShopper();

		getBatchJob().setConfigBatchSize(new TestSettingValueProvider(1));
	}

	/**
	 * Testing a case when only invalid carts (inactive/abandoned) exist in the db.
	 * All invalid carts must be purged.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void shouldPurgeAllCartsWhenAllAreInvalid() {
		ShoppingCartMemento cartOneToPurge = persistShoppingCart(getDesiredInvalidCartStatus(), getDesiredInvalidLastModifiedDate());
		ShoppingCartMemento cartTwoToPurge = persistShoppingCart(getDesiredInvalidCartStatus(), getDesiredInvalidLastModifiedDate());

		//verify the number of carts, cart orders and CO payment instruments before running the job
		preJobAssertions(2);

		removeEntityLifecycleListener();
		//run the job
		getBatchJob().execute();

		// assert that carts and relevant entities are purged
		postJobAssertions(cartOneToPurge, cartTwoToPurge);
	}

	/**
	 * Testing a case when both valid (e.g. active cart or cart not older than CURRENT_TIME - MAX_HISTORY_DAYS) and invalid (inactive/abandoned)
	 * carts exist in the db.
	 *
	 * Only invalid carts must be purged.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void shouldPurgeOnlyInvalidCarts() {
		final int expectedNumberOfCreatedCarts = 3;

		ShoppingCartMemento cartOneToPurge = persistShoppingCart(getDesiredInvalidCartStatus(), getDesiredInvalidLastModifiedDate());
		ShoppingCartMemento cartTwoToPurge = persistShoppingCart(getDesiredInvalidCartStatus(), getDesiredInvalidLastModifiedDate());
		ShoppingCartMemento validCart = persistShoppingCart(getDesiredValidCartStatus(), getDesiredValidLastModifiedDate());

		preJobAssertions(expectedNumberOfCreatedCarts);

		removeEntityLifecycleListener();

		getBatchJob().execute();

		//verify that first 2 carts (and relevant entities) are purged
		postJobAssertions(cartOneToPurge, cartTwoToPurge);
		//the third one (along with dependent entities) shouldn't be because it's valid
		assertCountOfRecords(validCart.getGuid(), 1);
	}

	/**
	 * Testing a case when error occurs during purging of a batch of invalid carts.
	 * The purging must work regardless of the error, as long as there are carts to process.
	 * Failed batches will be handled in the next run.
	 */
	@Test
	@DirtiesDatabase
	@SuppressWarnings("PMD.PrematureDeclaration")
	public void shouldContinuePurgingInCaseOfErrors() {
		ShoppingCartMemento nonPurgableCart = persistShoppingCart(getDesiredInvalidCartStatus(), getDesiredInvalidLastModifiedDate());
		ShoppingCartMemento purgableCart = persistShoppingCart(getDesiredInvalidCartStatus(), getDesiredInvalidLastModifiedDate());

		preJobAssertions(2);

		//create a persistence engine with a method that will throw exception under given conditions
		initJobWithPersistenceEngineThrowingException(nonPurgableCart.getUidPk());

		removeEntityLifecycleListener();
		//run the job
		getBatchJob().execute();

		//verify that first cart (and relevant entities) is purged
		postJobAssertions(purgableCart);
		//the second cart (along with dependent entities)  must exist
		assertCountOfRecords(nonPurgableCart.getGuid(), 1);
	}

	private void preJobAssertions(final int expectedNumberOfRecords) {
		assertCountOfRecords(null, expectedNumberOfRecords);
	}

	//asserted carts and relevant tables must be completely purged - 0 records
	private void postJobAssertions(final ShoppingCartMemento... carts) {
		//ensure that both L1 and L2 caches are not queried
		getPersistenceEngine().clearCache();

		for (ShoppingCartMemento cart : carts) {
			assertCountOfRecords(cart.getGuid(), 0);
		}
	}

	/* If cart GUID is provided then this method is used for individual cart assertions in special cases,
	   like exceptions or carts that do not meet criteria for purging.

	   Otherwise, asserts the total number of records per entity

	 */
	private void assertCountOfRecords(final String cartGuid, final int expectedNumberOfRecords) {
		int expectedTwiceMore = expectedNumberOfRecords * MULTIPLIER_TWO;

		//check carts
		String whereSQL = getFormattedWhereSQL("WHERE GUID = '%s'", cartGuid);

		assertThat(getCountForQuery(format(COUNT_CARTS_SQL, whereSQL)))
				.as(format("The number of carts is not %d", expectedNumberOfRecords))
				.isEqualTo(expectedNumberOfRecords);

		//check cart orders
		whereSQL = getFormattedWhereSQL("WHERE SHOPPINGCART_GUID = '%s'", cartGuid);

		assertThat(getCountForQuery(format(COUNT_CART_ORDERS_SQL, whereSQL)))
				.as(format("The number of cart orders is not %d", expectedNumberOfRecords))
				.isEqualTo(expectedNumberOfRecords);

		whereSQL = getFormattedWhereSQL("WHERE CARTORDER_UID IN (SELECT UIDPK FROM TCARTORDER WHERE SHOPPINGCART_GUID = '%s')", cartGuid);

		assertThat(getCountForQuery(format(COUNT_CART_ORDER_COUPONS_SQL, whereSQL)))
				.as(format("The list of created cart order coupons must be %d", expectedTwiceMore))
				.isEqualTo(expectedTwiceMore);

		//check cart order payment instruments
		whereSQL = getFormattedWhereSQL("WHERE CART_ORDER_UID IN (SELECT UIDPK FROM TCARTORDER WHERE SHOPPINGCART_GUID = '%s')", cartGuid);

		assertThat(getCountForQuery(format(COUNT_CART_ORDER_PAYMENT_INSTRUMENTS_SQL, whereSQL)))
				.as(format("The number of cart order payment instruments is not %d", expectedNumberOfRecords))
				.isEqualTo(expectedNumberOfRecords);

		assertDependencies(cartGuid, expectedNumberOfRecords);

	}

	private String getFormattedWhereSQL(final String whereSQL, final String cartGuid) {
		return cartGuid == null
				? ""
				: format(whereSQL, cartGuid);
	}

	/*
		The number of dependent records(like shopping item recurring price, shopping item, shopping item and cart data) can't asserted
		per cart because there the records are created and linked to their parents, thus there is no e.g. cart UidPk field that we could query on.

		In pre-assertions, this doesn't matter because we care about the total number.
		In post-assertions, where we'd like to check per cart, this is not possible due to aforementioned reasons, but if used at the last stage
		(after verifying that desired cart is not deleted) we can verify that the number of existing records corresponds to the number of
		non-deleted carts (multiplied by 2).

	 */
	private void assertDependencies(final String cartGuid, final int expectedNumberOfRecords) {

		int expectedFiveTimesMore = expectedNumberOfRecords * MULTIPLIER_FIVE;
		int expectedTwiceMore = expectedNumberOfRecords * MULTIPLIER_TWO;

		String whereSQL = "";

		if (cartGuid != null) {
			StringBuilder queryBuffer = new StringBuilder("WHERE CARTITEM_UID IN (SELECT ci.UIDPK FROM TCARTITEM ci")
					.append(" INNER JOIN TSHOPPINGCART cart ON ci.SHOPPING_CART_UID = cart.UIDPK OR ci.CHILD_ITEM_CART_UID = cart.UIDPK")
					.append(" WHERE cart.GUID = '%s')");

			whereSQL = format(queryBuffer.toString(), cartGuid);
		}

		assertThat(getCountForQuery(format(COUNT_SHOPPING_ITEM_RECURRING_PRICES_SQL, whereSQL)))
				.as(format("The list of created shopping item recurring prices must be %d", expectedFiveTimesMore))
				.isEqualTo(expectedFiveTimesMore);

		assertThat(getCountForQuery(format(COUNT_SHOPPING_ITEM_DATA_SQL, whereSQL)))
				.as(format("The list of created shopping item data must be %d", expectedTwiceMore))
				.isEqualTo(expectedTwiceMore);

		whereSQL = "";
		if (cartGuid != null) {
			StringBuilder queryBuffer = new StringBuilder("ci INNER JOIN TSHOPPINGCART cart ON ci.SHOPPING_CART_UID = cart.UIDPK")
					.append(" OR ci.CHILD_ITEM_CART_UID = cart.UIDPK")
					.append(" WHERE cart.GUID = '%s'");

			whereSQL = format(queryBuffer.toString(), cartGuid);
		}

		assertThat(getCountForQuery(format(COUNT_SHOPPING_ITEMS_SQL, whereSQL)))
				.as(format("The list of created shopping items must be %d", expectedFiveTimesMore))
				.isEqualTo(expectedFiveTimesMore);

		whereSQL = getFormattedWhereSQL("WHERE SHOPPING_CART_UID IN (SELECT UIDPK FROM TSHOPPINGCART WHERE GUID = '%s')", cartGuid);

		assertThat(getCountForQuery(format(COUNT_CART_DATA_SQL, whereSQL)))
				.as(format("The list of created cart data must be %d", expectedTwiceMore))
				.isEqualTo(expectedTwiceMore);
	}

	private long getCountForQuery(final String sqlQuery) {
		return testPersistenceEngine.<Long>retrieveNative(sqlQuery)
				.get(0);
	}

	// ===== persist util methods ============
	private void persistSimpleShoppingCartPromotion(final String promotionName, final String promotionCode) {
		persisterFactory.getPromotionTestPersister().createAndPersistSimpleShoppingCartPromotion(promotionName, store.getCode(),
				promotionCode, true);
	}

	private void persistCoupon(final String promotionName, final String promoCode, final String couponCode) {
		persistSimpleShoppingCartPromotion(promotionName, promoCode);

		CouponConfig couponConfig = persisterFactory.getCouponTestPersister().createAndPersistCouponConfig(promoCode, 1,
				CouponUsageType.LIMIT_PER_COUPON);
		persisterFactory.getCouponTestPersister().createAndPersistCoupon(couponConfig, couponCode);
	}

	private void persistShopper() {
		Customer customer = new CustomerImpl();
		customer.setStoreCode(store.getCode());
		customer.setStatus(STATUS_ACTIVE);
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer.initialize();

		doInTransaction(status -> saveOrUpdate(customer));

		ShopperMemento shopperMemento = new ShopperMementoImpl();
		shopperMemento.setCustomer(customer);
		shopperMemento.setGuid("shopperGuid");
		shopperMemento.setStoreCode(store.getCode());

		doInTransaction(status -> saveOrUpdate(shopperMemento));

		shopper = new ShopperImpl();
		shopper.setShopperMemento(shopperMemento);
	}

	private ShoppingCartMemento persistShoppingCart(final ShoppingCartStatus desiredShoppingCartStatus, final Date desiredLastModifiedDate) {
		ShoppingCartMemento shoppingCartMemento = new ShoppingCartMementoImpl();
		shoppingCartMemento.setStatus(desiredShoppingCartStatus);
		shoppingCartMemento.setGuid(UUID.randomUUID().toString());
		shoppingCartMemento.setShopper(shopper);
		shoppingCartMemento.setStoreCode(store.getCode());
		shoppingCartMemento.setLastModifiedDate(new Date());
		shoppingCartMemento.setDefault(false);

		persistCartData(shoppingCartMemento);

		saveShoppingCart(shoppingCartMemento);

		//must post-update the LDM because it's always set to the current date after calling saveOrUpdate
		doInTransaction(status -> getPersistenceEngine().executeQuery("UPDATE ShoppingCartMementoImpl cart "
				+ "SET cart.lastModifiedDate = ?1  WHERE cart.uidPk = ?2", desiredLastModifiedDate, shoppingCartMemento.getUidPk()));

		persistCarOrderWithPaymentInstrument(shoppingCartMemento.getGuid());

		return shoppingCartMemento;
	}

	private void saveShoppingCart(final ShoppingCartMemento shoppingCartMemento) {
		doInTransaction(status -> saveOrUpdate(shoppingCartMemento));
		persistShoppingItemsWithData(shoppingCartMemento);
		doInTransaction(status -> saveOrUpdate(shoppingCartMemento));
	}

	private <T extends Persistable> T saveOrUpdate(final T entity) {
		return getPersistenceEngine().saveOrUpdate(entity);
	}

	private void persistCartData(final ShoppingCartMemento shoppingCartMemento) {
		Map<String, CartData> cartDataMap = new HashMap<>();
		CartData cartData1 = new CartData("cdKey1", "cdValue1");
		CartData cartData2 = new CartData("cdKey2", "cdValue2");

		cartDataMap.put(cartData1.getKey(), cartData1);
		cartDataMap.put(cartData2.getKey(), cartData2);

		shoppingCartMemento.setCartData(cartDataMap);
	}

	private void persistShoppingItemsWithData(final ShoppingCartMemento shoppingCartMemento) {
		final int listPrice = 10;

		List<ShoppingItem> shoppingItems = new ArrayList<>();

		Price price = new PriceImpl();
		price.setCurrency(CAD_CURRENCY);
		price.setListPrice(Money.valueOf(listPrice, CAD_CURRENCY));

		final ShoppingItemSimplePrice simplePrice = new ShoppingItemSimplePrice(price, 1);

		final Price priceObjectToBeAssembled = new PriceImpl();
		priceObjectToBeAssembled.setCurrency(CAD_CURRENCY);

		final Set<ShoppingItemRecurringPrice> recurringPriceSet = new HashSet<>();

		final ShoppingItemRecurringPrice shoppingItemRecurringPrice = new ShoppingItemRecurringPriceImpl();
		shoppingItemRecurringPrice.setSimplePrice(simplePrice);
		shoppingItemRecurringPrice.setPaymentScheduleName("payment schedule 1");
		shoppingItemRecurringPrice.setPaymentFrequency(MONTHLY_QTY);

		recurringPriceSet.add(shoppingItemRecurringPrice);

		shoppingItemRecurringPriceAssembler.assemblePrice(priceObjectToBeAssembled, recurringPriceSet);

		for (int i = 0; i < 2; i++) {
			AbstractShoppingItemImpl shoppingItem = new ShoppingItemImpl();
			shoppingItem.setFieldValue(UUID.randomUUID().toString(), "shopping item value");
			shoppingItem.setShoppingItemRecurringPriceAssembler(shoppingItemRecurringPriceAssembler);
			shoppingItem.setPrice(1, priceObjectToBeAssembled);
			shoppingItem.initialize();
			if (i == 0) {
				shoppingItem.setItemType(ItemType.SIMPLE);
				shoppingItem.setSkuGuid(simpleProductSku.getGuid());
			} else {
				shoppingItem.setItemType(ItemType.BUNDLE);
				shoppingItem.setSkuGuid(bundlePersister.getBundleProductSku().getGuid());

				long shoppingCartUidPk = shoppingCartMemento.getUidPk();

				ShoppingItem mainBundleConstituentItemOne = createBundleConstituent(bundlePersister.getMainSimpleConstituentSku().getGuid(),
						shoppingCartUidPk, priceObjectToBeAssembled);

				shoppingItem.addChildItem(mainBundleConstituentItemOne);

				ShoppingItem mainBundleConstituentItemTwo = createBundleConstituent(bundlePersister.getLevel2BundleSKU().getGuid(),
						shoppingCartUidPk, priceObjectToBeAssembled);

				ShoppingItem level2ConstituentItem = createBundleConstituent(bundlePersister.getLevel2ConstituentSKU().getGuid(), shoppingCartUidPk,
						priceObjectToBeAssembled);

				mainBundleConstituentItemTwo.addChildItem(level2ConstituentItem);
				mainBundleConstituentItemTwo.setChildItemCartUid(shoppingCartMemento.getUidPk());

				shoppingItem.addChildItem(mainBundleConstituentItemTwo);
			}

			shoppingItems.add(shoppingItem);
		}

		shoppingCartMemento.setAllItems(shoppingItems);
	}

	private ShoppingItem createBundleConstituent(final String skuGuid, final long shoppingCartUidPk, final Price priceObjectToBeAssembled) {
		ShoppingItemImpl bundleConstituent = new ShoppingItemImpl();

		bundleConstituent.setPrice(1, priceObjectToBeAssembled);
		bundleConstituent.initialize();
		bundleConstituent.setItemType(ItemType.BUNDLE_CONSTITUENT);
		bundleConstituent.setSkuGuid(skuGuid);
		bundleConstituent.setChildItemCartUid(shoppingCartUidPk);

		return bundleConstituent;
	}

	private void persistCarOrderWithPaymentInstrument(final String cartGuid) {
		CartOrder cartOrder = new CartOrderImpl();
		cartOrder.setShoppingCartGuid(cartGuid);
		cartOrder.initialize();
		cartOrder.addCoupons(Arrays.asList("COUPON1", "COUPON2"));

		doInTransaction(status -> saveOrUpdate(cartOrder));

		CartOrderPaymentInstrument cartOrderPaymentInstrument = new CartOrderPaymentInstrumentImpl();
		cartOrderPaymentInstrument.setCartOrderUid(cartOrder.getUidPk());
		cartOrderPaymentInstrument.initialize();
		cartOrderPaymentInstrument.setPaymentInstrumentGuid("DummyPayInsGuid");

		doInTransaction(status -> saveOrUpdate(cartOrderPaymentInstrument));
	}

	//============== util methods ================

	//create a PersistenceEngine with a method that will throw exception under given condition
	private void initJobWithPersistenceEngineThrowingException(final long cartUidToFailFor) {
		PersistenceEngine failingJpaPersistenceEngine = new FailingJpaPersistenceEngine(batchPersistenceEngineTarget, cartUidToFailFor,
				"DELETE_SHOPPING_CART_BY_UIDS");
		purgeCartsBatchProcessor.setPersistenceEngine(failingJpaPersistenceEngine);
	}

	/*
		For starter, read the comment in the integration-context.xml about 	<util:list id="entityManagerLifecycleListeners">.

		Since we can't avoid using entityManagerLifecycleListeners in the integration tests and the way how OpenJPA operates in that case
		(SELECT-before-DELETE/UPDATE) but still want to test production code correctly (i.e. without listeners), this is the way
		how to do it.

		The listener must be removed right AFTER db population and BEFORE calling the "execute()" method.
		The list is not restored upon test completion because the db is already created, thus no harm is made.

	 */
	private void removeEntityLifecycleListener() {
		OpenJPAEntityManagerFactorySPI spiEMFactory = (OpenJPAEntityManagerFactorySPI) OpenJPAPersistence.cast(batchEntityManagerFactory);
		spiEMFactory.removeLifecycleListener(databaseTimestampsEntityListener);
	}
}
