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
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;

@RunWith(MockitoJUnitRunner.class)
public class EmailAddressShoppingCartValidatorImplTest {

	private static final String GUID = "GUID";

	@InjectMocks
	private EmailAddressShoppingCartValidatorImpl validator;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock
	private XPFShopper shopper;

	@Mock
	private XPFCustomer customer;

	@Mock
	private XPFShoppingCart shoppingCart;

	@Before
	public void setUp() throws Exception {

		validator = new EmailAddressShoppingCartValidatorImpl();

		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(shoppingCart.getShopper()).willReturn(shopper);
		given(shopper.getUser()).willReturn(customer);
		given(customer.getGuid()).willReturn(GUID);
	}

	@Test
	public void testEmailSet() {
		// Given
		given(customer.getEmail()).willReturn("info@some.com");

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void testEmailNotSet() {
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "need.email",
				"Customer email address must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(XPFCustomer.class, GUID));

		// Given
		given(customer.getEmail()).willReturn(null);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void testEmailSetButIncorrect() {

		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "need.email",
				"Customer email address must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(XPFCustomer.class, GUID));

		// Given
		given(customer.getEmail()).willReturn("k.boom.info");

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}
}

