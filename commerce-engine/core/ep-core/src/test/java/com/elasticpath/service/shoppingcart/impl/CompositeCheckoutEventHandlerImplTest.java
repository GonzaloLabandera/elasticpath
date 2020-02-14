/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.CheckoutEventHandler;

/**
 * Test class for {@link CompositeCheckoutEventHandlerImplTest}.
 */
public class CompositeCheckoutEventHandlerImplTest {

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private ShoppingCart shoppingCart;
	private Order order;

	private CompositeCheckoutEventHandlerImpl compositeCheckoutEventHandler;

	/**
	 * Test setup.
	 *
	 * @throws Exception on failure
	 */
	@Before
	public void setUp() throws Exception {
		order = context.mock(Order.class);
		shoppingCart = context.mock(ShoppingCart.class);

		compositeCheckoutEventHandler = new CompositeCheckoutEventHandlerImpl();
	}

	/**
	 * Test teardown.
	 */
	@After
	public void tearDown() {
		compositeCheckoutEventHandler = null;
	}

	/**
	 * Test that CheckoutEventHandler getters and setters are immutable.
	 */
	@Test
	public void testGetAndSetCheckoutEventHandlersIsImmutable() {
		final List<CheckoutEventHandler> delegatedHandlers = new ArrayList<>();
		delegatedHandlers.add(context.mock(CheckoutEventHandler.class));

		compositeCheckoutEventHandler.setCheckoutEventHandlers(delegatedHandlers);

		delegatedHandlers.clear();
		assertEquals("Clearing the original input list should not affect the stored list", 1,
				compositeCheckoutEventHandler.getCheckoutEventHandlers().size());

		compositeCheckoutEventHandler.getCheckoutEventHandlers().clear();
		assertEquals("Clearing the returned list should not affect the stored list", 1,
				compositeCheckoutEventHandler.getCheckoutEventHandlers().size());
	}

	/**
	 * Test empty list of CheckoutEventHandlers.
	 */
	@Test
	public void testAllOperationsPerformNoOpForUnsetAndEmptyListOfCheckoutEventHandlers() {
		compositeCheckoutEventHandler.preCheckout(shoppingCart);
		compositeCheckoutEventHandler.preCheckoutOrderPersist(shoppingCart, order);
		compositeCheckoutEventHandler.postCheckout(shoppingCart, order);

		compositeCheckoutEventHandler.setCheckoutEventHandlers(Collections.emptyList());

		compositeCheckoutEventHandler.preCheckout(shoppingCart);
		compositeCheckoutEventHandler.preCheckoutOrderPersist(shoppingCart, order);
		compositeCheckoutEventHandler.postCheckout(shoppingCart, order);
	}

	/**
	 * Test preCheckout delegates.
	 */
	@Test
	public void testPreCheckoutDelegates() {
		final List<CheckoutEventHandler> delegatedHandlers = getMockedCheckoutEventHandlerList();

		context.checking(new Expectations() {

			{
				for (final CheckoutEventHandler checkoutEventHandler : delegatedHandlers) {
					oneOf(checkoutEventHandler).preCheckout(shoppingCart);
				}
			}

		});

		compositeCheckoutEventHandler.setCheckoutEventHandlers(delegatedHandlers);
		compositeCheckoutEventHandler.preCheckout(shoppingCart);
	}

	/**
	 * Test preCheckoutOrderPersist delegates.
	 */
	@Test
	public void testPreCheckoutOrderPersistDelegates() {
		final List<CheckoutEventHandler> delegatedHandlers = getMockedCheckoutEventHandlerList();

		context.checking(new Expectations() {

			{
				for (final CheckoutEventHandler checkoutEventHandler : delegatedHandlers) {
					oneOf(checkoutEventHandler).preCheckoutOrderPersist(shoppingCart, order);
				}
			}

		});

		compositeCheckoutEventHandler.setCheckoutEventHandlers(delegatedHandlers);
		compositeCheckoutEventHandler.preCheckoutOrderPersist(shoppingCart, order);
	}

	/**
	 * Test postCheckout delegates.
	 */
	@Test
	public void testPostCheckoutDelegates() {
		final List<CheckoutEventHandler> delegatedHandlers = getMockedCheckoutEventHandlerList();

		context.checking(new Expectations() {

			{
				for (final CheckoutEventHandler checkoutEventHandler : delegatedHandlers) {
					oneOf(checkoutEventHandler).postCheckout(shoppingCart, order);
				}
			}

		});

		compositeCheckoutEventHandler.setCheckoutEventHandlers(delegatedHandlers);
		compositeCheckoutEventHandler.postCheckout(shoppingCart, order);
	}

	private List<CheckoutEventHandler> getMockedCheckoutEventHandlerList() {
		final CheckoutEventHandler checkoutEventHandler1 = context.mock(CheckoutEventHandler.class,
				"checkoutEventHandler1");
		final CheckoutEventHandler checkoutEventHandler2 = context.mock(CheckoutEventHandler.class,
				"checkoutEventHandler2");

		final List<CheckoutEventHandler> mockedHandlers = new ArrayList<>();
		mockedHandlers.add(checkoutEventHandler1);
		mockedHandlers.add(checkoutEventHandler2);

		return mockedHandlers;
	}
}