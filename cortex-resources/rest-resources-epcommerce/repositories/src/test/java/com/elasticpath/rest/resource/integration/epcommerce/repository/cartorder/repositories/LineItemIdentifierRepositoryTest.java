/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;

import java.util.Collections;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Test for {@link LineItemIdentifierRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LineItemIdentifierRepositoryTest {

	@InjectMocks
	private LineItemIdentifierRepository<CartIdentifier, LineItemIdentifier> repository;
	@Mock
	private ShoppingCartRepository shoppingCartRepository;
	@Mock
	private ShoppingCart cart;

	@Test
	public void getMultipleElements() {
		String guid1 = "guid1";
		String guid2 = "guid2";
		ShoppingItem shoppingItem1 = mock(ShoppingItem.class);
		when(shoppingItem1.getGuid()).thenReturn(guid1);
		ShoppingItem shoppingItem2 = mock(ShoppingItem.class);
		when(shoppingItem2.getGuid()).thenReturn(guid2);

		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(cart));
		when(cart.getAllItems()).thenReturn(ImmutableList.of(shoppingItem1, shoppingItem2));

		repository.getElements(IdentifierTestFactory.buildCartIdentifier(SCOPE, CART_ID))
				.test()
				.assertNoErrors()
				.assertValueCount(2)
				.assertValueAt(0, lineItemIdentifier -> lineItemIdentifier.getLineItemId().getValue().equals(guid1))
				.assertValueAt(1, lineItemIdentifier -> lineItemIdentifier.getLineItemId().getValue().equals(guid2));
	}

	@Test
	public void getNoElements() {
		when(shoppingCartRepository.getDefaultShoppingCart()).thenReturn(Single.just(cart));
		when(cart.getAllItems()).thenReturn(Collections.emptyList());

		repository.getElements(IdentifierTestFactory.buildCartIdentifier(SCOPE, CART_ID))
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}

}