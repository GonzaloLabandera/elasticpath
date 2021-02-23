/*
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.cartorder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.cartorder.impl.CartOrderImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.cartorder.CartOrderShippingService;
import com.elasticpath.service.rules.CartOrderCouponAutoApplier;

/**
 * New JUnit4 tests for {@code CartOrderPopulationStrategyImplTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartOrderPopulationOnCustomerProfileStrategyImplTest {
	private static final String CUSTOMER_ADDRESS_GUID = "CUSTOMER_ADDRESS_GUID";

	private static final String CART_GUID = "CART_GUID";

	@Mock
	private BeanFactory prototypeBeanFactory;

	@Mock
	private CartOrderShippingService cartOrderShippingService;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Shopper shopper;

	@Mock
	private Customer user;

	@Mock
	private Customer account;

	@InjectMocks
	private CartOrderPopulationOnCustomerProfileStrategyImpl strategy;

	private final CustomerAddress customerAddress = new CustomerAddressImpl();

	/**
	 * Setup.
	 */
	@Before
	public void setUp() {
		customerAddress.setGuid(CUSTOMER_ADDRESS_GUID);
		final CartOrderCouponAutoApplier cartOrderCouponAutoApplier = mock(CartOrderCouponAutoApplier.class);

		when(prototypeBeanFactory.getPrototypeBean(ContextIdNames.CART_ORDER, CartOrder.class)).thenReturn(new CartOrderImpl());
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shopper.getCustomer()).thenReturn(user);
		when(shoppingCart.getGuid()).thenReturn(CART_GUID);
		when(user.isAnonymous()).thenReturn(true);
		strategy.setCartOrderCouponAutoApplier(cartOrderCouponAutoApplier);
	}

	/**
	 * Test happy path to create a cart order with default billing address GUID and credit card GIUD.
	 */
	@Test
	public void testCreateCartOrder() {
		shouldContainBillingAddress(customerAddress, user);
		shouldContainShippingAddress(customerAddress, user);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertEquals("Customer address GUIDs should be equal.", CUSTOMER_ADDRESS_GUID, result.getBillingAddressGuid());
		assertEquals("Customer cart GUIDs should be equal.", CART_GUID, result.getShoppingCartGuid());
	}

	/**
	 * Test happy path to create a cart order with default billing address GUID and shipping address GUID.
	 */
	@Test
	public void testCreateCartOrderForAccount() {
		shouldContainBillingAddress(customerAddress, account);
		shouldContainShippingAddress(customerAddress, account);
		when(shopper.getAccount()).thenReturn(account);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertEquals("Customer billing address GUIDs should be equal.", CUSTOMER_ADDRESS_GUID, result.getBillingAddressGuid());
		verify(cartOrderShippingService).updateCartOrderShippingAddress(customerAddress.getGuid(), shoppingCart, result);
	}

	/**
	 * Test create cart order without default billing address.
	 */
	@Test
	public void testCreateCartOrderWithoutDefaultBillingAddress() {
		shouldContainBillingAddress(null, user);
		shouldContainShippingAddress(customerAddress, user);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertNull("Customer billing address GUID should be null.", result.getBillingAddressGuid());
	}

	/**
	 * Test create cart order without default shipping address.
	 */
	@Test
	public void testCreateCartOrderWithoutDefaultShippingAddress() {
		shouldContainBillingAddress(customerAddress, user);
		shouldContainShippingAddress(null, user);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertNull("Customer shipping address GUID should be null.", result.getShippingAddressGuid());
		verify(cartOrderShippingService, never()).updateCartOrderShippingAddress(anyString(), any(ShoppingCart.class), any(CartOrder.class));
	}

	/**
	 * Test create cart order without default shipping address.
	 */
	@Test
	public void testCreateCartOrderWithDefaultShippingAddress() {
		shouldContainBillingAddress(customerAddress, user);
		shouldContainShippingAddress(customerAddress, user);

		CartOrder result = strategy.createCartOrder(shoppingCart);

		verify(cartOrderShippingService).updateCartOrderShippingAddress(customerAddress.getGuid(), shoppingCart, result);
	}

	private void shouldContainBillingAddress(final CustomerAddress customerAddress, final Customer customer) {
		when(customer.getPreferredBillingAddress()).thenReturn(customerAddress);
	}

	private void shouldContainShippingAddress(final CustomerAddress customerAddress, final Customer customer) {
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(customer.getPreferredShippingAddress()).thenReturn(customerAddress);
	}

}
