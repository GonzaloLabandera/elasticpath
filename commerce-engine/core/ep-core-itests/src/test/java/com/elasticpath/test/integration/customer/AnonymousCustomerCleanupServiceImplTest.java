/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.test.integration.customer;

import static com.elasticpath.test.integration.datapolicy.AbstractDataPolicyTest.CUSTOMER_CONSENT_UNIQUE_CODE;
import static com.elasticpath.test.integration.datapolicy.AbstractDataPolicyTest.DATA_POLICY_DESCRIPTION;
import static com.elasticpath.test.integration.datapolicy.AbstractDataPolicyTest.DATA_POLICY_NAME;
import static com.elasticpath.test.integration.datapolicy.AbstractDataPolicyTest.DATA_POLICY_REFERENCE_KEY;
import static com.elasticpath.test.integration.datapolicy.AbstractDataPolicyTest.getSegments;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.datapolicy.ConsentAction;
import com.elasticpath.domain.datapolicy.CustomerConsent;
import com.elasticpath.domain.datapolicy.DataPolicy;
import com.elasticpath.domain.datapolicy.DataPolicyState;
import com.elasticpath.domain.datapolicy.RetentionType;
import com.elasticpath.domain.datapolicy.impl.CustomerConsentImpl;
import com.elasticpath.domain.datapolicy.impl.DataPolicyImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.customer.AnonymousCustomerCleanupService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.customer.impl.AnonymousCustomerCleanupServiceImpl;
import com.elasticpath.service.datapolicy.CustomerConsentService;
import com.elasticpath.service.datapolicy.DataPolicyService;
import com.elasticpath.service.misc.impl.MockTimeServiceImpl;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.OrderTestPersister;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.persister.testscenarios.AbstractScenario;
import com.elasticpath.test.persister.testscenarios.ProductsScenario;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * Integration tests of {@link AnonymousCustomerCleanupServiceImpl}.
 */
@ContextConfiguration(locations="/integration-mock-time-service.xml", inheritLocations = true)
public class AnonymousCustomerCleanupServiceImplTest extends BasicSpringContextTest {

	private static final String REGISTERED_EMAIL = "john.doe@elasticpath.com";
	private static final int EXPECTED_MAX_HISTORY = 60;

	@Autowired
	private AnonymousCustomerCleanupService anonymousCustomerCleanupService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ShopperService shopperService;

	@Autowired
	private ShoppingCartService shoppingCartService;

	@Autowired
	private WishListService wishListService;

	@Autowired
	private CustomerSessionService customerSessionService;

	@Autowired
	private CartOrderService cartOrderService;

	@Autowired @Qualifier(ContextIdNames.TIME_SERVICE)
	private MockTimeServiceImpl mockTimeService;

	@Autowired
	DataPolicyService dataPolicyService;

	@Autowired
	CustomerConsentService customerConsentService;

	private Date afterRemovalDate;

	private Date removalDate;

	private Date beforeRemovalDate;

	private SimpleStoreScenario storeScenario;
	private ProductsScenario productsScenario;

	/**
	 * Get a reference to TestApplicationContext for use within the test. <br>
	 * Setup scenarios.
	 * @throws Exception on exception
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		afterRemovalDate = new Date();
		removalDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_HISTORY);
		beforeRemovalDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_HISTORY + 1);

		mockTimeService.setCurrentTime(beforeRemovalDate);  // Ensure Products are purchasable!
		Map<Class<? extends AbstractScenario>, AbstractScenario> scenarios =
				getTac().useScenarios((List) Arrays.asList(SimpleStoreScenario.class, ProductsScenario.class));
		storeScenario = (SimpleStoreScenario) scenarios.get(SimpleStoreScenario.class);
		productsScenario = (ProductsScenario) scenarios.get(ProductsScenario.class);
		mockTimeService.setCurrentTime(afterRemovalDate);
	}

	/**
	 * Test that the max history applied to a date removes the anonymous customers, but not the non-anonymous customers.
	 */
	@DirtiesDatabase
	@Test
	public void testCandidateRemovalByDateBoundariesAndAnonymity() {
		Customer anonymousCustomerDatedAfterRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(afterRemovalDate);
		Customer anonymousCustomerDatedWithRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(removalDate);
		Customer anonymousCustomerDatedBeforeRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(beforeRemovalDate);
		Customer registeredCustomerDatedWithRemovalDate = getPersistRegisteredCustomerWithLastModifiedDateOf(beforeRemovalDate);

		int result = anonymousCustomerCleanupService.deleteAnonymousCustomers(removalDate, 1);
		assertEquals("There should have been one customer removed.", 1, result);

		assertDatabaseStillHasCustomer(anonymousCustomerDatedAfterRemovalDate);
		assertDatabaseStillHasCustomer(anonymousCustomerDatedWithRemovalDate);
		assertDatabaseStillHasCustomer(registeredCustomerDatedWithRemovalDate);
		assertDatabaseHasDeletedCustomer(anonymousCustomerDatedBeforeRemovalDate);
	}

