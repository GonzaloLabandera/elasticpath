/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offers.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferItemsIdentifier;
import com.elasticpath.rest.definition.offers.OfferItemsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read items for an offer.
 */
public class OfferItemsPrototype implements OfferItemsResource.Read {

	private final OfferItemsIdentifier offerItemsIdentifier;
	private final LinksRepository<OfferItemsIdentifier, ItemIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param repository repository
	 * @param offerItemsIdentifier offerItemsIdentifier
	 */
	@Inject
	public OfferItemsPrototype(@ResourceRepository final LinksRepository<OfferItemsIdentifier, ItemIdentifier> repository,
							   @RequestIdentifier final OfferItemsIdentifier offerItemsIdentifier) {
		this.offerItemsIdentifier = offerItemsIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<ItemIdentifier> onRead() {
		return repository.getElements(offerItemsIdentifier);
	}
}
