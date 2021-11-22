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
		Shopper registeredShopper = createShopper();
		ShoppingCart registeredShoppingCart = createShoppingCart(registeredShopper);
		registeredShopper.setCurrentShoppingCart(registeredShoppingCart);
		shoppingCartService.saveOrUpdate(registeredShoppingCart);

		Shopper anonymousShopper = createShopper();
		ShoppingCart anonymousShoppingCart = createShoppingCart(anonymousShopper);
		anonymousShopper.setCurrentShoppingCart(anonymousShoppingCart);
		shoppingCartService.saveOrUpdate(anonymousShoppingCart);

		shoppingCartMergerForShopperUpdates.invalidateShopper(anonymousShopper, registeredShopper);

		// assert that the shopper is using the registered account shopping cart and not the anonymous one
		ShoppingCart actualShoppingCart = shoppingCartService.findOrCreateDefaultCartByShopper(registeredShopper);
		assertEquals("The shopping cart used going forward is the one that was associated with the registered account, not the anonymous cart.",
				registeredShoppingCart.getGuid(),
				actualShoppingCart.getGuid());
	}

	/**
	 * The imported customers do not have created carts, thus before merging, the cart must be persisted.
	 * The test is validating merging anonymous cart to the imported registered account.
	 */
	@DirtiesDatabase
	@Test
	public void testMergingAnonymousAndImportedRegisteredUserCarts() {
		Shopper registeredShopper = createShopper();
		ShoppingCart registeredShoppingCart = createShoppingCart(registeredShopper);
		registeredShopper.setCurrentShoppingCart(registeredShoppingCart);
		registeredShopper.setStoreCode(registeredShoppingCart.getStore().getCode());

		Shopper anonymousShopper = createShopper();
		ShoppingCart anonymousShoppingCart = createShoppingCart(anonymousShopper);
		anonymousShopper.setCurrentShoppingCart(anonymousShoppingCart);
		shoppingCartService.saveOrUpdate(anonymousShoppingCart);

		shoppingCartMergerForShopperUpdates.invalidateShopper(anonymousShopper, registeredShopper);

		// assert that the shopper is using the registered account shopping cart and not the anonymous one
		ShoppingCart actualShoppingCart = shoppingCartService.findOrCreateDefaultCartByShopper(registeredShopper);
		assertEquals("The shopping cart used going forward is the one that was associated with the registered account, not the anonymous cart.",
				registeredShopper.getCurrentShoppingCart().getGuid(),
				actualShoppingCart.getGuid());
	}
}
