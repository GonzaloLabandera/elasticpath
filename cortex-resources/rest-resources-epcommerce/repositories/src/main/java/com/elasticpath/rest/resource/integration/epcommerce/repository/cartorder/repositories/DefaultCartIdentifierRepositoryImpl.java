/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.AliasRepository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.DefaultCartIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Repository for Default Cart Identifiers.
 *
 * @param <AI>  the Alias identifier type
 * @param <I> the identifier type
 */
@Component
public class DefaultCartIdentifierRepositoryImpl<AI extends DefaultCartIdentifier, I extends CartIdentifier>
		implements AliasRepository<DefaultCartIdentifier, CartIdentifier> {

	private ShoppingCartRepository shoppingCartRepository;

	@Override
	public Single<CartIdentifier> resolve(final DefaultCartIdentifier defaultCartIdentifier) {
		return shoppingCartRepository.getDefaultShoppingCart()
				.flatMap(cart -> Single.just(CartIdentifier.builder()
						.withCartId(StringIdentifier.of(cart.getGuid()))
						.withScope(defaultCartIdentifier.getScope())
						.build()));
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}
}


