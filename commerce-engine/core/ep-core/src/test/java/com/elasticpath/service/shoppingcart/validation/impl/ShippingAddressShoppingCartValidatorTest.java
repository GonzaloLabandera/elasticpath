/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFAddress;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;

@RunWith(MockitoJUnitRunner.class)
public class ShippingAddressShoppingCartValidatorTest {
	private static final String SHOPPING_CART_GUID = "shoppingCartGuid";

	@InjectMocks
	private ShippingAddressShoppingCartValidatorImpl validator;

	@Mock
	private XPFShoppingCart shoppingCart;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock
	private XPFAddress shippingAddress;

	@Before
	public void setUp() throws Exception {
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(shoppingCart.isRequiresShipping()).willReturn(true);
	}

	@Test
	public void testShippingNoAddressSpecifiedNotShippableItem() {
		// Given
		given(shoppingCart.isRequiresShipping()).willReturn(false);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testShippingAddressSpecified() {
		// Given
		given(shoppingCart.getShippingAddress()).willReturn(shippingAddress);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testShippingAddressNotSpecified() {
		// Given
		given(shoppingCart.getShippingAddress()).willReturn(null);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "need.shipping.address",
				"Shipping address must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(XPFShoppingCart.class, SHOPPING_CART_GUID));
		assertThat(messageCollections).containsOnly(errorMessage);
	}
}