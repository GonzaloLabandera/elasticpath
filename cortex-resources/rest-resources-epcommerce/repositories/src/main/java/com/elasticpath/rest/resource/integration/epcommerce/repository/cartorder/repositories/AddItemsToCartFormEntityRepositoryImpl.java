/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormEntity;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.ItemEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.validators.AddItemsToCartValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Repository for adding items to cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AddItemsToCartFormEntityRepositoryImpl<E extends AddItemsToCartFormEntity, I extends CartIdentifier>
		implements Repository<AddItemsToCartFormEntity, CartIdentifier> {

	private ShoppingCartRepository shoppingCartRepository;

	private AddItemsToCartValidator addItemsToCartValidator;

	private ReactiveAdapter reactiveAdapter;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<SubmitResult<CartIdentifier>> submit(final AddItemsToCartFormEntity entity, final IdentifierPart<String> scope) {


		String cartGuid;
		Optional<ResourceIdentifier> resourceIdentifier = resourceOperationContext.getResourceIdentifier();
		if (resourceIdentifier.isPresent()) {
			cartGuid = ((AddItemsToCartFormIdentifier) resourceIdentifier.get())
					.getCart().getCartId().getValue();
		} else {
			cartGuid = shoppingCartRepository.getDefaultShoppingCartGuid().blockingGet();
		}
		return addItemsToCartValidator.validate(entity, scope.getValue())
				.andThen(shoppingCartRepository.getShoppingCart(cartGuid))
				.flatMap(shoppingCart -> reactiveAdapter.fromServiceAsSingle(() -> createShoppingItemDtos(entity))
						.flatMap(shoppingItemDtos -> shoppingCartRepository.addItemsToCart(shoppingCart, shoppingItemDtos)))
				.map(shoppingCart -> buildResult(shoppingCart, scope));
	}

	/**
	 * Create a list of ShoppingItemDto from the given form entity.
	 *
	 * @param entity entity
	 * @return list of ShoppingItemDto
	 */
	protected List<ShoppingItemDto> createShoppingItemDtos(final AddItemsToCartFormEntity entity) {
		return entity.getItems().stream()
				.map(this::createShoppingItemDto)
				.collect(Collectors.toList());
	}

	/**
	 * Create a ShoppingItemDto from the given item entity.
	 *
	 * @param itemEntity itemEntity
	 * @return ShoppingItemDto
	 */
	protected ShoppingItemDto createShoppingItemDto(final ItemEntity itemEntity) {
		return shoppingCartRepository.getShoppingItemDto(itemEntity.getCode(), itemEntity.getQuantity(), Collections.emptyMap());
	}

	/**
	 * Build the form submit result.
	 *
	 * @param shoppingCart shoppingCart
	 * @param scope        scope
	 * @return SubmitResult
	 */
	protected SubmitResult<CartIdentifier> buildResult(final ShoppingCart shoppingCart, final IdentifierPart<String> scope) {
		return SubmitResult.<CartIdentifier>builder()
				.withStatus(SubmitStatus.CREATED)
				.withIdentifier(CartIdentifier.builder()
						.withCartId(StringIdentifier.of(shoppingCart.getGuid()))
						.withCarts(CartsIdentifier.builder()
								.withScope(scope)
								.build())
						.build())
				.build();
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	@Reference
	public void setAddItemsToCartValidator(final AddItemsToCartValidator addItemsToCartValidator) {
		this.addItemsToCartValidator = addItemsToCartValidator;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
