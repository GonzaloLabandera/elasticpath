/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.cartorder.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.cartorder.CartOrderShippingInformationSanitizer;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.shipping.ShippingServiceLevelService;

/**
 * New JUnit4 tests for {@code CartOrderShippingServiceImpl}.
 */
public class CartOrderShippingServiceImplTest {

	private static final String CART_ORDER_SHOULD_BE_UPDATED = "Cart Order should be updated.";

	private static final long SHIPPING_SERVICE_LEVEL_UID = Long.MIN_VALUE;

	private static final String SHIPPING_ADDRESS_GUID = "shippingAddressGuid";

	private static final String BILLING_ADDRESS_GUID = "billingAddressGuid";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private CartOrderShippingServiceImpl service;

	private ShippingServiceLevelService shippingServiceLevelService;

	private CartOrderShippingInformationSanitizer cartOrderShippingInformationSanitizer;

	private CustomerAddressDao customerAddressDao;

	private CartOrder cartOrder;

	private Address shippingAddress;

	private ShippingServiceLevel shippingServiceLevel;

	private List<ShippingServiceLevel> shippingServiceLevels;

	private ShoppingCart shoppingCart;

	private Address billingAddress;

	private Store store;

	private static final String EXISTING_SHIPPING_ADDRESS_GUID = "EXISTING_SHIPPING_ADDRESS_GUID";

	private static final String UPDATED_SHIPPING_ADDRESS_GUID = "UPDATED_SHIPPING_ADDRESS_GUID";

	private static final String EXISTING_SHIPPING_SERVICE_LEVEL_GUID = "EXISTING_SHIPPING_SERVICE_LEVEL_GUID";

	private static final String NEW_SHIPPING_SERVICE_LEVEL_GUID = "NEW_SHIPPING_SERVICE_LEVEL_GUID";

	private static final String STORE_CODE = "STORE_CODE";

	/**
	 * Mock required services and objects.
	 */
	@Before
	public void mockRequiredServicesAndObjects() {
		service = new CartOrderShippingServiceImpl();
		customerAddressDao = context.mock(CustomerAddressDao.class);
		shippingServiceLevelService = context.mock(ShippingServiceLevelService.class);
		cartOrderShippingInformationSanitizer = context.mock(CartOrderShippingInformationSanitizer.class);
		shoppingCart = context.mock(ShoppingCart.class);
		billingAddress = context.mock(Address.class, "billingAddress");
		shippingAddress = context.mock(Address.class, "shippingAddress");
		store = context.mock(Store.class);
		service.setCustomerAddressDao(customerAddressDao);
		service.setShippingServiceLevelService(shippingServiceLevelService);
		service.setCartOrderShippingInformationSanitizer(cartOrderShippingInformationSanitizer);

		cartOrder = context.mock(CartOrder.class);
		shippingAddress = context.mock(Address.class);
		shippingServiceLevel = context.mock(ShippingServiceLevel.class);
		shippingServiceLevels = Arrays.asList(shippingServiceLevel);

		context.checking(new Expectations() {
			{
				allowing(customerAddressDao).findByGuid(UPDATED_SHIPPING_ADDRESS_GUID);
				will(returnValue(shippingAddress));

				allowing(cartOrder).getBillingAddressGuid();
				will(returnValue(BILLING_ADDRESS_GUID));

				allowing(customerAddressDao).findByGuid(BILLING_ADDRESS_GUID);
				will(returnValue(billingAddress));

				allowing(shippingServiceLevel).getUidPk();
				will(returnValue(SHIPPING_SERVICE_LEVEL_UID));
			}
		});
	}

