/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.shoppingcart.actions.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.shoppingcart.OrderFactory;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContextImpl;

/**
 * Test class for {@link com.elasticpath.service.shoppingcart.actions.impl.CreateNewOrderCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNewOrderCheckoutActionTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	private CreateNewOrderCheckoutAction checkoutAction;

	@Mock
	private OrderFactory orderFactory;
	@Mock
	private Customer customer;
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private CustomerSession customerSession;
	@Mock
	private ShoppingCartTaxSnapshot taxSnapshot;
	@Mock
	private Shopper shopper;
	@Mock
	private Order order;

	@Before
	public void setUp() {
		checkoutAction = new CreateNewOrderCheckoutAction();
		checkoutAction.setOrderFactory(orderFactory);

		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shopper.getCustomer()).thenReturn(customer);
	}

	@Test
	public void verifyExecuteDelegatesToOrderFactory() {
		final boolean orderExchange = false;
		final boolean awaitExchangeCompletion = false;
		final OrderReturn exchange = null;

		when(orderFactory.createAndPersistNewEmptyOrder(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(order);
		when(orderFactory.fillInNewOrderFromShoppingCart(any(), any(), any(), any(), any())).thenReturn(order);

		final PreCaptureCheckoutActionContext checkoutActionContext = new PreCaptureCheckoutActionContextImpl(
				shoppingCart,
				taxSnapshot,
				customerSession,
				orderExchange,
				awaitExchangeCompletion,
				exchange,
				null);

		checkoutAction.execute(checkoutActionContext);

		verify(orderFactory).createAndPersistNewEmptyOrder(
				customer,
				customerSession,
				shoppingCart,
				orderExchange,
				awaitExchangeCompletion);

		verify(orderFactory).fillInNewOrderFromShoppingCart(
				order,
				customer,
				customerSession,
				shoppingCart,
				taxSnapshot);

		assertEquals("Unexpected Order populated on CheckoutActionContext", order, checkoutActionContext.getOrder());
	}

	@Test
	public void verifyExecuteDelegatesToOrderFactoryForExchanges() {
		final boolean orderExchange = true;
		final boolean awaitExchangeCompletion = true;
		final OrderReturn exchange = mock(OrderReturn.class);

		when(orderFactory.createAndPersistNewEmptyOrder(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(order);
		when(orderFactory.fillInNewExchangeOrderFromShoppingCart(any(), any(), any(), any(), any(), anyBoolean(), any())).thenReturn(order);

		final PreCaptureCheckoutActionContext checkoutActionContext = new PreCaptureCheckoutActionContextImpl(
				shoppingCart,
				taxSnapshot,
				customerSession,
				orderExchange,
				awaitExchangeCompletion,
				exchange,
				null);

		checkoutAction.execute(checkoutActionContext);

		verify(orderFactory).createAndPersistNewEmptyOrder(
				customer,
				customerSession,
				shoppingCart,
				orderExchange,
				awaitExchangeCompletion);

		verify(orderFactory).fillInNewExchangeOrderFromShoppingCart(
				order,
				customer,
				customerSession,
				shoppingCart,
				taxSnapshot,
				awaitExchangeCompletion,
				exchange);

		assertEquals("Unexpected Order populated on CheckoutActionContext", order, checkoutActionContext.getOrder());
	}

	@Test
	public void verifyOrderIsStillPopulatedOnContextIfPopulateOrderFailsOnNonExchangeOrder() {
		// Given an order is initially created by the checkout action
		when(orderFactory.createAndPersistNewEmptyOrder(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(order);

		// And an exception is thrown when the order is populated by populateOrder() using the OrderFactory
		final EpDomainException expectedException = new EpDomainException("Throwing Deliberate Error");
		doThrow(expectedException).when(orderFactory)
				.fillInNewOrderFromShoppingCart(any(), any(), any(), any(), any());

		final PreCaptureCheckoutActionContext checkoutActionContext = new PreCaptureCheckoutActionContextImpl(
				shoppingCart,
				taxSnapshot,
				customerSession,
				false,
				false,
				null,
				null);

		// When the checkout action is executed, then the expected exception should be thrown
		assertThatThrownBy(() -> checkoutAction.execute(checkoutActionContext))
				.isSameAs(expectedException);

		// And the order is still set on the context
		assertThat(checkoutActionContext.getOrder()).isSameAs(order);
	}
}