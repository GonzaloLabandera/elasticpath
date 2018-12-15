/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.prices.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.prices.OfferPriceRangeIdentifier;
import com.elasticpath.rest.definition.prices.PriceRangeForOfferRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Adds a link from price range to offer.
 */
public class PriceRangeToOfferRelationshipImpl implements PriceRangeForOfferRelationship.LinkFrom {

	private final OfferPriceRangeIdentifier offerPriceRangeIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerPriceRangeIdentifier offerPriceRangeIdentifier
	 */
	@Inject
	public PriceRangeToOfferRelationshipImpl(@RequestIdentifier final OfferPriceRangeIdentifier offerPriceRangeIdentifier) {
		this.offerPriceRangeIdentifier = offerPriceRangeIdentifier;
	}

	@Override
	public Observable<OfferIdentifier> onLinkFrom() {
		return Observable.just(offerPriceRangeIdentifier.getOffer());
	}
}