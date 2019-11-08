/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.definition.offers.OfferForItemRelationship;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Offer for an item Relationship link.
 */
public class OfferForItemRelationshipImpl implements OfferForItemRelationship.LinkTo {

	private final ItemIdentifier itemIdentifier;
	private final LinksRepository<ItemIdentifier, OfferIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param itemIdentifier item identifier
	 * @param repository     repository
	 */
	@Inject
	public OfferForItemRelationshipImpl(
			@RequestIdentifier final ItemIdentifier itemIdentifier,
			@ResourceRepository final LinksRepository<ItemIdentifier, OfferIdentifier> repository) {
		this.itemIdentifier = itemIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<OfferIdentifier> onLinkTo() {
		return repository.getElements(itemIdentifier);
	}
}
