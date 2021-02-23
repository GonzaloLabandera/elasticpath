/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.prices.PriceForCartLineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Link repository that adds price link if the item has a price.
 *
 * @param <I>  extends LineItemIdentifier
 * @param <LI> extends PriceForCartLineItemIdentifier
 */
@Component
public class CartLineItemToPriceLinksRepositoryImpl<I extends LineItemIdentifier, LI extends PriceForCartLineItemIdentifier>
		implements LinksRepository<LineItemIdentifier, PriceForCartLineItemIdentifier> {

	private CartTotalsCalculator cartTotalsCalculator;

	@Override
	public Observable<PriceForCartLineItemIdentifier> getElements(final LineItemIdentifier lineItemIdentifier) {
		final CartIdentifier cartIdentifier = lineItemIdentifier.getLineItems().getCart();

		String lineItemId = lineItemIdentifier.getLineItemId().getValue();
		String cartId = cartIdentifier.getCartId().getValue();
		String scope = cartIdentifier.getCarts().getScope().getValue();

		if (cartTotalsCalculator.shoppingItemHasPrice(scope, cartId, lineItemId)) {
			return Observable.just(PriceForCartLineItemIdentifier.builder()
					.withLineItem(lineItemIdentifier)
					.build());
		}
		return Observable.empty();
	}

	@Reference
	public void setCartTotalsCalculator(final CartTotalsCalculator cartTotalsCalculator) {
		this.cartTotalsCalculator = cartTotalsCalculator;
	}
}
