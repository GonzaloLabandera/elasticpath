/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.integration.cart.AbstractCartIntegrationTestParent;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * An integration test for ShoppingCartService.  We are testing service calls that verify certain properties of a shopping cart.
 */
public class ShoppingCartServiceImplCartVerificationTest extends AbstractCartIntegrationTestParent {

	private static final String NONEXISTENT_GUID = "NONEXISTENT_GUID";
	private static final String NONEXISTENT_STORECODE = "NONEXISTENT_STORECODE";
	@Autowired
	private ShoppingCartService shoppingCartService;
	private final Locale DEFAULT_LOCALE = Locale.US;
	
	@DirtiesDatabase
	@Test
	public void testShoppingCartExists() {
		Shopper shopper = createShopper();
		ShoppingCart shoppingCart = createShoppingCart(shopper);
		ShoppingCart updatedCart = shoppingCartService.saveOrUpdate(shoppingCart);
		assertTrue("The shoppingCart should exist.", shoppingCartService.shoppingCartExists(updatedCart.getGuid()));
	}

	@DirtiesDatabase
	@Test
	public void testShoppingCartExistsForNonExistentCart() {
		assertFalse("The shoppingCart should not exist.", shoppingCartService.shoppingCartExists(NONEXISTENT_GUID));
	}
	
	@DirtiesDatabase
	@Test
	public void testShoppingCartExistsForStore() {
		Store store = ((SimpleStoreScenario)getTac().getScenario(SimpleStoreScenario.class)).getStore();
		Shopper shopper = createShopper();
		ShoppingCart shoppingCart = createShoppingCart(shopper);
		ShoppingCart updatedCart = shoppingCartService.saveOrUpdate(shoppingCart);
		assertTrue("The shoppingCart with store code should exist.", 
				shoppingCartService.shoppingCartExistsForStore(updatedCart.getGuid(), store.getCode()));
	}

	@DirtiesDatabase
	@Test
	public void expectShoppingCartExistsForStoreCodeWithCaseInsensitivity() {
		Store store = ((SimpleStoreScenario)getTac().getScenario(SimpleStoreScenario.class)).getStore();
		Shopper shopper = createShopper();
		ShoppingCart shoppingCart = createShoppingCart(shopper);
		ShoppingCart updatedCart = shoppingCartService.saveOrUpdate(shoppingCart);
		String caseDifferingStoreCode = createCaseDifferingStoreCode(store.getCode());
		assertTrue("The shoppingCart with store code should exist.", 
				shoppingCartService.shoppingCartExistsForStore(updatedCart.getGuid(), caseDifferingStoreCode));
	}
	
	@DirtiesDatabase	
	@Test
	public void testShoppingCartExistsForStoreWithNonExistentShoppingCartGuid() {
		Store store = ((SimpleStoreScenario)getTac().getScenario(SimpleStoreScenario.class)).getStore();
		assertFalse("The shoppingCart with store code should not exist.", 
				shoppingCartService.shoppingCartExistsForStore(NONEXISTENT_GUID, store.getCode()));
	}
	
	@DirtiesDatabase
	@Test
	public void testShoppingCartExistsForStoreWithNonExistentStoreCode() {
		Shopper shopper = createShopper();
		ShoppingCart shoppingCart = createShoppingCart(shopper);
		ShoppingCart updatedCart = shoppingCartService.saveOrUpdate(shoppingCart);
		assertFalse("The shoppingCart with store code should not exist.", 
				shoppingCartService.shoppingCartExistsForStore(updatedCart.getGuid(), NONEXISTENT_STORECODE));
	}
	
	private String createCaseDifferingStoreCode(final String code) {
		String result = code.toLowerCase(DEFAULT_LOCALE);
		if (result.equals(code)) {
			result = code.toUpperCase(DEFAULT_LOCALE);
		}
		return result;
	}

}
