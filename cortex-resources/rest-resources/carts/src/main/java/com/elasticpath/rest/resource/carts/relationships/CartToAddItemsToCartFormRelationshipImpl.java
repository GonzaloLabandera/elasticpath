/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.carts.AddItemsToCartFormFromCartRelationship;
import com.elasticpath.rest.definition.carts.AddItemsToCartFormIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Cart to Add Items to Cart Form link.
 */
public class CartToAddItemsToCartFormRelationshipImpl implements AddItemsToCartFormFromCartRelationship.LinkTo {

	private final CartIdentifier cartIdentifier;

	/**
	 * Constructor.
	 *
	 * @param cartIdentifier cartIdentifier
	 */
	@Inject
	public CartToAddItemsToCartFormRelationshipImpl(@RequestIdentifier final CartIdentifier cartIdentifier) {
		this.cartIdentifier = cartIdentifier;
	}

	@Override
	public Observable<AddItemsToCartFormIdentifier> onLinkTo() {
		return Observable.just(AddItemsToCartFormIdentifier
				.builder()
				.withCart(cartIdentifier)
				.build());
	}
}
