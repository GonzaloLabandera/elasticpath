/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.actions.FinalizeCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;

/**
 * Test for {@link ClearShoppingCartCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClearShoppingCartCheckoutActionTest {

	@Mock
	private ShoppingCartService shoppingCartService;

	private final ShoppingCart oldShoppingCart = mock(ShoppingCart.class);

	@InjectMocks
	private ClearShoppingCartCheckoutAction fixture;

	private FinalizeCheckoutActionContext checkoutContext;

	/**
	 * Test checkout action on order exchange.
	 */
	@Test
	public void testCheckoutActionOnOrderExchange() {
		shouldHaveCheckoutContextAsOrderExchange();

		fixture.execute(checkoutContext);

		verify(shoppingCartService, never()).saveOrUpdate(oldShoppingCart);
	}

	/**
	 * Test basic checkout action.
	 */
	@Test
	public void testDefaultCheckoutAction() {
		shouldHaveDefaultCheckoutContext();

		fixture.execute(checkoutContext);
	}

	private void shouldHaveCheckoutContextAsOrderExchange() {
		createCheckoutContext(true);
	}

	private void shouldHaveDefaultCheckoutContext() {
		createCheckoutContext(false);
	}

	private void createCheckoutContext(final boolean isOrderExchange) {
		PreCaptureCheckoutActionContext checkoutActionContext = new PreCaptureCheckoutActionContextImpl(oldShoppingCart,
																					null,
																					null, false, isOrderExchange, null, null);
		checkoutContext = new FinalizeCheckoutActionContextImpl(checkoutActionContext);
	}
}
