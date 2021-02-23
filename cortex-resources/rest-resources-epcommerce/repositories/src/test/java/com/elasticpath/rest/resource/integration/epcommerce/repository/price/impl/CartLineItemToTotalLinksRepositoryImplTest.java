package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.carts.LineItemsIdentifier;
import com.elasticpath.rest.definition.totals.CartLineItemTotalIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Test for {@link CartLineItemToTotalLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartLineItemToTotalLinksRepositoryImplTest {
	private static final String STORE_CODE = "SCOPE";
	private static final String SHOPPING_CART_GUID = "SHOPPING_CART_GUID";
	private static final String CART_ITEM_GUID = "CART_ITEM_GUID";

	@Mock
	private CartTotalsCalculator cartTotalsCalculator;

	@InjectMocks
	private CartLineItemToTotalLinksRepositoryImpl<LineItemIdentifier, CartLineItemTotalIdentifier> repository;

	@Test
	public void shouldReturnLinkWhenPriceExists() {
		when(cartTotalsCalculator.shoppingItemHasPrice(STORE_CODE, SHOPPING_CART_GUID, CART_ITEM_GUID)).thenReturn(true);

		repository.getElements(getLineItemIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(lineItemIdentifier -> lineItemIdentifier.getLineItem().getLineItemId().equals(StringIdentifier.of(CART_ITEM_GUID)));
	}

	@Test
	public void shouldNotReturnLinkWhenPriceExists() {
		when(cartTotalsCalculator.shoppingItemHasPrice(STORE_CODE, SHOPPING_CART_GUID, CART_ITEM_GUID)).thenReturn(false);

		repository.getElements(getLineItemIdentifier())
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	private LineItemIdentifier getLineItemIdentifier() {
		return LineItemIdentifier.builder()
				.withLineItemId(StringIdentifier.of(CART_ITEM_GUID))
				.withLineItems(LineItemsIdentifier.builder()
						.withCart(CartIdentifier.builder()
								.withCartId(StringIdentifier.of(SHOPPING_CART_GUID))
								.withCarts(CartsIdentifier.builder()
										.withScope(StringIdentifier.of(STORE_CODE))
										.build())
								.build())
						.build())
				.build();
	}

}