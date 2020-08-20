/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.structured.EpStructureErrorMessageException;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.CartData;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Test for {@link CartDescriptorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartDescriptorRepositoryImplTest {
	private static final String TEST_NAME = "test-name";
	private static final String TEST_VALUE = "test-value";
	@InjectMocks
	private CartDescriptorRepositoryImpl<CartDescriptorEntity, CartDescriptorIdentifier> repository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private ShoppingCartService shoppingCartService;

	@Mock
	MultiCartResolutionStrategyHolder multiCartResolutionStrategyHolder;

	@Mock
	MultiCartResolutionStrategy strategy;

	@Mock
	ResourceOperationContext resourceOperationContext;

	@Mock
	ExceptionTransformer exceptionTransformer;

	@Mock
	private Subject subject;


	@Before
	public void setUp() {
		when(multiCartResolutionStrategyHolder.getStrategies()).thenReturn(Collections.singletonList(strategy));

	}

	@Test
	public void testFindOneWithIdentifierData() {
		Map<String, CartData> cartDescriptors = new HashMap<>();
		CartData value = mock(CartData.class);
		when(value.getKey()).thenReturn(TEST_NAME);
		when(value.getValue()).thenReturn(TEST_VALUE);

		cartDescriptors.put(TEST_NAME, value);

		when(shoppingCartRepository.getCartDescriptors(CART_GUID))
				.thenReturn(cartDescriptors);

		CartDescriptorIdentifier identifier = mock(CartDescriptorIdentifier.class);
		when(identifier.getCart()).thenReturn(CartIdentifier.builder()
				.withCartId(StringIdentifier.of(CART_GUID))
				.withCarts(CartsIdentifier.builder().withScope(SCOPE_IDENTIFIER_PART).build())
				.build());

		repository.findOne(identifier)
				.test()
				.assertNoErrors()
				.assertValue(entity -> entity.getDynamicProperties().containsKey(TEST_NAME))
				.assertValue(entity -> entity.getDynamicProperties().containsValue(TEST_VALUE));

	}

	@Test
	public void testUpdate() {
		CartDescriptorIdentifier identifier = mock(CartDescriptorIdentifier.class);
		when(identifier.getCart()).thenReturn(CartIdentifier.builder()
				.withCartId(StringIdentifier.of(CART_GUID))
				.withCarts(CartsIdentifier.builder().withScope(SCOPE_IDENTIFIER_PART).build())
				.build());

		CartDescriptorEntity entity = mock(CartDescriptorEntity.class);
		ShoppingCart cart = mock(ShoppingCart.class);

		when(shoppingCartService.findByGuid(CART_GUID)).thenReturn(cart);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);

		repository.update(entity, identifier)
				.test()
				.assertNoErrors();
		verify(strategy, times(1)).validateCreate(cart);
		verify(shoppingCartService, times(1)).saveOrUpdate(cart);
	}

	@Test
	public void testUpdateWhenValidationFails() {
		CartDescriptorIdentifier identifier = mock(CartDescriptorIdentifier.class);
		when(identifier.getCart()).thenReturn(CartIdentifier.builder()
				.withCartId(StringIdentifier.of(CART_GUID))
				.withCarts(CartsIdentifier.builder().withScope(SCOPE_IDENTIFIER_PART).build())
				.build());

		CartDescriptorEntity entity = mock(CartDescriptorEntity.class);
		ShoppingCart cart = mock(ShoppingCart.class);

		when(shoppingCartService.findByGuid(CART_GUID)).thenReturn(cart);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(strategy.isApplicable(subject)).thenReturn(true);
		doThrow(new EpStructureErrorMessageException("err", null)).when(strategy).validateCreate(cart);
		when(exceptionTransformer.getResourceOperationFailure(any(EpValidationException.class)))
				.thenReturn(ResourceOperationFailure.badRequestBody());

		repository.update(entity, identifier)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

}