	@Test
	public void testCartOrderIsUpdatedWhenCartOrderShippingAddressIsNull() {
		allowingUpdateOnCartOrderFromExistingShippingAddressGuid();
		allowingShippingServiceLevelsForAddress(shippingServiceLevels);
		allowingShippingServiceLevelGuidOnCartOrder();
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingAddressGuid();
				will(returnValue(null));

				allowing(cartOrderShippingInformationSanitizer).sanitize(STORE_CODE, cartOrder);

				allowing(shippingServiceLevel).getGuid();
				will(returnValue(EXISTING_SHIPPING_SERVICE_LEVEL_GUID));

				allowing(cartOrder).setShippingAddressGuid(with(any(String.class)));
			}
		});

		boolean result = service.updateCartOrderShippingAddress(UPDATED_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertTrue(CART_ORDER_SHOULD_BE_UPDATED, result);
	}

	@Test
	public void testCartOrderIsNotUpdatedWhenUpdatingToSameShippingAddressGuid() {
		allowingShippingServiceLevelGuidOnCartOrder();

		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingAddressGuid();
				will(returnValue(EXISTING_SHIPPING_ADDRESS_GUID));
			}
		});

		boolean result = service.updateCartOrderShippingAddress(EXISTING_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertFalse("Cart Order should not be updated.", result);
	}

	@Test
	public void testCartIsSanitizedWhenCartOrderShippingAddressGuidIsUpdated() {
		allowingUpdateOnCartOrderFromExistingShippingAddressGuid();
		allowingShippingServiceLevelsForAddress(Collections.<ShippingServiceLevel>emptyList());
		allowingShippingServiceLevelGuidOnCartOrder();

		boolean result = service.updateCartOrderShippingAddress(UPDATED_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertTrue(CART_ORDER_SHOULD_BE_UPDATED, result);
	}

	@Test
	public void testShippingServiceLevelIsNotSetIfNoneValidForAddress() {
		allowingUpdateOnCartOrderFromExistingShippingAddressGuid();
		allowingShippingServiceLevelsForAddress(Collections.<ShippingServiceLevel>emptyList());
		allowingShippingServiceLevelGuidOnCartOrder();

		context.checking(new Expectations() {
			{
				never(cartOrder).setShippingAddressGuid(with(any(String.class)));
			}
		});

		boolean result = service.updateCartOrderShippingAddress(UPDATED_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertTrue(CART_ORDER_SHOULD_BE_UPDATED, result);
	}

	@Test
	public void testShippingServiceLevelDoesNotSwitchToDefaultIfStillValid() {
		allowingUpdateOnCartOrderFromExistingShippingAddressGuid();
		allowingShippingServiceLevelsForAddress(shippingServiceLevels);
		allowingShippingServiceLevelGuidOnCartOrder();

		context.checking(new Expectations() {
			{
				allowing(shippingServiceLevel).getGuid();
				will(returnValue(EXISTING_SHIPPING_SERVICE_LEVEL_GUID));

				never(cartOrder).setShippingAddressGuid(with(any(String.class)));
			}
		});

		boolean result = service.updateCartOrderShippingAddress(UPDATED_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertTrue(CART_ORDER_SHOULD_BE_UPDATED, result);
	}

	@Test
	public void testShippingServiceLevelSwitchesToDefaultIfExistingSSLNotValid() {
		allowingUpdateOnCartOrderFromExistingShippingAddressGuid();
		allowingShippingServiceLevelsForAddress(shippingServiceLevels);
		allowingShippingServiceLevelGuidOnCartOrder();
		context.checking(new Expectations() {
			{
				allowing(shippingServiceLevel).getGuid();
				will(returnValue(NEW_SHIPPING_SERVICE_LEVEL_GUID));

				oneOf(cartOrder).setShippingServiceLevelGuid(NEW_SHIPPING_SERVICE_LEVEL_GUID);
			}
		});

		boolean result = service.updateCartOrderShippingAddress(UPDATED_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertTrue(CART_ORDER_SHOULD_BE_UPDATED, result);
	}

	@Test
	public void testCartOrderIsUpdatedWhenUpdatingToSameShippingAddressGuidWhenSelectedSSLIsNull() {
		allowingUpdateOnCartOrderFromExistingShippingAddressGuid();
		allowingShippingServiceLevelsForAddress(shippingServiceLevels);
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingAddressGuid();
				will(returnValue(EXISTING_SHIPPING_ADDRESS_GUID));

				allowing(cartOrder).getShippingServiceLevelGuid();
				will(returnValue(null));

				allowing(cartOrderShippingInformationSanitizer).sanitize(STORE_CODE, cartOrder);

				allowing(shippingServiceLevel).getGuid();
				will(returnValue(EXISTING_SHIPPING_SERVICE_LEVEL_GUID));

				allowing(cartOrder).setShippingAddressGuid(with(any(String.class)));

				oneOf(cartOrder).setShippingServiceLevelGuid(EXISTING_SHIPPING_SERVICE_LEVEL_GUID);

			}
		});

		boolean result = service.updateCartOrderShippingAddress(EXISTING_SHIPPING_ADDRESS_GUID, cartOrder, STORE_CODE);

		assertTrue(CART_ORDER_SHOULD_BE_UPDATED, result);
	}

	/**
	 * Test population of shopping cart with valid address and shipping transient fields.
	 */
	@Test
	public void testPopulateAddressAndShippingFields() {
		allowingShippingServiceLevelGuidOnCartOrder();

		context.checking(new Expectations() {
			{
				oneOf(cartOrder).getShippingAddressGuid();
				will(returnValue(SHIPPING_ADDRESS_GUID));
				oneOf(customerAddressDao).findByGuid(SHIPPING_ADDRESS_GUID);
				will(returnValue(shippingAddress));
				oneOf(shoppingCart).getStore();
				will(returnValue(store));
				oneOf(store).getCode();
				will(returnValue(STORE_CODE));
				oneOf(shippingServiceLevelService).retrieveShippingServiceLevel(STORE_CODE, shippingAddress);
				will(returnValue(shippingServiceLevels));
				allowing(shippingServiceLevel).getGuid();
				will(returnValue(EXISTING_SHIPPING_SERVICE_LEVEL_GUID));

				oneOf(shoppingCart).setBillingAddress(billingAddress);
				oneOf(shoppingCart).setShippingAddress(shippingAddress);
				oneOf(shoppingCart).setSelectedShippingServiceLevelUid(SHIPPING_SERVICE_LEVEL_UID);
				oneOf(shoppingCart).setShippingServiceLevelList(shippingServiceLevels);
			}
		});
		service.populateAddressAndShippingFields(shoppingCart, cartOrder);
	}

	/**
	 * Test population of shopping cart with null shipping address.
	 */
	@Test
	public void testPopulationOfShoppingCartWithNullShippingAddress() {
		allowingShippingServiceLevelGuidOnCartOrder();
		context.checking(new Expectations() {
			{
				allowing(shippingServiceLevel).getGuid();
				will(returnValue(NEW_SHIPPING_SERVICE_LEVEL_GUID));

				oneOf(shippingServiceLevelService).retrieveShippingServiceLevel(STORE_CODE, null);
				will(returnValue(Collections.emptyList()));

				oneOf(cartOrder).getShippingAddressGuid();
				will(returnValue("nonexistent-guid"));

				oneOf(customerAddressDao).findByGuid("nonexistent-guid");
				will(returnValue(null));

				oneOf(shoppingCart).setBillingAddress(billingAddress);
				oneOf(shoppingCart).setShippingAddress(null);
				never(shoppingCart).setSelectedShippingServiceLevelUid(with(any(Long.class)));
				oneOf(shoppingCart).getStore();
				will(returnValue(store));

				oneOf(store).getCode();
				will(returnValue(STORE_CODE));
			}
		});

		service.populateAddressAndShippingFields(shoppingCart, cartOrder);

	}

	private void allowingUpdateOnCartOrderFromExistingShippingAddressGuid() {
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingAddressGuid();
				will(returnValue(EXISTING_SHIPPING_ADDRESS_GUID));

				allowing(customerAddressDao).findByGuid(EXISTING_SHIPPING_ADDRESS_GUID);
				will(returnValue(shippingAddress));

				allowing(cartOrder).setShippingAddressGuid(UPDATED_SHIPPING_ADDRESS_GUID);

				oneOf(cartOrderShippingInformationSanitizer).sanitize(STORE_CODE, cartOrder);
			}
		});
	}

	private void allowingShippingServiceLevelsForAddress(final List<ShippingServiceLevel> shippingServiceLevels) {
		context.checking(new Expectations() {
			{
				allowing(shippingServiceLevelService).retrieveShippingServiceLevel(STORE_CODE, shippingAddress);
				will(returnValue(shippingServiceLevels));
			}
		});
	}

	private void allowingShippingServiceLevelGuidOnCartOrder() {
		context.checking(new Expectations() {
			{
				allowing(cartOrder).getShippingServiceLevelGuid();
				will(onConsecutiveCalls(
					returnValue(EXISTING_SHIPPING_SERVICE_LEVEL_GUID),
					returnValue(EXISTING_SHIPPING_SERVICE_LEVEL_GUID),
					returnValue(NEW_SHIPPING_SERVICE_LEVEL_GUID)));

			}
		});
	}

}
