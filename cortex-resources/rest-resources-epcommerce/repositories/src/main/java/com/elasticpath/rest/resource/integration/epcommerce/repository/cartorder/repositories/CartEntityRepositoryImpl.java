/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.Collections;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.definition.carts.CartEntity;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Repository for cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CartEntityRepositoryImpl<E extends CartEntity, I extends CartIdentifier>
		implements Repository<CartEntity, CartIdentifier> {

	private static final String DELETE_DEFAULT_CART_EXCEPTION_MESSAGE = "Default Cart cannot be removed.";

	private ShoppingCartRepository shoppingCartRepository;

	private ConversionService conversionService;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<CartEntity> findOne(final CartIdentifier cartIdentifier) {
		return shoppingCartRepository.getShoppingCart(cartIdentifier.getCartId().getValue())
				.map(shoppingCart -> conversionService.convert(shoppingCart, CartEntity.class));
	}

	@Override
	public Observable<CartIdentifier> findAll(final IdentifierPart<String> scope) {
		String customerGuid = resourceOperationContext.getUserIdentifier();
		String accountSharedId = SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject());

		return shoppingCartRepository.findAllCarts(customerGuid, accountSharedId, scope.getValue())
				.map(cartId -> CartIdentifier.builder()
						.withCartId(StringIdentifier.of(cartId))
						.withCarts(CartsIdentifier.builder().withScope(scope).build())
						.build());
	}

	@Override
	public Completable delete(final CartIdentifier identifier) {
		String cartGuid = identifier.getCartId().getValue();

		return shoppingCartRepository.getDefaultShoppingCartGuid()
				.filter(defaultCartGuid -> !defaultCartGuid.equals(cartGuid))
				.switchIfEmpty(Single.error(ResourceOperationFailure.stateFailure(DELETE_DEFAULT_CART_EXCEPTION_MESSAGE,
						Collections.singletonList(Message.builder()
								.withType(StructuredMessageTypes.ERROR)
								.withId("cart.delete.not.permitted")
								.withDebugMessage(DELETE_DEFAULT_CART_EXCEPTION_MESSAGE)
								.build()))))
				.ignoreElement()
				.andThen(removeNamedCartByGuid(cartGuid));
	}

	private Completable removeNamedCartByGuid(final String cartGuid) {
		return shoppingCartRepository.getShoppingCart(cartGuid)
				.flatMapCompletable(shoppingCart -> shoppingCartRepository.removeCart(shoppingCart.getGuid()));
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
