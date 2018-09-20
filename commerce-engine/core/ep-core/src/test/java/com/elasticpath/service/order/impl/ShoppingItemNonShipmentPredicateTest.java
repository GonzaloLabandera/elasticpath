/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.order.impl;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Test cases for {@link ShoppingItemNonShipmentPredicate}.
 */
public class ShoppingItemNonShipmentPredicateTest {

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	@Mock private ProductSkuLookup productSkuLookup;

	private ShoppingItemNonShipmentPredicate predicate;
	
	/**
	 * Setup method to be called before any test methods.
	 */
	@Before
	public void setUp() {
		predicate = new ShoppingItemNonShipmentPredicate(productSkuLookup);
	}
	
	
	/**
	 * Tests whether the predicate accepts non ShoppingItem classes. 
	 */
	@Test (expected = ClassCastException.class)
	public void testEvaluateObjectCCE() {
		predicate.evaluate(new Object());
	}
	
	/**
	 * from description of SUBS-29: Accept items that Shippable type is not Shippable or Downloadable, is a Digital Assest.
	 */
	@Test
	public void testEvaluateTrue() {
		testForSku(false, false, false, false);
		testForSku(true,  false, false, false);
		testForSku(false, true,  false, true);
		testForSku(true,  true,  false, false);
		testForSku(false, false, true,  false);
		testForSku(true,  false, true,  false);
		testForSku(false, true,  true,  false);
		testForSku(true,  true,  true,  false);
	}
	
	private void testForSku(final boolean shippable, final boolean digital, final boolean downloadable, 
			final boolean expectedValue) {
		final ProductSku sku = context.mock(ProductSku.class, "ProductSku " + System.nanoTime());
		final ShoppingItem shoppingItem = context.mock(ShoppingItem.class, "ShoppingItem " + System.nanoTime());
		context.checking(new Expectations() {
			{
				final String guid = new RandomGuidImpl().toString();

				allowing(sku).getGuid(); will(returnValue(guid));
				allowing(sku).isShippable(); will(returnValue(shippable));
				allowing(sku).isDigital(); will(returnValue(digital));
				allowing(sku).isDownloadable(); will(returnValue(downloadable));
				allowing(shoppingItem).getSkuGuid(); will(returnValue(guid));
				allowing(productSkuLookup).findByGuid(guid); will(returnValue(sku));
			}
		});
		assertEquals(predicate.evaluate(shoppingItem), expectedValue);
	}
	
	
}
