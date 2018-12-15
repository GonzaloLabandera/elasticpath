/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.prices.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.prices.OfferPriceRangeIdentifier;
import com.elasticpath.rest.definition.prices.PriceRangeForOfferRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Adds a link from offer to price range.
 */
public class PriceRangeForOfferRelationshipImpl implements PriceRangeForOfferRelationship.LinkTo {

	private final OfferIdentifier offerIdentifier;
	private final LinksRepository<OfferIdentifier, OfferPriceRangeIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier offerIdentifier
	 * @param repository      repository
	 */
	@Inject
	public PriceRangeForOfferRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier,
											  @ResourceRepository final LinksRepository<OfferIdentifier, OfferPriceRangeIdentifier> repository) {
		this.offerIdentifier = offerIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<OfferPriceRangeIdentifier> onLinkTo() {
		return repository.getElements(offerIdentifier);
	}
}