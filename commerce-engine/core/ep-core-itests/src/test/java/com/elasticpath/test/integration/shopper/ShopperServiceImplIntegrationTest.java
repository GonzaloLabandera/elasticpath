/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.integration.shopper;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.attribute.AttributeService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.cart.AbstractCartIntegrationTestParent;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Integration tests for ShopperServiceImpl.
 */
public class ShopperServiceImplIntegrationTest extends AbstractCartIntegrationTestParent {

	private static final String GUID = "GUID";
	@Autowired
	private CustomerService customerService;

	@Autowired
	private ShopperService shopperService;

	/**
	 * Tests trying to find by Customer and Store.
	 *
	 * 1st time should create it.
	 *
	 * 2nd time to retrieve it.
	 *
	 * Checks that the guids match.
	 */
	@DirtiesDatabase
	@Test
	public void testFindByCustomerAndStore() {
		// Create a persistent customer and store.
		SimpleStoreScenario scenario = (SimpleStoreScenario) getTac().getScenario(SimpleStoreScenario.class);
		final String storeCode = scenario.getStore().getCode();

		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);
		customer.setStoreCode(storeCode);
		customer = customerService.add(customer);

		// Test
		final Shopper newShopper = shopperService.findOrCreateShopper(customer, storeCode);
		final Shopper persistedShopper = shopperService.findOrCreateShopper(customer, storeCode);

