/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.service.shopper.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.impl.InactiveShoppingCartsRemoverForShopperUpdates;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.cart.AbstractCartIntegrationTestParent;
import com.elasticpath.test.persister.PaymentInstrumentPersister;

public class InactiveShoppingCartsRemoverForShopperUpdatesTest extends AbstractCartIntegrationTestParent {

	@Autowired
	private InactiveShoppingCartsRemoverForShopperUpdates testee;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ShoppingCartService shoppingCartService;

	@Autowired
	private PaymentInstrumentPersister paymentInstrumentPersister;

	@DirtiesDatabase
	@Test
	public void testInvalidateShopperRemovesAllInactiveShoppingCarts() {
		Customer customer = createSavedCustomer();
		CustomerSession session = createCustomerSession(customer);
		ShoppingCart shoppingCart = createShoppingCart(session);
		shoppingCart.deactivateCart();
		shoppingCartService.saveOrUpdate(shoppingCart);

		testee.invalidateShopper(session, session.getShopper());

		ShoppingCart updatedInactiveCart = shoppingCartService.findByGuid(shoppingCart.getGuid());
		assertNull("Inactive cart should have been removed.", updatedInactiveCart);
	}

	@DirtiesDatabase
	@Test
	public void testInvalidateShopperKeepAllActiveShoppingCarts() {
		Customer customer = createSavedCustomer();
		CustomerSession session = createCustomerSession(customer);
		ShoppingCart shoppingCart = createShoppingCart(session);

		testee.invalidateShopper(session, session.getShopper());

		ShoppingCart updatedActiveCart = shoppingCartService.findByGuid(shoppingCart.getGuid());
		assertNotNull("Active cart should NOT have been removed.", updatedActiveCart);
	}

	private Customer createSavedCustomer() {
		final Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		String email = "a@b.com";
		customer.setUserId(email);
		customer.setEmail(email);
		customer.setStoreCode(getScenario().getStore().getCode());
		customer.setAnonymous(false);
		return customerService.add(customer);
	}

	@Override
	protected ShoppingCart createShoppingCart(CustomerSession customerSession) {
		final ShoppingCart shoppingCart = super.createShoppingCart(customerSession);
		final ShoppingCart persistedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);
		paymentInstrumentPersister.persistPaymentInstrument(persistedShoppingCart);
		return persistedShoppingCart;
	}

}
