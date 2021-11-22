/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.integration.shopper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.shopper.Shopper;
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

	private Customer createNewAccountCustomer() {
		Customer account = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		account.setCustomerType(CustomerType.ACCOUNT);
		return account;
	}

}
