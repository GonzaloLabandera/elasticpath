/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.carts.CartDescriptorIdentifier;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.CartToDescriptorRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Cart to descriptor relationship Impl.
 */
public class CartToDescriptorRelationshipImpl implements CartToDescriptorRelationship.LinkTo {

	private final CartIdentifier identifier;

	/**
	 * Constructor.
	 * @param identifier the cart identifier.
	 */
	@Inject
	public CartToDescriptorRelationshipImpl(@RequestIdentifier final CartIdentifier identifier) {

		this.identifier = identifier;
	}

	@Override
	public Observable<CartDescriptorIdentifier> onLinkTo() {
		return Observable.just(CartDescriptorIdentifier.builder()
				.withCart(identifier)
				.build());
	}

}
