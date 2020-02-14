/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.base.exception.structured.InvalidBusinessStateException;
import com.elasticpath.domain.catalog.AvailabilityException;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.shoppingcart.CheckoutService;

/**
 * Test for the  {@link PurchaseRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchaseRepositoryImplTest {

	@Mock
	private CheckoutService checkoutService;
	@Mock
	private ExceptionTransformer exceptionTransformer;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private PurchaseRepositoryImpl purchaseRepository;

	@Before
	public void setUp() {
		purchaseRepository.setCheckoutService(checkoutService);
		purchaseRepository.setReactiveAdapter(reactiveAdapter);
	}

	@Test
	public void testCheckout() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);
		CheckoutResults checkoutResults = mock(CheckoutResults.class);

		when(checkoutService.checkout(shoppingCart, taxSnapshot, customerSession, true)).thenReturn(checkoutResults);

		purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession)
				.test()
				.assertNoErrors()
				.assertValue(checkoutResults);
	}

	@Test
	public void testCheckoutException() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);

		when(checkoutService.checkout(shoppingCart, taxSnapshot, customerSession, true))
				.thenThrow(new EpServiceException("an exception"));

		purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession)
				.test()
				.assertError(ResourceOperationFailure.stateFailure("EpServiceException: an exception"));
	}

	@Test
	public void testCheckoutInvalidBusinessStateException() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);

		String productNotAvailableError = "error message";
		StructuredErrorMessage structuredErrorMessage = mock(StructuredErrorMessage.class);
		when(checkoutService.checkout(shoppingCart, taxSnapshot, customerSession, true))
				.thenThrow(
						new AvailabilityException(
								productNotAvailableError,
								Collections.singletonList(structuredErrorMessage)
						)

				);
		ResourceOperationFailure failure = ResourceOperationFailure.stateFailure(productNotAvailableError);
		when(exceptionTransformer.getResourceOperationFailure(any(InvalidBusinessStateException.class)))
				.thenReturn(failure);

		purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession)
				.test()
				.assertError(failure);
	}

	@Test
	public void testCheckoutEpValidationException() {
		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		ShoppingCartTaxSnapshot taxSnapshot = mock(ShoppingCartTaxSnapshot.class);
		CustomerSession customerSession = mock(CustomerSession.class);

		String validationError = "validation error";
		StructuredErrorMessage structuredErrorMessage = mock(StructuredErrorMessage.class);
		when(checkoutService.checkout(shoppingCart, taxSnapshot, customerSession, true))
				.thenThrow(
						new EpValidationException(
								validationError,
								Collections.singletonList(structuredErrorMessage)
						)

				);
		ResourceOperationFailure failure = ResourceOperationFailure.stateFailure(validationError);
		when(exceptionTransformer.getResourceOperationFailure(any(EpValidationException.class)))
				.thenReturn(failure);

		purchaseRepository.checkout(shoppingCart, taxSnapshot, customerSession)
				.test()
				.assertError(failure);
	}

}