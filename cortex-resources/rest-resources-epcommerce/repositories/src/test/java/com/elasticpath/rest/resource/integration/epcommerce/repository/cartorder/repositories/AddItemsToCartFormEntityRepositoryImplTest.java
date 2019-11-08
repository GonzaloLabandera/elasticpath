/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.AddItemsToCartValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;

/**
 * Test for {@link AddItemToSpecificCartRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddItemsToCartFormEntityRepositoryImplTest {

	public static final String ITEM_CODE = "CODE";
	@Mock
	private ShoppingCartRepository shoppingCartRepository;

	@Mock
	private AddItemsToCartValidator addItemsToCartValidator;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@InjectMocks
	private final ReactiveAdapter reactiveAdapter = new ReactiveAdapterImpl(mock(ExceptionTransformer.class));

	private AddItemsToCartFormEntityRepositoryImpl<AddItemsToCartFormEntity, CartIdentifier> repository;

	@Before

	public void setUp() {
		repository = new AddItemsToCartFormEntityRepositoryImpl<>();
		repository.setAddItemsToCartValidator(addItemsToCartValidator);
		repository.setReactiveAdapter(reactiveAdapter);
		repository.setResourceOperationContext(resourceOperationContext);
		repository.setShoppingCartRepository(shoppingCartRepository);
	}

	@Test
	public void testSubmit() {


		AddItemsToCartFormEntity entity = mock(AddItemsToCartFormEntity.class);
		AddItemsToCartFormIdentifier identifier = mock(AddItemsToCartFormIdentifier.class);
		CartIdentifier cartIdentifier = mock(CartIdentifier.class);
		ShoppingCart mockCart = mock(ShoppingCart.class);
		ItemEntity item = ItemEntity.builder()
				.withCode(ITEM_CODE)
				.withQuantity(1)
				.build();
		ShoppingItemDto mockShoppingItemDto = mock(ShoppingItemDto.class);

		when(resourceOperationContext.getResourceIdentifier())
				.thenReturn(Optional.of(identifier));
		when(addItemsToCartValidator.validate(entity, SCOPE))
				.thenReturn(Completable.complete());
		when(identifier.getCart())
				.thenReturn(cartIdentifier);
		when(cartIdentifier.getCartId())
				.thenReturn(StringIdentifier.of(CART_ID));
		when(shoppingCartRepository.getShoppingCart(CART_ID))
				.thenReturn(Single.just(mockCart));
		when(entity.getItems()).thenReturn(Collections.singletonList(item));
		when(shoppingCartRepository.getShoppingItemDto(ITEM_CODE, 1, Collections.emptyMap()))
				.thenReturn(mockShoppingItemDto);
		when(shoppingCartRepository.addItemsToCart(mockCart, Collections.singletonList(mockShoppingItemDto)))
				.thenReturn(Single.just(mockCart));

		when(mockCart.getGuid()).thenReturn(CART_ID);


		repository.submit(entity, SCOPE_IDENTIFIER_PART)
				.test()
				.assertNoErrors()
				.assertValue(result -> result.getStatus().equals(SubmitStatus.CREATED));
	}
	@Test
	public void testSubmitWithDefaultCart() {


		AddItemsToCartFormEntity entity = mock(AddItemsToCartFormEntity.class);
		ShoppingCart mockCart = mock(ShoppingCart.class);
		ItemEntity item = ItemEntity.builder()
				.withCode(ITEM_CODE)
				.withQuantity(1)
				.build();
		ShoppingItemDto mockShoppingItemDto = mock(ShoppingItemDto.class);

		when(resourceOperationContext.getResourceIdentifier())
				.thenReturn(Optional.empty());
		when(shoppingCartRepository.getDefaultShoppingCartGuid())
				.thenReturn(Single.just(CART_ID));
		when(addItemsToCartValidator.validate(entity, SCOPE))
				.thenReturn(Completable.complete());
		when(shoppingCartRepository.getShoppingCart(CART_ID))
				.thenReturn(Single.just(mockCart));
		when(entity.getItems()).thenReturn(Collections.singletonList(item));
		when(shoppingCartRepository.getShoppingItemDto(ITEM_CODE, 1, Collections.emptyMap()))
				.thenReturn(mockShoppingItemDto);
		when(shoppingCartRepository.addItemsToCart(mockCart, Collections.singletonList(mockShoppingItemDto)))
				.thenReturn(Single.just(mockCart));

		when(mockCart.getGuid()).thenReturn(CART_ID);


		repository.submit(entity, SCOPE_IDENTIFIER_PART)
				.test()
				.assertNoErrors()
				.assertValue(result -> result.getStatus().equals(SubmitStatus.CREATED));
	}
	@Test
	public void testSubmitFailsValidate() {


		AddItemsToCartFormEntity entity = mock(AddItemsToCartFormEntity.class);
		AddItemsToCartFormIdentifier identifier = mock(AddItemsToCartFormIdentifier.class);
		CartIdentifier cartIdentifier = mock(CartIdentifier.class);
		ShoppingCart mockCart = mock(ShoppingCart.class);

		when(resourceOperationContext.getResourceIdentifier())
				.thenReturn(Optional.of(identifier));
		when(addItemsToCartValidator.validate(entity, SCOPE))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody()));
		when(identifier.getCart())
				.thenReturn(cartIdentifier);
		when(cartIdentifier.getCartId())
				.thenReturn(StringIdentifier.of(CART_ID));
		when(shoppingCartRepository.getShoppingCart(CART_ID))
				.thenReturn(Single.just(mockCart));
		repository.submit(entity, SCOPE_IDENTIFIER_PART)
				.test()
				.assertError(ResourceOperationFailure.badRequestBody());
	}
}