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
public class BillingAddressShoppingCartValidatorTest {

	private static final String GUID = "GUID";

	@InjectMocks
	private BillingAddressShoppingCartValidatorImpl validator;

	@Mock
	private XPFAddress address;

	@Mock
	private XPFShoppingCart shoppingCart;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Before
	public void setUp() throws Exception {
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(shoppingCart.getGuid()).willReturn(GUID);
	}

	@Test
	public void billAddressSpecified() {
		// Given
		given(shoppingCart.getBillingAddress()).willReturn(address);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();

	}

	@Test
	public void billingAddressNotSpecified() {
		// Given
		given(shoppingCart.getBillingAddress()).willReturn(null);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "need.billing.address",
				"Billing address must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(XPFShoppingCart.class, shoppingCart.getGuid()));
		assertThat(messageCollections).containsOnly(errorMessage);
	}

}