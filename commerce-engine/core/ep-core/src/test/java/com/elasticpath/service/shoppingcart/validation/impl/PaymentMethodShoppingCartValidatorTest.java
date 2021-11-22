/*
 * Copyright (c) Elastic Path Software Inc., 2019
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

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.xpf.connectivity.context.XPFShoppingCartValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessageType;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorResolution;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFShopper;
import com.elasticpath.xpf.connectivity.entity.XPFShoppingCart;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodShoppingCartValidatorTest {
	private static final String CART_ORDER_GUID = "CART_ORDER_GUID";
	private static final String STORE_CODE = "STORE_CODE";
	private static final String USER_GUID = "USER_GUID";
	private static final String ACCOUNT_GUID = "ACCOUNT_GUID";
	private static final String MESSAGE_ID_NEED_PAYMENT_METHOD = "need.payment.method";
	private static final String MESSAGE_PAYMENT_METHOD_MUST_BE_SPECIFIED = "Payment method must be specified.";

	@Mock
	private XPFShoppingCart xpfShoppingCart;

	@Mock
	private XPFShopper xpfShopper;

	@Mock
	private XPFCustomer xpfUser;

	@Mock
	private XPFCustomer xpfAccount;

	@Mock
	private XPFStore xpfStore;

	@Mock
	private CartOrderPaymentInstrument cartOrderPaymentInstrument;

	@Mock
	private CustomerPaymentInstrument customerPaymentInstrument;

	@Mock
	private XPFShoppingCartValidationContext context;

	@Mock
	private FilteredPaymentInstrumentService filteredPaymentInstrumentService;

	@InjectMocks
	private PaymentMethodShoppingCartValidatorImpl validator;

	@Before
	public void setUp() throws Exception {
		given(context.getShoppingCart()).willReturn(xpfShoppingCart);
		given(xpfShoppingCart.getCartOrderGuid()).willReturn(CART_ORDER_GUID);
		given(xpfShoppingCart.getShopper()).willReturn(xpfShopper);
		given(xpfShopper.getStore()).willReturn(xpfStore);
		given(xpfStore.getCode()).willReturn(STORE_CODE);
		given(xpfShopper.getUser()).willReturn(xpfUser);
		given(xpfUser.getGuid()).willReturn(USER_GUID);
	}

	@Test
	public void userPaymentMethodRequiredButNotSpecified() {
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, MESSAGE_ID_NEED_PAYMENT_METHOD,
				MESSAGE_PAYMENT_METHOD_MUST_BE_SPECIFIED, Collections.emptyMap(),
				new XPFStructuredErrorResolution(CartOrder.class, CART_ORDER_GUID));

		// Given
		given(context.isPaymentRequired()).willReturn(true);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(CART_ORDER_GUID, STORE_CODE))
				.willReturn(Collections.EMPTY_LIST);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void userPaymentMethodRequiredAndSpecified() {
		// Given
		given(context.isPaymentRequired()).willReturn(true);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(CART_ORDER_GUID, STORE_CODE))
				.willReturn(Collections.singletonList(cartOrderPaymentInstrument));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void paymentMethodRequiredButNotSpecified() {
		XPFStructuredErrorMessage errorMessage = new XPFStructuredErrorMessage(XPFStructuredErrorMessageType.NEEDINFO, "need.payment.method",
				"Payment method must be specified.", Collections.emptyMap(),
				new XPFStructuredErrorResolution(CartOrder.class, CART_ORDER_GUID));

		// Given
		given(context.isPaymentRequired()).willReturn(true);
		given(xpfShopper.getAccount()).willReturn(xpfAccount);
		given(xpfAccount.getGuid()).willReturn(ACCOUNT_GUID);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(CART_ORDER_GUID, STORE_CODE))
				.willReturn(Collections.EMPTY_LIST);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).containsOnly(errorMessage);
	}

	@Test
	public void accountPaymentMethodRequiredAndSpecified() {
		// Given
		given(context.isPaymentRequired()).willReturn(true);
		given(xpfShopper.getAccount()).willReturn(xpfAccount);
		given(xpfAccount.getGuid()).willReturn(ACCOUNT_GUID);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(CART_ORDER_GUID, STORE_CODE))
				.willReturn(Collections.singletonList(cartOrderPaymentInstrument));

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void accountPaymentMethodRequiredAndDefaultSpecified() {
		// Given
		given(context.isPaymentRequired()).willReturn(true);
		given(xpfShopper.getAccount()).willReturn(xpfAccount);
		given(xpfAccount.getGuid()).willReturn(ACCOUNT_GUID);
		given(filteredPaymentInstrumentService.findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(CART_ORDER_GUID, STORE_CODE))
				.willReturn(Collections.EMPTY_LIST);
		given(filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerGuidAndStore(ACCOUNT_GUID, STORE_CODE))
				.willReturn(customerPaymentInstrument);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

	@Test
	public void paymentMethodNotRequired() {
		// Given
		given(context.isPaymentRequired()).willReturn(false);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}
}