		// Verify
		assertEquals(String.format("newShopper GUID (%s) does not match persistedShopper GUID (%s)", newShopper.getGuid(), persistedShopper.getGuid()),
				newShopper.getGuid(), persistedShopper.getGuid());
	}

	@DirtiesDatabase
	@Test
	public void testFindByCustomerAndStoreWithAccount() {
		// Create a persistent customer and store.
		SimpleStoreScenario scenario = (SimpleStoreScenario) getTac().getScenario(SimpleStoreScenario.class);
		final String storeCode = scenario.getStore().getCode();

		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setCustomerType(CustomerType.SINGLE_SESSION_USER);
		customer.setStoreCode(storeCode);
		customer = customerService.add(customer);

		Customer account = createNewAccountCustomer();
		account.setSharedId("testFindByCustomerAndStoreAccount@ShopperServiceImplIntegrationTest.com");
		account.setBusinessName("TEST_ACCOUNT");
		account = customerService.add(account);

		// Test
		final Shopper newShopper = shopperService.findOrCreateShopper(customer, account, storeCode);
		final Shopper persistedShopper = shopperService.findOrCreateShopper(customer, account, storeCode);

		// Verify
		assertEquals(String.format("newShopper GUID (%s) does not match persistedShopper GUID (%s)", newShopper.getGuid(), persistedShopper.getGuid()),
				newShopper.getGuid(), persistedShopper.getGuid());
	}

	@DirtiesDatabase
	@Test
	public void testFindByCustomerGuid() {
		// Create a persistent customer and store.
		SimpleStoreScenario scenario = (SimpleStoreScenario) getTac().getScenario(SimpleStoreScenario.class);
		final String storeCode = scenario.getStore().getCode();

		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setStoreCode(storeCode);
		customer.setGuid(GUID);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer = customerService.add(customer);

		Customer account = createNewAccountCustomer();
		account.setSharedId("accountForTestWrongShopper@ShopperServiceImplIntegrationTest.com");
		account.setBusinessName("TEST_ACCOUNT");
		account = customerService.add(account);

		Shopper wrongShopper = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		wrongShopper.setCustomer(customer);
		wrongShopper.setAccount(account);
		shopperService.save(wrongShopper);

		Shopper expectedShopper = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		expectedShopper.setCustomer(customer);
		shopperService.save(expectedShopper);

		// Test
		final Shopper actualShopper = shopperService.findByCustomerGuid(GUID);

		// Verify
		assertEquals("Expected and actual shoppers must be equal", expectedShopper, actualShopper);
	}

	@DirtiesDatabase
	@Test
	public void testFindByCustomerUserIdAndStore() {
		// Create a persistent customer and store.
		SimpleStoreScenario scenario = (SimpleStoreScenario) getTac().getScenario(SimpleStoreScenario.class);
		final String storeCode = scenario.getStore().getCode();

		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId("testFindByCustomerAndStore@ShopperServiceImplIntegrationTest.com");
		customer.setStoreCode(storeCode);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer = customerService.add(customer);

		Customer account = createNewAccountCustomer();
		account.setSharedId("accountForTestWrongShopper2@ShopperServiceImplIntegrationTest.com");
		account.setBusinessName("TEST_ACCOUNT");
		account = customerService.add(account);

		Shopper wrongShopper = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		wrongShopper.setCustomer(customer);
		wrongShopper.setAccount(account);
		shopperService.save(wrongShopper);

		Shopper expectedShopper = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		expectedShopper.setCustomer(customer);
		expectedShopper.setStoreCode(storeCode);
		shopperService.save(expectedShopper);

		// Test
		final Shopper actualShopper = shopperService.findByCustomerSharedIdAndStoreCode(customer.getSharedId(), storeCode);

		// Verify
		assertEquals("Expected and actual shoppers must be equal", expectedShopper, actualShopper);
	}

	@DirtiesDatabase
	@Test
	public void testFindByCustomerUserIdAndAccountIdAndStore() {
		// Create a persistent customer and store.
		SimpleStoreScenario scenario = (SimpleStoreScenario) getTac().getScenario(SimpleStoreScenario.class);
		final String storeCode = scenario.getStore().getCode();

		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId("testFindByCustomerIdAccountIdStore@ShopperServiceImplIntegrationTest.com");
		customer.setStoreCode(storeCode);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer = customerService.add(customer);

		Customer account = createNewAccountCustomer();
		account.setSharedId("testAccountFindByUserIdAccountId@ShopperServiceImplIntegrationTest.com");
		account.setBusinessName("TEST_ACCOUNT");
		account = customerService.add(account);

		Shopper expectedShopper = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		expectedShopper.setCustomer(customer);
		expectedShopper.setAccount(account);
		expectedShopper.setStoreCode(storeCode);
		shopperService.save(expectedShopper);

		// Test
		final Shopper actualShopper = shopperService.findByCustomerSharedIdAndAccountSharedIdAndStore(
				customer.getSharedId(), account.getSharedId(), storeCode);

		// Verify
		assertEquals("Expected and actual shoppers must be equal", expectedShopper, actualShopper);
	}

	@DirtiesDatabase
	@Test
	public void testFindByCustomerGuidAndAccountIdAndStore() {
		SimpleStoreScenario scenario = (SimpleStoreScenario) getTac().getScenario(SimpleStoreScenario.class);
		final String storeCode = scenario.getStore().getCode();

		Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId("testFindByCustomerGuidAndAccountIdAndStore@ShopperServiceImplIntegrationTest.com");
		customer.setStoreCode(storeCode);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer = customerService.add(customer);

		Customer account = createNewAccountCustomer();
		account.setSharedId("testAccount@ShopperServiceImplIntegrationTest.com");
		account.setBusinessName("TEST_ACCOUNT");
		account = customerService.add(account);

		Shopper expectedShopper = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPER, Shopper.class);
		expectedShopper.setCustomer(customer);
		expectedShopper.setAccount(account);
		expectedShopper.setStoreCode(storeCode);
		shopperService.save(expectedShopper);

		// Test
		final Shopper actualShopper = shopperService.findByCustomerGuidAndAccountSharedIdAndStore(customer.getGuid(), account.getSharedId(),
				storeCode);

		// Verify
		assertEquals("Expected and actual shoppers must be equal", expectedShopper, actualShopper);
	}

	private Customer createNewAccountCustomer() {
		Customer account = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		account.setCustomerType(CustomerType.ACCOUNT);
		return account;
	}

}
