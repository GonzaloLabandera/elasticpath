/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

@RunWith(MockitoJUnitRunner.class)
public class UniqueCartDataValidatorImplTest {

	private static final String STORECODE = "storeCode";
	private static final String CUSTOMER_GUID = "customerGUID";
	private static final String NAME = "name";
	private static final String VALUE = "a";
	private static final String OTHERCARTGUID = "OTHERCARTGUID";

	@InjectMocks
	private UniqueCartDataValidatorImpl validator;

	@Mock
	private ShoppingCartValidationContext context;

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Shopper shopper;

	@Mock
	private Customer customer;


	@Before
	public void setUp() {

		when(context.getShoppingCart()).thenReturn(shoppingCart);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shopper.getCustomer()).thenReturn(customer);
		when(customer.getGuid()).thenReturn(CUSTOMER_GUID);
		when(shopper.getStoreCode()).thenReturn(STORECODE);
	}

	@Test
	public void testValidateWithConflict() {

		CartData otherCartData = mock(CartData.class);
		CartData cartData = mock(CartData.class);

		when(shoppingCart.getCartData()).thenReturn(Collections.singletonMap(NAME, cartData));

		List<String> otherCartGuids = Collections.singletonList(OTHERCARTGUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORECODE))
				.thenReturn(otherCartGuids);

		List<CartData> otherCartDataList = Collections.singletonList(otherCartData);
		Map<String, List<CartData>> otherCartDatas = Collections.singletonMap(OTHERCARTGUID, otherCartDataList);

		when(shoppingCartService.findCartDataForCarts(otherCartGuids))
				.thenReturn(otherCartDatas);

		when(cartData.getValue()).thenReturn(VALUE);
		when(otherCartData.getValue()).thenReturn(VALUE);
		when(otherCartData.getKey()).thenReturn(NAME);

		Collection<StructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isNotEmpty();

	}

	@Test
	public void testValidateWithoutConflict() {

		CartData otherCartData = mock(CartData.class);
		CartData cartData = mock(CartData.class);

		when(shoppingCart.getCartData()).thenReturn(Collections.singletonMap(NAME, cartData));

		List<String> otherCartGuids = Collections.singletonList(OTHERCARTGUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORECODE))
				.thenReturn(otherCartGuids);

		List<CartData> otherCartDataList = Collections.singletonList(otherCartData);
		Map<String, List<CartData>> otherCartDatas = Collections.singletonMap(OTHERCARTGUID, otherCartDataList);

		when(shoppingCartService.findCartDataForCarts(otherCartGuids))
				.thenReturn(otherCartDatas);

		when(cartData.getValue()).thenReturn(VALUE);
		when(otherCartData.getKey()).thenReturn(NAME);
		when(otherCartData.getValue()).thenReturn("otherValue");

		Collection<StructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isEmpty();
	}

	@Test
	public void testValidateWhenOtherCartHasNoCartData() {

		CartData cartData = mock(CartData.class);

		when(shoppingCart.getCartData()).thenReturn(Collections.singletonMap(NAME, cartData));

		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORECODE))
				.thenReturn(Collections.singletonList(OTHERCARTGUID));

		List<String> otherCartGuids = Collections.singletonList(OTHERCARTGUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORECODE))
				.thenReturn(otherCartGuids);

		List<CartData> otherCartDataList = Collections.emptyList();
		Map<String, List<CartData>> otherCartDatas = Collections.singletonMap(OTHERCARTGUID, otherCartDataList);

		when(shoppingCartService.findCartDataForCarts(otherCartGuids))
				.thenReturn(otherCartDatas);

		Collection<StructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isEmpty();
	}
	@Test
	public void testCreateWithNoOtherCarts() {

		CartData cartData = mock(CartData.class);

		when(shoppingCart.getCartData()).thenReturn(Collections.singletonMap(NAME, cartData));

		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORECODE))
				.thenReturn(Collections.emptyList());

		Collection<StructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isEmpty();
	}

}