	/**
	 * Ensure that the associated referential entities are removed with anonymous customer removal.
	 */
	@DirtiesDatabase
	@Test
	public void ensureAssociatedReferentialEntitiesAreRemovedWithAnonymousCustomerRemoval() {
		Customer customer = getPersistedAnonymousCustomerWithLastModifiedDateOf(beforeRemovalDate);
		CustomerSession customerSession = getTac().getPersistersFactory().getStoreTestPersister().persistCustomerSessionWithAssociatedEntities(customer);
		Shopper shopper = customerSession.getShopper();
		WishList wishList = wishListService.createWishList(shopper);
		ShoppingCart shoppingCart =  shopper.getCurrentShoppingCart();
		String shoppingCartGuid = shoppingCart.getGuid();
		cartOrderService.createOrderIfPossible(shoppingCart);

		int deletedCustomerCount = anonymousCustomerCleanupService.deleteAnonymousCustomers(removalDate, 1);
		assertEquals("There should have been one customer removed.", 1, deletedCustomerCount);

		CustomerSession retrievedCustomerSession = customerSessionService.findByGuid(customerSession.getGuid());
		assertNull("The associated customer session should have been removed", retrievedCustomerSession);

		Shopper retrievedShopper = shopperService.get(shopper.getUidPk());
		assertNull("The associated shopper should have been removed.", retrievedShopper);

		ShoppingCart retrievedShoppingCart = shoppingCartService.findByGuid(shoppingCartGuid);
		assertNull("The associated shopping cart should have been removed.", retrievedShoppingCart);

		CartOrder retrievedCartOrder = cartOrderService.findByShoppingCartGuid(shoppingCartGuid);
		assertNull("The associated cart order should have been removed.", retrievedCartOrder);
		
		WishList retrievedWishList = wishListService.get(wishList.getUidPk());
		assertNull("The associated wish list should have been removed.", retrievedWishList);

		Customer retrievedCustomer = customerService.findByGuid(customer.getGuid());
		assertNull("The candidate customer which is before the removal date should not exist in database.", retrievedCustomer);
	}


	/**
	 * Test removal with customer consents does removal.
	 */
	@DirtiesDatabase
	@Test
	public void testRemovalAnonymousCustomerWithCustomerConsent() {
		DataPolicy dataPolicy = createAndSaveDataPolicy(storeScenario.getStore().getCode());

		Customer anonymousCustomerDatedBeforeRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(beforeRemovalDate);

		CustomerConsent customerConsent =
				createAndSaveCustomerConsent(CUSTOMER_CONSENT_UNIQUE_CODE, anonymousCustomerDatedBeforeRemovalDate, dataPolicy, beforeRemovalDate);

		Date afterRemovalDate = new Date();
		Date removalDate = AnonymousCustomerCleanupServiceImplTest.getAdjustedDate(afterRemovalDate, EXPECTED_MAX_HISTORY);

		int deletedCustomerCount = anonymousCustomerCleanupService.deleteAnonymousCustomers(removalDate, 1);
		assertEquals("There should have been one customer removed.", 1, deletedCustomerCount);

		Customer retrievedCustomer = customerService.findByGuid(customerConsent.getCustomerGuid());
		assertNull("The customer should not exist in database.", retrievedCustomer);
	}

