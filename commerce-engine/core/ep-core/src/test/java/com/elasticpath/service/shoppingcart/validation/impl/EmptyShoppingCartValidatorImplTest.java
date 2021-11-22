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
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;

@RunWith(MockitoJUnitRunner.class)
public class EmptyShoppingCartValidatorImplTest {

	@InjectMocks
	private EmptyShoppingCartValidatorImpl validator;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock
	private XPFShoppingCart shoppingCart;

	@Before
	public void setUp() throws Exception {
		given(context.getShoppingCart()).willReturn(shoppingCart);
	}

	@Test
	public void testCartNotEmpty() {
		// Given
		given(shoppingCart.getLineItems()).willReturn(Collections.singletonList(null));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testCartEmpty() {
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "cart.empty",
				"Shopping cart is empty.", Collections.emptyMap());

		// Given
		given(shoppingCart.getLineItems()).willReturn(Collections.emptyList());

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

}