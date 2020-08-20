/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_GUID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.Observable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Test for {@link CartDescriptorLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartDescriptorLinksRepositoryImplTest {

	@InjectMocks
	private CartDescriptorLinksRepositoryImpl<AddToCartFormsIdentifier, CartDescriptorIdentifier> repository;

	@Mock
	private ShoppingCartRepository shoppingCartRepository;
	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Test
	public void testGetElements() {

		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(shoppingCartRepository.findAllCarts(USER_ID, null, SCOPE_IDENTIFIER_PART.getValue())).thenReturn(Observable.just(CART_GUID));

		AddToCartFormsIdentifier addToCartsListIdentifier = mock(AddToCartFormsIdentifier.class);
		when(addToCartsListIdentifier.getCarts()).thenReturn(CartsIdentifier.builder().withScope(SCOPE_IDENTIFIER_PART).build());

		repository.getElements(addToCartsListIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(identifier -> identifier.getCart().getCartId().getValue().equals(CART_GUID));

	}

}