/*
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.cartorder.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.cartorder.impl.CartOrderImpl;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.cartorder.CartOrderShippingService;
import com.elasticpath.service.rules.CartOrderCouponAutoApplier;

/**
 * New JUnit4 tests for {@code CartOrderPopulationStrategyImplTest}.
 */
public class CartOrderPopulationOnCustomerProfileStrategyImplTest {
	private static final String CUSTOMER_ADDRESS_GUID = "CUSTOMER_ADDRESS_GUID";

	private static final String CART_GUID = "CART_GUID";

	private static final String STORE_CODE = "STORE_CODE";
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private final CartOrderPopulationOnCustomerProfileStrategyImpl strategy = new CartOrderPopulationOnCustomerProfileStrategyImpl();
	private final BeanFactory prototypeBeanFactory = context.mock(BeanFactory.class);

	private final CartOrderShippingService cartOrderShippingService = context.mock(CartOrderShippingService.class);

	private final ShoppingCart shoppingCart = context.mock(ShoppingCart.class);

	private final Shopper shopper = context.mock(Shopper.class);

	private final Customer customer = context.mock(Customer.class);

	private final Store store = context.mock(Store.class);

	private final CartOrderCouponAutoApplier cartOrderCouponAutoApplier = context.mock(CartOrderCouponAutoApplier.class);

	private final CustomerAddress customerAddress = new CustomerAddressImpl();

	/**
	 * Setup.
	 */
	@Before
	public void setUp() {
		strategy.setPrototypeBeanFactory(prototypeBeanFactory);
		strategy.setCartOrderShippingService(cartOrderShippingService);
		strategy.setCartOrderCouponAutoApplier(cartOrderCouponAutoApplier);

		customerAddress.setGuid(CUSTOMER_ADDRESS_GUID);

		context.checking(new Expectations() {
			{
				oneOf(prototypeBeanFactory).getPrototypeBean(ContextIdNames.CART_ORDER, CartOrder.class);
				will(returnValue(new CartOrderImpl()));
				allowing(shoppingCart).getStore();
				will(returnValue(store));
				allowing(store).getCode();
				will(returnValue(STORE_CODE));
				oneOf(shoppingCart).getShopper();
				will(returnValue(shopper));
				oneOf(shopper).getCustomer();
				will(returnValue(customer));
				allowing(shoppingCart).getGuid();
				will(returnValue(CART_GUID));
				allowing(customer).getEmail();
				will(returnValue("EMAIL"));
				oneOf(customer).isAnonymous();
				will(returnValue(true));
			}
		});
	}

	/**
	 * Test happy path to create a cart order with default billing address GUID and credit card GIUD.
	 */
	@Test
	public void testCreateCartOrder() {
		shouldContainBillingAddress(customerAddress);
		shouldContainShippingAddress(customerAddress);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertEquals("Customer address GUIDs should be equal.", CUSTOMER_ADDRESS_GUID, result.getBillingAddressGuid());
	}

	/**
	 * Test create cart order without default billing address.
	 */
	@Test
	public void testCreateCartOrderWithoutDefaultBillingAddress() {
		shouldContainBillingAddress(null);
		shouldContainShippingAddress(customerAddress);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertNull("Customer billing address GUID should be null.", result.getBillingAddressGuid());
	}

	/**
	 * Test create cart order without default shipping address.
	 */
	@Test
	public void testCreateCartOrderWithoutDefaultShippingAddress() {
		shouldContainBillingAddress(customerAddress);
		shouldContainShippingAddress(null);

		CartOrder result = strategy.createCartOrder(shoppingCart);
		assertNull("Customer shipping address GUID should be null.", result.getShippingAddressGuid());
	}

	/**
	 * Test create cart order without default shipping address.
	 */
	@Test
	public void testCreateCartOrderWithDefaultShippingAddress() {
		shouldContainBillingAddress(customerAddress);
		shouldContainShippingAddress(customerAddress);

		strategy.createCartOrder(shoppingCart);

		context.assertIsSatisfied();
	}

	private void shouldContainBillingAddress(final CustomerAddress customerAddress) {
		context.checking(new Expectations() {
			{
				oneOf(customer).getPreferredBillingAddress();
				will(returnValue(customerAddress));
			}
		});
	}

	private void shouldContainShippingAddress(final CustomerAddress customerAddress) {
		context.checking(new Expectations() {
			{
				allowing(shoppingCart).getShopper();
				will(returnValue(shopper));

				allowing(shopper).getStoreCode();
				will(returnValue(STORE_CODE));

				oneOf(customer).getPreferredShippingAddress();
				will(returnValue(customerAddress));

				if (customerAddress == null) {
					never(cartOrderShippingService).updateCartOrderShippingAddress(with(any(String.class)),
							with(any(ShoppingCart.class)),
							with(any(CartOrder.class))
					);
				} else {
					oneOf(cartOrderShippingService).updateCartOrderShippingAddress(with(CUSTOMER_ADDRESS_GUID),
							with(any(ShoppingCart.class)),
							with(any(CartOrder.class)));
				}
			}
		});
	}

}
