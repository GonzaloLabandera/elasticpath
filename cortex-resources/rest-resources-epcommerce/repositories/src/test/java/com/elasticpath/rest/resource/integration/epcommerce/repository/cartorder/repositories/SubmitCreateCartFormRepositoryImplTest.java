/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Test for {@link SubmitCreateCartFormRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SubmitCreateCartFormRepositoryImplTest {

	@InjectMocks
	private SubmitCreateCartFormRepositoryImpl<CreateCartFormEntity, CartIdentifier> repository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;


	@Before
	public void setUp() {
		repository.setShoppingCartRepository(shoppingCartRepository);
	}

	@Test
	public void testSubmit() {

		CreateCartFormEntity entity = CreateCartFormEntity.builder()
				.withDescriptor(CartDescriptorEntity.builder()
						.addingProperty("K", "V")
						.build())
				.build();

		ShoppingCart shoppingCart = mock(ShoppingCart.class);
		when(shoppingCart.getGuid()).thenReturn(CART_GUID);
		when(shoppingCartRepository.createCart(anyMap(), eq(SCOPE))).thenReturn(Single.just(shoppingCart));
		repository.submit(entity, SCOPE_IDENTIFIER_PART).test().assertNoErrors()
		.assertValueCount(1);
	}

}