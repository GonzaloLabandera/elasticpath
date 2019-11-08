/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.carts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.carts.AddToSpecificCartFormIdentifier;
import com.elasticpath.rest.definition.carts.AddToSpecificCartFormToCartRelationship;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Add to specific cart form to cart relationship.
 */
public class AddToSpecificCartFormToMulticartRelationshipImpl implements AddToSpecificCartFormToCartRelationship.LinkTo {

	private final AddToSpecificCartFormIdentifier identifier;

	/**
	 * Constructor.
	 * @param identifier the item identifier.
	 */
	@Inject
	public AddToSpecificCartFormToMulticartRelationshipImpl(@RequestIdentifier final AddToSpecificCartFormIdentifier identifier) {

		this.identifier = identifier;
	}

	@Override
	public Observable<CartIdentifier> onLinkTo() {
		return Observable.just(identifier.getCart());
	}

}
