/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferComponentsForOfferRelationship;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * OfferComponents for Offer Relationship link
 */
public class OfferComponentsForOfferRelationshipImpl implements OfferComponentsForOfferRelationship.LinkTo {

	private final OfferIdentifier offerIdentifier;
	private final LinksRepository<OfferIdentifier, OfferComponentsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier OfferIdentifier
	 * @param repository      Offer Component Links Repository
	 */
	@Inject
	public OfferComponentsForOfferRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier,
												   @ResourceRepository final LinksRepository<OfferIdentifier, OfferComponentsIdentifier> repository) {
		this.offerIdentifier = offerIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<OfferComponentsIdentifier> onLinkTo() {
		return repository.getElements(offerIdentifier);

	}
}
