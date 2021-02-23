/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.ReversibleCheckoutAction;

/**
 * Tests for the {@code CheckoutServiceImpl} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckoutServiceImplTest {

	@Mock
	private CustomerSession customerSession;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private ShoppingCartTaxSnapshot pricingSnapshot;

	@Mock(name = "action1")
	private ReversibleCheckoutAction action1;

	@Mock(name = "action2")
	private ReversibleCheckoutAction action2;

	@Mock(name = "action3")
	private ReversibleCheckoutAction action3;

	/**
	 * Test that when checkout fails due to an exception thrown during processing of an action,
	 * all previous actions are rolled back.
	 */
	@Test
	public void testCheckoutRollbackMechanism() {

		final CheckoutResults checkoutResults = mock(CheckoutResults.class);
		doThrow(new EpSystemException("testing exception handling.")).when(action2).execute(any(PreCaptureCheckoutActionContext.class));

		CheckoutServiceImpl service = new CheckoutServiceImpl();
		List<ReversibleCheckoutAction> actionList = new ArrayList<>();
		actionList.add(action1);
		actionList.add(action2);
		actionList.add(action3);
		service.setReversibleActionList(actionList);

		try {
			service.checkoutInternal(shoppingCart, pricingSnapshot, customerSession, true, false, null, checkoutResults);
		} catch (EpSystemException ex) {
			assertNotNull("The EpSystemException should be rethrown after the payments have been rolled back.", ex);
		}

		InOrder inOrder = Mockito.inOrder(action1, action2);
		inOrder.verify(action1).execute(any(PreCaptureCheckoutActionContext.class));
		inOrder.verify(action2).execute(any(PreCaptureCheckoutActionContext.class));
		inOrder.verify(action2).rollback(any(PreCaptureCheckoutActionContext.class));
		inOrder.verify(action1).rollback(any(PreCaptureCheckoutActionContext.class));
		inOrder.verifyNoMoreInteractions();
		verifyZeroInteractions(action3);
		verify(checkoutResults).setOrder(null);
	}

	/**
	 * Test that when checkout fails and then rollback also fails,
	 * all previous actions are rolled back.
	 */
	@Test
	public void testCheckoutRollbackMechanismWithRollbackException() {

		final CheckoutResults checkoutResults = mock(CheckoutResults.class);
		doThrow(new EpSystemException("testing exception handling.")).when(action2).execute(any(PreCaptureCheckoutActionContext.class));
		doThrow(new EpSystemException("testing exception handling.")).when(action2).rollback(any(PreCaptureCheckoutActionContext.class));

		CheckoutServiceImpl service = new CheckoutServiceImpl();
		List<ReversibleCheckoutAction> actionList = new ArrayList<>();
		actionList.add(action1);
		actionList.add(action2);
		service.setReversibleActionList(actionList);

		try {
			service.checkoutInternal(shoppingCart, pricingSnapshot, customerSession, true, false, null, checkoutResults);
		} catch (EpSystemException ex) {
			assertNotNull("The EpSystemException should be rethrown after the payments have been rolled back.", ex);
		}

		InOrder inOrder = Mockito.inOrder(action1, action2);
		inOrder.verify(action1).execute(any(PreCaptureCheckoutActionContext.class));
		inOrder.verify(action2).execute(any(PreCaptureCheckoutActionContext.class));
		inOrder.verify(action2).rollback(any(PreCaptureCheckoutActionContext.class));
		inOrder.verify(action1).rollback(any(PreCaptureCheckoutActionContext.class));
		inOrder.verifyNoMoreInteractions();
		verify(checkoutResults).setOrder(null);
	}

	/**
	 * Test that the expected values are populated in the checkout action context object.
	 */
	@Test
	public void verifyCheckoutActionContextPopulatedWithAllExpectedParameters() {
		final boolean isOrderExchange = true;
		final boolean awaitExchangeCompletion = true;
		final OrderReturn exchange = mock(OrderReturn.class);

		final PreCaptureCheckoutActionContext actionContext = new CheckoutServiceImpl().createActionContext(
				shoppingCart,
				null, customerSession,
				isOrderExchange,
				awaitExchangeCompletion,
				exchange);
	
		assertEquals("Unexpected shopping cart", shoppingCart, actionContext.getShoppingCart());
		assertEquals("Unexpected customer session", customerSession, actionContext.getCustomerSession());
		assertEquals("Unexpected isOrderExchange", isOrderExchange, actionContext.isOrderExchange());
		assertEquals("Unexpected awaitExchangeCompletion", awaitExchangeCompletion, actionContext.isAwaitExchangeCompletion());
		assertEquals("Unexpected exchange", exchange, actionContext.getExchange());
	}

}
