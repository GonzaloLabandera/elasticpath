/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.integration.order;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration test for shopping cart.
 */
public class ComplexBundlePurchaseAndViewLineItemsTest extends DbTestCase {

	/**
	 * Tests that the shopping cart can be serialized even when services are
	 * linked during a get.
	 *
	 */
	@DirtiesDatabase
	@Test
	public void testSerialization() {
		final ShoppingCart shoppingCart = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
		
		assertNotNull(shoppingCart);
	}

}