/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class UniqueCartDataValidatorImplTest {

	private static final String STORECODE = "storeCode";
	private static final String CUSTOMER_GUID = "customerGUID";
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String NAME = "name";
	private static final String VALUE = "a";
	private static final String OTHERCARTGUID = "OTHERCARTGUID";
	private static final String CURRENTCARTGUID = "cartGUID";

	@InjectMocks
	private UniqueCartDataValidatorImpl validator;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private XPFShoppingCart shoppingCart;

	@Mock
	private XPFShopper shopper;

	@Mock
	private XPFCustomer account;

	@Mock
	private XPFCustomer customer;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private XPFStore store;

	@Before
	public void setUp() {
		when(context.getShoppingCart()).thenReturn(shoppingCart);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shopper.getUser()).thenReturn(customer);
		when(shopper.getAccount()).thenReturn(account);
		when(customer.getGuid()).thenReturn(CUSTOMER_GUID);
		when(account.getSharedId()).thenReturn(ACCOUNT_SHARED_ID);
		when(shopper.getStore()).thenReturn(store);
		when(store.getCode()).thenReturn(STORECODE);
		when(shoppingCart.getGuid()).thenReturn(CURRENTCARTGUID);
		when(beanFactory.getSingletonBean(ContextIdNames.SHOPPING_CART_SERVICE, ShoppingCartService.class))
				.thenReturn(shoppingCartService);
	}

	@Test
	public void testValidateWithConflict() {

		Map<String, String> otherCartData = new HashMap<>();
		otherCartData.put(NAME, VALUE);

		Map<String, String> cartData = new HashMap<>();
		cartData.put(NAME, VALUE);

		when(shoppingCart.getModifierFields()).thenReturn(cartData);

		List<String> otherCartGuids = Collections.singletonList(OTHERCARTGUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORECODE))
				.thenReturn(otherCartGuids);

		List<Map<String, String>> otherCartDataList = Collections.singletonList(otherCartData);
		Map<String, List<Map<String, String>>> otherCartDatas = Collections.singletonMap(OTHERCARTGUID, otherCartDataList);

		when(shoppingCartService.findCartDataForCarts(otherCartGuids))
				.thenReturn(otherCartDatas);

		Collection<XPFStructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isNotEmpty();
	}

	@Test
	public void testValidateWithoutConflict() {

		Map<String, String> otherCartData = new HashMap<>();
		otherCartData.put(NAME, "otherValue");

		Map<String, String> cartData = new HashMap<>();
		cartData.put(NAME, VALUE);

		when(shoppingCart.getModifierFields()).thenReturn(cartData);

		List<String> otherCartGuids = Collections.singletonList(OTHERCARTGUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORECODE))
				.thenReturn(otherCartGuids);

		List<Map<String, String>> otherCartDataList = Collections.singletonList(otherCartData);
		Map<String, List<Map<String, String>>> otherCartDatas = Collections.singletonMap(OTHERCARTGUID, otherCartDataList);

		when(shoppingCartService.findCartDataForCarts(otherCartGuids))
				.thenReturn(otherCartDatas);

		Collection<XPFStructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isEmpty();
	}

	@Test
	public void testValidateWhenOtherCartHasNoCartData() {

		Map<String, String> cartData = new HashMap<>();

		when(shoppingCart.getModifierFields()).thenReturn(cartData);

		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORECODE))
				.thenReturn(Collections.singletonList(OTHERCARTGUID));

		List<String> otherCartGuids = Collections.singletonList(OTHERCARTGUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORECODE))
				.thenReturn(otherCartGuids);

		List<Map<String, String>> otherCartDataList = Collections.emptyList();
		Map<String, List<Map<String, String>>> otherCartDatas = Collections.singletonMap(OTHERCARTGUID, otherCartDataList);

		when(shoppingCartService.findCartDataForCarts(otherCartGuids))
				.thenReturn(otherCartDatas);

		Collection<XPFStructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isEmpty();
	}

	@Test
	public void testCreateWithNoOtherCarts() {

		Map<String, String> cartData = new HashMap<>();

		when(shoppingCart.getModifierFields()).thenReturn(cartData);

		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, ACCOUNT_SHARED_ID, STORECODE))
				.thenReturn(Collections.emptyList());

		Collection<XPFStructuredErrorMessage> result = validator.validate(context);

		assertThat(result).isEmpty();
	}

}