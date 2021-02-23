/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collection;
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
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;

@RunWith(MockitoJUnitRunner.class)
public class SuspendedAccountShoppingCartValidatorImplTest {
	private static final String ACCOUNT_SHARED_ID = "accountSharedId";
	private static final String ACCOUNT_BUSINESS_NAME = "accountBusinessName";
	private static final String ACCOUNT_GUID = "accountGuid";

	@InjectMocks
	private SuspendedAccountShoppingCartValidatorImpl validator;

	@Mock
	private ShoppingCartValidationContext context;

	@Mock
	private Shopper shopper;

	@Mock
	private Customer customer;

	@Mock
	private ShoppingCart shoppingCart;


	@Before
	public void setUp() throws Exception {
		given(context.getShoppingCart()).willReturn(shoppingCart);
		given(shoppingCart.getShopper()).willReturn(shopper);
		given(shopper.getAccount()).willReturn(customer);
	}

	@Test
	public void testThatValidatorReturnsStructuredErrorMessageIfAccountStatusSuspended() {
		given(customer.getSharedId()).willReturn(ACCOUNT_SHARED_ID);
		given(customer.getBusinessName()).willReturn(ACCOUNT_BUSINESS_NAME);
		given(customer.getGuid()).willReturn(ACCOUNT_GUID);

		final StructuredErrorMessage structuredErrorMessage = createErrorMessage(context);

		given(customer.getStatus()).willReturn(Customer.STATUS_SUSPENDED);

		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testThatValidatorReturnsEmptyListIfAccountNotSuspended() {
		given(customer.getStatus()).willReturn(Customer.STATUS_DISABLED);

		Collection<StructuredErrorMessage> messageCollections = validator.validate(context);

		assertThat(messageCollections).isEmpty();
	}

	private StructuredErrorMessage createErrorMessage(final ShoppingCartValidationContext context) {
		final Customer account = context.getShoppingCart().getShopper().getAccount();
		final Map<String, String> data = new HashMap<>();
		data.put("account-shared-id", account.getSharedId());
		data.put("account-business-name", account.getBusinessName());

		return new StructuredErrorMessage(StructuredErrorMessageType.ERROR, SuspendedAccountShoppingCartValidatorImpl.MESSAGE_ID,
				"The account you are transacting for is currently suspended.", data,
				new StructuredErrorResolution(Customer.class, account.getGuid()));
	}
}
