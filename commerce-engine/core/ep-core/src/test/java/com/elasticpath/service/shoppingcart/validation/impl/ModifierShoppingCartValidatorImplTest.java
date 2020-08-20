/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.common.dto.StructuredErrorMessageType;
import com.elasticpath.base.common.dto.StructuredErrorResolution;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierType;
import com.elasticpath.domain.modifier.impl.ModifierFieldImpl;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.MulticartItemListTypeLocationProvider;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

@RunWith(MockitoJUnitRunner.class)
public class ModifierShoppingCartValidatorImplTest {

	private static final String GUID = "GUID";
	private static final String STORE_CODE = "TESTSTORE";
	private static final String CART_TYPE_NAME = "SHOPPING_CART";
	private static final String FIELD_NAME = "FIELD_NAME";

	@InjectMocks
	private ModifierShoppingCartValidatorImpl validator;

	@Mock
	private MulticartItemListTypeLocationProvider multicartItemListTypeLocationProvider;

	@Mock
	private ShoppingCartValidationContext context;

	@Mock
	private Store store;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private CartType cartType;

	@Mock
	private ModifierGroup modifierGroup;

	private final ModifierField modifierField = new ModifierFieldImpl();

	@Mock
	private CartData cartData;

	@Mock
	private CartOrder cartOrder;

	@Before
	public void setUp() throws Exception {

		given(multicartItemListTypeLocationProvider.getMulticartItemListTypeForStore(STORE_CODE))
				.willReturn(CART_TYPE_NAME);

		given(shoppingCart.getStore()).willReturn(store);
		given(store.getShoppingCartTypes()).willReturn(Collections.singletonList(cartType));
		given(store.getCode()).willReturn(STORE_CODE);
		given(cartType.getName()).willReturn(CART_TYPE_NAME);
		given(cartType.getModifiers()).willReturn(Collections.singletonList(modifierGroup));
		given(modifierGroup.getModifierFields()).willReturn(Collections.singleton(modifierField));
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(shoppingCart.getCartData()).willReturn(Collections.singletonMap(FIELD_NAME, cartData));
		given(cartData.getKey()).willReturn(FIELD_NAME);
		given(context.getCartOrder()).willReturn(cartOrder);
		given(cartOrder.getGuid()).willReturn(GUID);
		modifierField.setCode(FIELD_NAME);
		modifierField.setRequired(true);
		modifierField.setFieldType(ModifierType.SHORT_TEXT);

	}

	@Test
	public void testRequiredCartDataIsProvided() {
		// Given
		given(cartData.getValue()).willReturn("cart data");

		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testRequiredCartDataIsNotProvided() {

		Map<String, String> data = new HashMap<>();
		data.put("field-name", FIELD_NAME);
		StructuredErrorMessage errorMessage = new StructuredErrorMessage(StructuredErrorMessageType.NEEDINFO, "cart.missing.data",
				"'" + FIELD_NAME + "'"
				+ " cart descriptor value is required.", data, new StructuredErrorResolution(CartOrder.class, GUID));

		// Given
		given(cartData.getValue()).willReturn("");

		// When
		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}
}

