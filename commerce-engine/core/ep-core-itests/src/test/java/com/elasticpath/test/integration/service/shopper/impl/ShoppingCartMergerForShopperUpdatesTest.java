/**
 * Copyright (c) Elastic Path Software Inc., 2012
 */
package com.elasticpath.test.integration.service.shopper.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shopper.impl.ShoppingCartMergerForShopperUpdates;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.cart.AbstractCartIntegrationTestParent;

/**
 * Test functionality regarding {@link ShoppingCartMergerForShopperUpdates}.
 */
public class ShoppingCartMergerForShopperUpdatesTest extends AbstractCartIntegrationTestParent {

	@Autowired
	@Qualifier("shoppingCartMergerForShopperUpdates")
	private ShoppingCartMergerForShopperUpdates shoppingCartMergerForShopperUpdates;

	private ShoppingCartService shoppingCartService;

	/**
	 * Setup the tests.
	 *
	 * @throws Exception on error.
	 */
	@Before
	public void setUp() throws Exception {
		shoppingCartService = getBeanFactory().getSingletonBean(ContextIdNames.SHOPPING_CART_SERVICE, ShoppingCartService.class);
	}

	/**
	 * Test that the correct shopping cart remains after invalidating a cart.<br>
	 * In other words, test when an anonymous shopper logs in to a registered account, that the cart used going forward is the one associated with
	 * the registered account and not the one created for the anonymous shopper.<br>
	 * Also tests that the anonymous shopping cart is removed in this case.
	 */
	@DirtiesDatabase
	@Test
	public void testMergingCartsKeepsTheCartAssociatedWithTheCustomerAccountNotTheAnonymouslyCreatedCart() {
		CustomerSession registeredCustomerSession = createCustomerSession();
		ShoppingCart registeredShoppingCart = createShoppingCart(registeredCustomerSession);
		shoppingCartService.saveOrUpdate(registeredShoppingCart);

		CustomerSession anonymousCustomerSession = createCustomerSession();
		Shopper anonymousShopper = anonymousCustomerSession.getShopper();
		ShoppingCart anonymousShoppingCart = createShoppingCart(anonymousCustomerSession);
		anonymousShopper.setCurrentShoppingCart(anonymousShoppingCart);

		shoppingCartService.saveOrUpdate(anonymousShoppingCart);

		shoppingCartMergerForShopperUpdates.invalidateShopper(registeredCustomerSession, anonymousShopper);

		// assert that the shopper is using the registered account shopping cart and not the anonymous one
		ShoppingCart actualShoppingCart = shoppingCartService.findOrCreateDefaultCartByCustomerSession(registeredCustomerSession);
		assertEquals("The shopping cart used going forward is the one that was associated with the registered account, not the anonymous cart.",
				registeredShoppingCart.getGuid(),
				actualShoppingCart.getGuid());
	}
}
