/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.KeyValueSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartPostProcessor;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.validation.CreateShoppingCartValidationService;
import com.elasticpath.service.shoppingcart.validation.ShoppingCartValidationContext;
import com.elasticpath.service.store.StoreService;

/**
 * Test for {@link B2CMultiCartResolutionStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class B2CMultiCartResolverStrategyImplTest {

	private static final String CUSTOMER_GUID = "customerGuid";
	private static final String STORE_CODE = "storeCode";


	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private CartPostProcessor cartPostProcessor;

	@Mock
	private CustomerSessionRepository customerSessionRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	private B2CMultiCartResolutionStrategyImpl strategy;

	@Mock
	private Subject subject;

	@Mock
	private StoreService storeService;

	@Mock
	private CreateShoppingCartValidationService createShoppingCartValidationService;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Before
	public void setUp() {
		strategy = new B2CMultiCartResolutionStrategyImpl();
		strategy.setReactiveAdapter(reactiveAdapterImpl);
		strategy.setReactiveAdapter(reactiveAdapterImpl);
		strategy.setCartPostProcessor(cartPostProcessor);
		strategy.setCreateShoppingCartValidationService(createShoppingCartValidationService);
		strategy.setCustomerSessionRepository(customerSessionRepository);
		strategy.setExceptionTransformer(exceptionTransformer);
		strategy.setShoppingCartService(shoppingCartService);
		strategy.setStoreService(storeService);
		strategy.setResourceOperationContext(resourceOperationContext);
	}


	@Test
	public void testGetValidCartTypeForStrategy() {
		assertThat(strategy.getValidCartTypeForStrategy()).isEqualTo("default");
	}


	@Test
	public void testIsApplicable() {

		SubjectAttribute attribute = new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID,
				"test", ShoppingCartResourceConstants.METADATA);
		when(subject.getAttributes()).thenReturn(Collections.singletonList(attribute));
		boolean applicable = strategy.isApplicable(subject);
		assertThat(applicable).isFalse();
	}
	@Test
	public void testIsApplicableWhenSubjectHasNoMetadata() {
		boolean applicable = strategy.isApplicable(subject);
		assertThat(applicable).isTrue();
	}

	@Test
	public void testIsApplicableWithNoSubject() {
		boolean applicable = strategy.isApplicable(null);
		assertThat(applicable).isTrue();
	}
	@Test
	public void testSupportsCreateWithRegistedCustomer() {
		Shopper mockShopper = mock(Shopper.class);
		Customer mockCustomer = mock(Customer.class);
		when(mockShopper.getCustomer()).thenReturn(mockCustomer);
		when(mockCustomer.isRegistered()).thenReturn(true);
		mockCartType(strategy.getValidCartTypeForStrategy());

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isTrue();
	}


	@Test
	public void testSupportsCreateWithNonRegisteredCustomer() {
		Shopper mockShopper = mock(Shopper.class);
		Customer mockCustomer = mock(Customer.class);
		when(mockShopper.getCustomer()).thenReturn(mockCustomer);
		when(mockCustomer.isRegistered()).thenReturn(false);
		mockCartType(strategy.getValidCartTypeForStrategy());

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isFalse();

	}

	@Test
	public void testSupportsCreateWithInvalidCartType() {
		Shopper mockShopper = mock(Shopper.class);
		mockCartType("invalid");

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isFalse();
	}

	@Test
	public void testSupportsCreateWithNoCartType() {
		Shopper mockShopper = mock(Shopper.class);
		when(storeService.getCartTypeNamesForStore(STORE_CODE)).thenReturn(Collections.emptyList());

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isFalse();
	}


	private void mockCartType(final String cartTypeName) {
		when(storeService.getCartTypeNamesForStore(STORE_CODE))
				.thenReturn(Collections.singletonList(cartTypeName));
	}

	@Test
	public void testValidateCreate() {

		ShoppingCart mockCart = mockExistingCart();

		ShoppingCartValidationContext mockContext = mock(ShoppingCartValidationContext.class);
		when(createShoppingCartValidationService.buildContext(mockCart))
				.thenReturn(mockContext);
		when(createShoppingCartValidationService.validate(mockContext))
				.thenReturn(Collections.emptyList());

			//will throw exception to fail test if validation fails.
			strategy.validateCreate(mockCart);

	}

	@Test(expected = EpStructureErrorMessageException.class)
	public void testValidateCreateWithException() {

		ShoppingCart mockCart = mockExistingCart();

		ShoppingCartValidationContext mockContext = mock(ShoppingCartValidationContext.class);
		when(createShoppingCartValidationService.buildContext(mockCart))
				.thenReturn(mockContext);
		StructuredErrorMessage errorMessage = mock(StructuredErrorMessage.class);
		when(createShoppingCartValidationService.validate(mockContext))
				.thenReturn(Collections.singletonList(errorMessage));

		//will throw exception to fail test if validation fails.
		strategy.validateCreate(mockCart);

	}
	@Test
	public void testFindAllCarts() {
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORE_CODE.toUpperCase(Locale.getDefault())))
				.thenReturn(Collections.singletonList(CART_GUID));

		strategy.findAllCarts(CUSTOMER_GUID, STORE_CODE, null)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(CART_GUID);

	}

	@Test
	public void testGetShoppingCart() {

		ShoppingCart mockCart = mockExistingCart();
		CustomerSession customerSession = mock(CustomerSession.class);

		when(customerSessionRepository.findOrCreateCustomerSession())
				.thenReturn(Single.just(customerSession));
		when(shoppingCartService.findByGuid(CART_GUID))
				.thenReturn(mockCart);

		strategy.getShoppingCartSingle(CART_GUID)
				.test()
				.assertNoErrors()
				.assertValue(mockCart);

	}

	@Test
	public void testGetModifierFields() {

		ModifierField modifierField = mock(ModifierField.class);
		CartType cartType = mock(CartType.class);
		Store store = mock(Store.class);
		ModifierGroup modifierGroup = mock(ModifierGroup.class);

		when(store.getShoppingCartTypes()).thenReturn(Collections.singletonList(cartType));
		when(cartType.getModifiers()).thenReturn(Collections.singletonList(modifierGroup));
		when(modifierGroup.getModifierFields()).thenReturn(Collections.singleton(modifierField));

		when(storeService.findStoreWithCode(STORE_CODE)).thenReturn(store);

		assertThat(strategy.getModifierFields(STORE_CODE)).containsExactly(modifierField);
	}

	/**
	 * Test the behaviour of get default shopping cart guid.
	 */
	@Test
	public void testGetDefaultShoppingCartGuidWhenNotFound() {
		CustomerSession mockCustomerSession = createMockCustomerSession();

		when(shoppingCartService.findDefaultShoppingCartGuidByCustomerSession(mockCustomerSession)).thenReturn(null);
		strategy.getDefaultShoppingCartGuid()
				.test()
				.assertError(isResourceOperationFailureNotFound());

	}

	/**
	 * Test the behaviour of get default shopping cart guid.
	 */
	@Test
	public void testGetDefaultShoppingCartGuid() {
		CustomerSession mockCustomerSession = createMockCustomerSession();

		when(shoppingCartService.findDefaultShoppingCartGuidByCustomerSession(mockCustomerSession)).thenReturn(CART_GUID);
		strategy.getDefaultShoppingCartGuid()
				.test()
				.assertNoErrors()
				.assertValue(CART_GUID);

	}

	private ShoppingCart mockExistingCart() {
		ShoppingCart mockCart = mock(ShoppingCart.class);
		when(shoppingCartService.findByGuid(CART_GUID))
				.thenReturn(mockCart);
		return mockCart;
	}

	private CustomerSession createMockCustomerSession() {
		CustomerSession mockCustomerSession = mock(CustomerSession.class);

		when(customerSessionRepository.findOrCreateCustomerSession()).thenReturn(Single.just(mockCustomerSession));
		return mockCustomerSession;
	}

	private Predicate<Throwable> isResourceOperationFailureNotFound() {
		return throwable -> throwable instanceof ResourceOperationFailure
				&& ResourceStatus.NOT_FOUND.equals(((ResourceOperationFailure) throwable).getResourceStatus());
	}


}