	/**
	 * Test max history applied to a date removes the anonymous customers, but not the non-anonymous customers with customer consents.
	 */
	@DirtiesDatabase
	@Test
	public void testRemovalVariousCustomersWithCustomerConsent() {
		DataPolicy dataPolicy = createAndSaveDataPolicy(storeScenario.getStore().getCode());

		Customer anonymousCustomerDatedAfterRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(afterRemovalDate);
		Customer anonymousCustomerDatedWithRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(removalDate);
		Customer anonymousCustomerDatedBeforeRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(beforeRemovalDate);
		Customer registeredCustomerDatedWithRemovalDate = getPersistRegisteredCustomerWithLastModifiedDateOf(beforeRemovalDate);

		CustomerConsent customerConsentAnonToRemove =
				createAndSaveCustomerConsent(CUSTOMER_CONSENT_UNIQUE_CODE, anonymousCustomerDatedBeforeRemovalDate, dataPolicy, beforeRemovalDate);
		CustomerConsent customerConsentAnonToKeep =
				createAndSaveCustomerConsent(CUSTOMER_CONSENT_UNIQUE_CODE, anonymousCustomerDatedAfterRemovalDate, dataPolicy, beforeRemovalDate);
		CustomerConsent customerConsentAnonToKeep2 =
				createAndSaveCustomerConsent(CUSTOMER_CONSENT_UNIQUE_CODE, anonymousCustomerDatedWithRemovalDate, dataPolicy, beforeRemovalDate);
		CustomerConsent customerConsentRegToKeep =
				createAndSaveCustomerConsent(CUSTOMER_CONSENT_UNIQUE_CODE, registeredCustomerDatedWithRemovalDate, dataPolicy, beforeRemovalDate);

		Date afterRemovalDate = new Date();
		Date removalDate = AnonymousCustomerCleanupServiceImplTest.getAdjustedDate(afterRemovalDate, EXPECTED_MAX_HISTORY);

		int deletedCustomerCount = anonymousCustomerCleanupService.deleteAnonymousCustomers(removalDate, 1);
		assertEquals("There should have been one customer removed.", 1, deletedCustomerCount);

		Customer retrievedCustomer = customerService.findByGuid(customerConsentAnonToRemove.getCustomerGuid());
		assertNull("The customer should not exist in database.", retrievedCustomer);

		List<CustomerConsent> all = customerConsentService.list();
		assertThat(all)
				.containsExactlyInAnyOrder(customerConsentAnonToKeep, customerConsentAnonToKeep2, customerConsentRegToKeep);

		List<CustomerConsent> allHistory = customerConsentService.listHistory();
		assertThat(allHistory)
				.containsExactlyInAnyOrder(customerConsentAnonToKeep, customerConsentAnonToKeep2, customerConsentRegToKeep);
	}

	/**
	 * Test removal ignores anonymous customer with order.
	 */
	@DirtiesDatabase
	@Test
	public void testRemovalIgnoresAnonymousCustomerWithOrder() {
		Customer anonymousCustomerWithOrderBeforeRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(beforeRemovalDate);
		addOrderToCustomer(anonymousCustomerWithOrderBeforeRemovalDate);

		int deletedCustomerCount = anonymousCustomerCleanupService.deleteAnonymousCustomers(removalDate, 1);
		assertEquals("There should be no customers removed.", 0, deletedCustomerCount);

		assertDatabaseStillHasCustomer(anonymousCustomerWithOrderBeforeRemovalDate);
	}

	/**
	 * Test removal restricted to maximum amount.
	 */
	@DirtiesDatabase
	@Test
	public void testRemovalRestrictedToMaximumAmount() {
		int expiredAnonymousCustomerCount = 6;
		List<Long> addedCustomerUids = new ArrayList<>();

		for (int i = 0; i < expiredAnonymousCustomerCount; ++i) {
			Customer anonymousCustomerDatedAfterRemovalDate = getPersistedAnonymousCustomerWithLastModifiedDateOf(beforeRemovalDate);
			addedCustomerUids.add(anonymousCustomerDatedAfterRemovalDate.getUidPk());
		}

		int removalBatchSize = expiredAnonymousCustomerCount - 1;
		int deletedCustomerCount = anonymousCustomerCleanupService.deleteAnonymousCustomers(removalDate, removalBatchSize);
		assertEquals("There should be " + removalBatchSize + " customers removed.", removalBatchSize, deletedCustomerCount);
		List<Customer> retrievedCustomers = customerService.findByUids(addedCustomerUids);
		assertEquals("Number of remaining customers is incorrect.", expiredAnonymousCustomerCount - removalBatchSize, retrievedCustomers.size());
	}

