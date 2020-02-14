/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.identity.ScopePrincipal;
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
 * Test for {@link B2BMultiCartResolutionStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class B2BMultiCartResolverStrategyImplTest {

	private static final String STORE_CODE = "storeCode";
	private static final String CUSTOMER_GUID = "customerGuid";
	private static final String USER_ID_VALUE = "user-id";


	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	private CartPostProcessor cartPostProcessor;

	@Mock
	private CustomerSessionRepository customerSessionRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapterImpl;

	private B2BMultiCartResolutionStrategyImpl strategy;

	@Mock
	private Subject subject;

	@Mock
	private StoreService storeService;

	@Mock
	private CreateShoppingCartValidationService createShoppingCartValidationService;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private ExceptionTransformer exceptionTransformer;

	@Before
	public void setUp() {
		strategy = new B2BMultiCartResolutionStrategyImpl();
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
		assertThat(applicable).isTrue();
	}


	@Test
	public void testIsApplicableWhenSubjectHasNoMetadata() {
		boolean applicable = strategy.isApplicable(subject);
		assertThat(applicable).isFalse();
	}

	@Test
	public void testIsApplicableWithNoSubject() {
		boolean applicable = strategy.isApplicable(null);
		assertThat(applicable).isFalse();
	}

	@Test
	public void testSupportsCreateWithRegistedCustomer() {
		Shopper mockShopper = mock(Shopper.class);
		Customer mockCustomer = mock(Customer.class);

		mockCartType(strategy.getValidCartTypeForStrategy());

		when(mockShopper.getCustomer()).thenReturn(mockCustomer);
		when(mockCustomer.isRegistered()).thenReturn(true);

		boolean result = strategy.supportsCreate(subject, mockShopper, STORE_CODE);
		assertThat(result).isTrue();
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

	@Test
	public void testValidateCreateWithException() {

		ShoppingCart mockCart = mockExistingCart();

		ShoppingCartValidationContext mockContext = mock(ShoppingCartValidationContext.class);
		when(createShoppingCartValidationService.buildContext(mockCart))
				.thenReturn(mockContext);
		StructuredErrorMessage errorMessage = mock(StructuredErrorMessage.class);
		when(errorMessage.toString()).thenReturn("error Message");

		when(createShoppingCartValidationService.validate(mockContext))
				.thenReturn(Collections.singletonList(errorMessage));

		try {

			strategy.validateCreate(mockCart);

			//strategy should have thrown error,
			//so fail if we get here
			fail();
		} catch (EpSystemException exception) {
			assertThat(exception).isInstanceOf(EpStructureErrorMessageException.class);
			assertThat(exception.getMessage()).isEqualTo("Create cart validation failure.: [error Message]");
		}
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


	@Test
	public void testFindAllCarts() {
		List<String> cartGuids = Collections.singletonList(CART_GUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORE_CODE))
				.thenReturn(cartGuids);

		Map<String, List<CartData>> cartDataMap = new HashMap<>();
		CartData data = new CartData(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID, USER_ID_VALUE);
		cartDataMap.put(CART_GUID, Collections.singletonList(data));

		when(shoppingCartService.findCartDataForCarts(cartGuids))
				.thenReturn(cartDataMap);
		Subject subject = mock(Subject.class);
		SubjectAttribute attribute =
				new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID, USER_ID_VALUE,
						ShoppingCartResourceConstants.METADATA);
		Collection<SubjectAttribute> attributes = Collections.singletonList(attribute);
		when(subject.getAttributes()).thenReturn(attributes);

		strategy.findAllCarts(CUSTOMER_GUID, STORE_CODE, subject)
				.test()
				.assertNoErrors()
				.assertValue(CART_GUID);
	}

	@Test
	public void testFindAllCartsWhenSubjectHasNoAttributes() {
		List<String> cartGuids = Collections.singletonList(CART_GUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORE_CODE))
				.thenReturn(cartGuids);

		Map<String, List<CartData>> cartDataMap = new HashMap<>();
		CartData data = new CartData(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID, USER_ID_VALUE);
		cartDataMap.put(CART_GUID, Collections.singletonList(data));

		when(shoppingCartService.findCartDataForCarts(cartGuids))
				.thenReturn(cartDataMap);
		Subject subject = mock(Subject.class);
		when(subject.getAttributes()).thenReturn(Collections.emptyList());

		strategy.findAllCarts(CUSTOMER_GUID, STORE_CODE, subject)
				.test()
				.assertError(EpSystemException.class);
	}

	@Test
	public void testFindAllCartsWhenCartDataNotMatching() {
		List<String> cartGuids = Collections.singletonList(CART_GUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORE_CODE))
				.thenReturn(cartGuids);

		Map<String, List<CartData>> cartDataMap = new HashMap<>();
		CartData data = new CartData("other-id", "otherValue");
		cartDataMap.put(CART_GUID, Collections.singletonList(data));

		when(shoppingCartService.findCartDataForCarts(cartGuids))
				.thenReturn(cartDataMap);

		SubjectAttribute attribute =
				new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID, USER_ID_VALUE,
						ShoppingCartResourceConstants.METADATA);
		Collection<SubjectAttribute> attributes = Collections.singletonList(attribute);
		when(subject.getAttributes()).thenReturn(attributes);

		strategy.findAllCarts(CUSTOMER_GUID, STORE_CODE, subject)
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

	@Test
	public void testGetDefaultShoppingCartGuid() {
		when(resourceOperationContext.getSubject())
				.thenReturn(subject);

		when(resourceOperationContext.getUserIdentifier())
				.thenReturn(CUSTOMER_GUID);

		ScopePrincipal principal = new ScopePrincipal(STORE_CODE);
		when(subject.getPrincipals()).thenReturn(Collections.singletonList(principal));
		List<String> cartGuids = Collections.singletonList(CART_GUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORE_CODE))
				.thenReturn(cartGuids);

		String subjectAttributeUserEmailValue = "email@email";
		String subjectAttributeUserNameValue = "Test Name";


		mockCartData(cartGuids, subjectAttributeUserEmailValue, subjectAttributeUserNameValue, USER_ID_VALUE);
		mockAssociateAttributes(subjectAttributeUserEmailValue, subjectAttributeUserNameValue, USER_ID_VALUE);

		strategy.getDefaultShoppingCartGuid()
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(CART_GUID);
	}

	@Test
	public void testGetDefaultShoppingCartGuidCreatesNewCartWhenNotFound() {
		when(resourceOperationContext.getSubject())
				.thenReturn(subject);

		when(resourceOperationContext.getUserIdentifier())
				.thenReturn(CUSTOMER_GUID);

		ScopePrincipal principal = new ScopePrincipal(STORE_CODE);
		when(subject.getPrincipals()).thenReturn(Collections.singletonList(principal));
		List<String> cartGuids = Collections.singletonList(CART_GUID);
		when(shoppingCartService.findByCustomerAndStore(CUSTOMER_GUID, STORE_CODE))
				.thenReturn(cartGuids);

		String subjectAttributeUserEmailValue = "email@email";
		String subjectAttributeUserNameValue = "Test Name";

		mockCartData(cartGuids, subjectAttributeUserEmailValue, subjectAttributeUserNameValue, "not found");
		mockAssociateAttributes(subjectAttributeUserEmailValue, subjectAttributeUserNameValue, USER_ID_VALUE);

		CustomerSession customerSession = mock(CustomerSession.class);
		Shopper shopper = mock(Shopper.class);
		when(customerSession.getShopper()).thenReturn(shopper);

		when(customerSessionRepository.createCustomerSessionAsSingle())
				.thenReturn(Single.just(customerSession));

		ShoppingCart cart = mock(ShoppingCart.class);
		when(shoppingCartService.createByCustomerSession(customerSession))
				.thenReturn(cart);

		ShoppingCartValidationContext validationContext = mock(ShoppingCartValidationContext.class);
		when(createShoppingCartValidationService.buildContext(cart))
				.thenReturn(validationContext);
		when(createShoppingCartValidationService.validate(validationContext))
				.thenReturn(Collections.emptyList());

		when(shoppingCartService.saveOrUpdate(cart))
				.thenReturn(cart);

		when(cart.getGuid()).thenReturn(CART_GUID);

		strategy.getDefaultShoppingCartGuid()
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(CART_GUID);

		verify(cartPostProcessor).postProcessCart(cart, shopper, customerSession);
	}

	private void mockCartData(final List<String> cartGuids,
							  final String subjectAttributeUserEmailValue,
							  final String subjectAttributeUserNameValue,
							  final String subjectAttributeUserIdValue) {
		Map<String, List<CartData>> cartDataMap = new HashMap<>();
		CartData subjectAttributeUserIdData =
				new CartData(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID, subjectAttributeUserIdValue);
		CartData subjectAttributeUserEmailData =
				new CartData(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL, subjectAttributeUserEmailValue);
		CartData subjectAttributeUserNameData =
				new CartData(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME, subjectAttributeUserNameValue);
		cartDataMap.put(CART_GUID, Arrays.asList(subjectAttributeUserIdData, subjectAttributeUserEmailData, subjectAttributeUserNameData));

		when(shoppingCartService.findCartDataForCarts(cartGuids))
				.thenReturn(cartDataMap);
	}

	private void mockAssociateAttributes(final String subjectAttributeUserEmailValue,
										 final String subjectAttributeUserNameValue,
										 final String subjectAttributeUserIdValue) {
		SubjectAttribute subjectAttributeUserIdAttribute = new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_ID,
				subjectAttributeUserIdValue, ShoppingCartResourceConstants.METADATA);
		SubjectAttribute subjectAttributeUserEmailAttribute = new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_EMAIL,
				subjectAttributeUserEmailValue, ShoppingCartResourceConstants.METADATA);
		SubjectAttribute subjectAttributeUserNameAttribute = new KeyValueSubjectAttribute(ShoppingCartResourceConstants.SUBJECT_ATTRIBUTE_USER_NAME,
				subjectAttributeUserNameValue, ShoppingCartResourceConstants.METADATA);

		when(subject.getAttributes())
				.thenReturn(Arrays.asList(subjectAttributeUserIdAttribute, subjectAttributeUserEmailAttribute, subjectAttributeUserNameAttribute));
	}

	private ShoppingCart mockExistingCart() {
		ShoppingCart mockCart = mock(ShoppingCart.class);
		when(shoppingCartService.findByGuid(CART_GUID))
				.thenReturn(mockCart);
		return mockCart;
	}


}
