/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.prices.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.prices.PriceForCartLineItemIdentifier;
import com.elasticpath.rest.definition.prices.PriceForCartLineItemRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Adds a link from cart lineitem to price.
 */
public class CartLineItemToPriceRelationshipImpl implements PriceForCartLineItemRelationship.LinkTo {

	private final LineItemIdentifier lineItemIdentifier;
	private final LinksRepository<LineItemIdentifier, PriceForCartLineItemIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param lineItemIdentifier line item identifier
	 * @param repository repository
	 */
	@Inject
	public CartLineItemToPriceRelationshipImpl(@RequestIdentifier final LineItemIdentifier lineItemIdentifier,
				@ResourceRepository final LinksRepository<LineItemIdentifier, PriceForCartLineItemIdentifier> repository) {
		this.lineItemIdentifier = lineItemIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<PriceForCartLineItemIdentifier> onLinkTo() {
		return repository.getElements(lineItemIdentifier);
	}
}
