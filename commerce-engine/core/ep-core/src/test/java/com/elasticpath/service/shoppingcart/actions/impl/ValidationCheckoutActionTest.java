/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.actions.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.shoppingcart.actions.PreCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.exception.CheckoutValidationException;
import com.elasticpath.service.shoppingcart.validation.PurchaseCartValidationService;

/**
 * Unit tests for {@link ValidationCheckoutAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationCheckoutActionTest {

	public static final StructuredErrorMessage ERROR_MESSAGE
			= new StructuredErrorMessage("some.error", "debug.error.message", Collections.emptyMap());


	@InjectMocks
	private ValidationCheckoutAction checkoutAction;

	@Mock
	private PurchaseCartValidationService validationService;

	@Mock
	private PreCaptureCheckoutActionContext checkoutActionContext;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private Store store;

	@Mock
	private Shopper shopper;

	@Before
	public void setUp() {
		given(checkoutActionContext.getShoppingCart()).willReturn(shoppingCart);
		given(checkoutActionContext.getShopper()).willReturn(shopper);
		given(shoppingCart.getStore()).willReturn(store);
	}

	@Test
	public void testCheckoutValid() {
		// Given
		given(validationService.validate(shoppingCart, shopper, store)).willReturn(Collections.emptyList());

		// When
		checkoutAction.execute(checkoutActionContext);

		// Then
		verify(validationService).validate(shoppingCart, shopper, store);
	}


	@Test
	public void testCheckoutInvalid() {
		// Given
		given(validationService.validate(shoppingCart, shopper, store)).willReturn(
				ImmutableList.of(ERROR_MESSAGE));

		// When
		Throwable thrown = catchThrowable(() -> checkoutAction.execute(checkoutActionContext));

		// Then
		assertThat(thrown)
				.isInstanceOf(CheckoutValidationException.class);

		CheckoutValidationException validationException = (CheckoutValidationException) thrown;

		assertThat(validationException.getStructuredErrorMessages())
				.extracting(
						StructuredErrorMessage::getMessageId,
						StructuredErrorMessage::getDebugMessage,
						StructuredErrorMessage::getData)
				.containsExactly(tuple(
						ERROR_MESSAGE.getMessageId(),
						ERROR_MESSAGE.getDebugMessage(),
						ERROR_MESSAGE.getData()
				));
	}
}
