/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Repository for cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CartDescriptorLinksRepositoryImpl<E extends AddToCartFormsIdentifier,
		I extends CartDescriptorIdentifier>
implements LinksRepository<AddToCartFormsIdentifier, CartDescriptorIdentifier> {

	private ShoppingCartRepository shoppingCartRepository;
	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<CartDescriptorIdentifier> getElements(final AddToCartFormsIdentifier identifier) {
		String customerGuid = resourceOperationContext.getUserIdentifier();
		String accountSharedId = SubjectUtil.getAccountSharedId(resourceOperationContext.getSubject());

		Observable<String> allCarts = shoppingCartRepository.findAllCarts(customerGuid, accountSharedId,
				identifier.getCarts().getScope().getValue());

		return allCarts.map(cartGuid -> CartDescriptorIdentifier
				.builder()
				.withCart(CartIdentifier.builder()
						.withCartId(StringIdentifier.of(cartGuid))
						.withCarts(identifier.getCarts())
						.build())
				.build());
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