	private Customer getPersistedAnonymousCustomerWithLastModifiedDateOf(final Date lastModifiedDate) {
		return createAndPersistCustomer(true, lastModifiedDate);
	}

	private Customer getPersistRegisteredCustomerWithLastModifiedDateOf(final Date lastModifiedDate) {
		return createAndPersistCustomer(false, lastModifiedDate);
	}

	private Customer createAndPersistCustomer(final boolean anonymous, final Date lastModifiedDate) {
		mockTimeService.setCurrentTime(lastModifiedDate);

		StoreTestPersister storeTestPersister = getTac().getPersistersFactory().getStoreTestPersister();
		Customer customer = storeTestPersister.createDefaultCustomer(storeScenario.getStore());
		customer.setAnonymous(anonymous);
		if (!anonymous) {
			customer.setUserId(REGISTERED_EMAIL);
			customer.setEmail(REGISTERED_EMAIL);
		}

		customer.setLastModifiedDate(lastModifiedDate);
		return customerService.update(customer);
	}

	private void assertDatabaseStillHasCustomer(final Customer customer) {
		Customer retrievedCustomer = customerService.findByGuid(customer.getGuid());
		assertEquals("The customer should still exist in database.", retrievedCustomer.getGuid(), customer.getGuid());
	}

	private void assertDatabaseHasDeletedCustomer(final Customer customer) {
		Customer retrievedCustomer = customerService.findByGuid(customer.getGuid());
		assertNull("The customer should not exist in database.", retrievedCustomer);
	}

	private void addOrderToCustomer(final Customer customer) {
		OrderTestPersister orderTestPersister = getTac().getPersistersFactory().getOrderTestPersister();
		orderTestPersister.createOrderForCustomerWithSkusQuantity(customer, storeScenario.getStore(), 1, productsScenario.getNonShippableProducts()
				.get(0)
				.getDefaultSku());
	}

	/**
	 * create special adjusted date.
	 * @param now now date.
	 * @param adjustment how far to adjust by year.
	 * @return adjusted date.
	 */
	public static Date getAdjustedDate(final Date now, final int adjustment) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.DAY_OF_YEAR, - adjustment);
		return calendar.getTime();
	}

	protected DataPolicy createAndSaveDataPolicy(final String code) {
		final DataPolicy dataPolicy = new DataPolicyImpl();
		dataPolicy.setGuid(Utils.uniqueCode(code));
		dataPolicy.setPolicyName(DATA_POLICY_NAME);
		dataPolicy.setRetentionPeriodInDays(1);
		dataPolicy.setDescription(DATA_POLICY_DESCRIPTION);
		dataPolicy.setEndDate(new Date());
		dataPolicy.setStartDate(new Date());
		dataPolicy.setState(DataPolicyState.ACTIVE);
		dataPolicy.setRetentionType(RetentionType.FROM_CREATION_DATE);
		dataPolicy.setSegments(getSegments());
		dataPolicy.setReferenceKey(DATA_POLICY_REFERENCE_KEY);
		return dataPolicyService.save(dataPolicy);
	}

	private CustomerConsent createAndSaveCustomerConsent(final String customerConsentGuid,
														   final Customer customer,
														   final DataPolicy dataPolicy,
														   final Date consentDate) {
		final CustomerConsent customerConsent = new CustomerConsentImpl();
		customerConsent.setGuid(Utils.uniqueCode(customerConsentGuid));
		customerConsent.setDataPolicy(dataPolicy);
		customerConsent.setAction(ConsentAction.GRANTED);
		customerConsent.setCustomerGuid(customer.getGuid());
		customerConsent.setConsentDate(consentDate);
		return customerConsentService.save(customerConsent);
	}
}
