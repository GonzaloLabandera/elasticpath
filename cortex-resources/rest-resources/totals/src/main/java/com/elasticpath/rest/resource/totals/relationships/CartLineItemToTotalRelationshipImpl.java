/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.totals.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.totals.CartLineItemTotalIdentifier;
import com.elasticpath.rest.definition.totals.TotalForCartLineItemRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Cart line item to cart line item total link.
 */
public class CartLineItemToTotalRelationshipImpl implements TotalForCartLineItemRelationship.LinkTo {

	private final LineItemIdentifier lineItemIdentifier;
	private final LinksRepository<LineItemIdentifier, CartLineItemTotalIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param lineItemIdentifier line item identifier
	 * @param repository repository
	 */
	@Inject
	public CartLineItemToTotalRelationshipImpl(@RequestIdentifier final LineItemIdentifier lineItemIdentifier,
				@ResourceRepository final LinksRepository<LineItemIdentifier, CartLineItemTotalIdentifier> repository) {
		this.lineItemIdentifier = lineItemIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<CartLineItemTotalIdentifier> onLinkTo() {
		return repository.getElements(lineItemIdentifier);
	}
}
