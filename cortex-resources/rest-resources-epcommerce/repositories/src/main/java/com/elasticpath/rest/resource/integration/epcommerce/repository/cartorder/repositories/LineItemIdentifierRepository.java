/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import io.reactivex.Completable;
import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.carts.LineItemsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Repository for line items in a cart.
 *
 * @param <I>  the identifier type
 * @param <LI> the linked identifier type
 */
@Component
public class LineItemIdentifierRepository<I extends CartIdentifier, LI extends LineItemIdentifier>
		implements LinksRepository<CartIdentifier, LineItemIdentifier> {

	private ShoppingCartRepository shoppingCartRepository;

	@Override
	public Observable<LineItemIdentifier> getElements(final CartIdentifier cartIdentifier) {
		return shoppingCartRepository.getDefaultShoppingCart()
				.flatMapObservable(shoppingCart -> Observable.fromIterable(shoppingCart.getAllItems()))
				.map(shoppingItem -> buildLineItemIdentifier(cartIdentifier, shoppingItem));
	}

	/**
	 * Build line item identifier.
	 *
	 * @param cartIdentifier cartIdentifier
	 * @param shoppingItem   shoppingItem
	 * @return line item identifier
	 */
	protected LineItemIdentifier buildLineItemIdentifier(final CartIdentifier cartIdentifier, final ShoppingItem shoppingItem) {
		return LineItemIdentifier.builder()
				.withLineItems(LineItemsIdentifier.builder()
						.withCart(cartIdentifier)
						.build())
				.withLineItemId(StringIdentifier.of(shoppingItem.getGuid()))
				.build();
	}

	@Override
	public Completable deleteAll(final CartIdentifier cartIdentifier) {
		return shoppingCartRepository.removeAllItemsFromCart();
	}

	@Reference
	public void setShoppingCartRepository(final ShoppingCartRepository shoppingCartRepository) {
		this.shoppingCartRepository = shoppingCartRepository;
	}

}
