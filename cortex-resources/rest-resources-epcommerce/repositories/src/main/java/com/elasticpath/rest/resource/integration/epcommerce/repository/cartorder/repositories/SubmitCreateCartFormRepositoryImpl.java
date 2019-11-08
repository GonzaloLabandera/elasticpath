/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Repository for cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class SubmitCreateCartFormRepositoryImpl<E extends CreateCartFormEntity, I extends CartIdentifier>
		implements Repository<CreateCartFormEntity, CartIdentifier> {

	private ShoppingCartRepository shoppingCartRepository;


	@Override
	public Single<SubmitResult<CartIdentifier>> submit(final CreateCartFormEntity entity, final IdentifierPart<String> scope) {

		return shoppingCartRepository.createCart(entity.getDescriptor().getDynamicProperties(), scope.getValue())
				.map(shoppingCart -> CartIdentifier.builder()
				.withCarts(CartsIdentifier.builder()
						.withScope(scope).build())
				.withCartId(StringIdentifier.of(shoppingCart.getGuid()))
				.build())
				.map(cartIdentifier -> SubmitResult.<CartIdentifier>builder()
						.withIdentifier(cartIdentifier)
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}


}
