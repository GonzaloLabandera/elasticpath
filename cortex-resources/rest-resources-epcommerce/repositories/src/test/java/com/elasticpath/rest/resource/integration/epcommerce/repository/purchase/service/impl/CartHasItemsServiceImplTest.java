/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Test for the  {@link CartHasItemsServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartHasItemsServiceImplTest {

	private static final String SCOPE = "Store";
	private static final String ORDER_ID = "Order_id";
	private static final String CART_IS_EMPTY = "Shopping cart must not be empty during checkout.";

	@InjectMocks
	private CartHasItemsServiceImpl cartHasItemsService;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private ShoppingCart shoppingCart;

	private OrderIdentifier orderIdentifier;

	@Before
	public void setUp() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
		when(cartOrderRepository.getEnrichedShoppingCartSingle(SCOPE, cartOrder)).thenReturn(Single.just(shoppingCart));

		orderIdentifier = OrderIdentifier.builder()
				.withOrderId(StringIdentifier.of(ORDER_ID))
				.withScope(StringIdentifier.of(SCOPE))
				.build();
	}

	@Test
	public void validateCartHasItemsReturnsStructuredErrorMessageForEmptyCart() {
		when(shoppingCart.getCartItems()).thenReturn(Collections.emptyList());

		cartHasItemsService.validateCartHasItems(orderIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(message -> message.getType().equals(StructuredMessageTypes.ERROR))
				.assertValue(message -> message.getId().equals(StructuredErrorMessageIdConstants.CART_NOT_PURCHASABLE))
				.assertValue(message -> message.getDebugMessage().equals(CART_IS_EMPTY))
				.assertValue(message -> message.getData() == null);
	}

	@Test
	public void validateCartHasItemsReturnsNothingWhenCartIsNotEmpty() {
		final ShoppingItem shoppingItem = mock(ShoppingItem.class);
		when(shoppingCart.getCartItems()).thenReturn(Collections.singletonList(shoppingItem));

		cartHasItemsService.validateCartHasItems(orderIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void isCartEmptyReturnsFalseWhenCartIsNotEmpty() {
		final ShoppingItem shoppingItem = mock(ShoppingItem.class);
		when(shoppingCart.getCartItems()).thenReturn(Collections.singletonList(shoppingItem));

		cartHasItemsService.isCartEmpty(orderIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void isCartEmptyReturnsTrueWhenCartIsEmpty() {
		when(shoppingCart.getCartItems()).thenReturn(Collections.emptyList());

		cartHasItemsService.isCartEmpty(orderIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}
}