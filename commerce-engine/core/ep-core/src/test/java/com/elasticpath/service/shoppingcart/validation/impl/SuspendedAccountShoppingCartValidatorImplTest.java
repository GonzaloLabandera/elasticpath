/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFCustomerStatusEnum;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;

@RunWith(MockitoJUnitRunner.class)
public class SuspendedAccountShoppingCartValidatorImplTest {
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String ACCOUNT_BUSINESS_NAME = "accountBusinessName";
	private static final String ACCOUNT_GUID = "accountGuid";

	@InjectMocks
	private SuspendedAccountShoppingCartValidatorImpl validator;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock
	private XPFShopper shopper;

	@Mock
	private XPFCustomer customer;

	@Mock
	private XPFShoppingCart shoppingCart;

	@Mock
	private XPFAttributeValue attributeValue;


	@Before
	public void setUp() throws Exception {
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(shoppingCart.getShopper()).willReturn(shopper);
		given(shopper.getAccount()).willReturn(customer);
	}

	@Test
	public void testThatValidatorReturnsStructuredErrorMessageIfAccountStatusSuspended() {
		given(customer.getSharedId()).willReturn(ACCOUNT_SHARED_ID);
		given(customer.getAttributeValueByKey("AP_NAME", null)).willReturn(Optional.of(attributeValue));
		given(attributeValue.getStringValue()).willReturn(ACCOUNT_BUSINESS_NAME);
		given(customer.getGuid()).willReturn(ACCOUNT_GUID);
		given(customer.getStatus()).willReturn(XPFCustomerStatusEnum.STATUS_SUSPENDED);

		final XPFStructuredErrorMessage structuredErrorMessage = createErrorMessage(context);

		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testThatValidatorReturnsEmptyListIfAccountNotSuspended() {
		given(customer.getStatus()).willReturn(XPFCustomerStatusEnum.STATUS_DISABLED);

		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		assertThat(messageCollections).isEmpty();
	}

	private XPFStructuredErrorMessage createErrorMessage(final XPFShoppingCartValidationContext context) {
		final XPFCustomer account = context.getShoppingCart().getShopper().getAccount();
		final Map<String, String> data = new HashMap<>();
		data.put("account-shared-id", account.getSharedId());
		data.put("account-business-name",
				account.getAttributeValueByKey("AP_NAME", null).map(XPFAttributeValue::getStringValue).orElse(null));

		return new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.ERROR, SuspendedAccountShoppingCartValidatorImpl.MESSAGE_ID,
				"The account you are transacting for is currently suspended.", data,
				new XPFStructuredErrorResolution(XPFCustomer.class, account.getGuid()));
	}
}
