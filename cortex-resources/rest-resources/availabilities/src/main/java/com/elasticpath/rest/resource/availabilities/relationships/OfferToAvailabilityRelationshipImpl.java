/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.availabilities.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.availabilities.AvailabilityForOfferIdentifier;
import com.elasticpath.rest.definition.availabilities.OfferToAvailabilityRelationship;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Offer to availability link.
 */
public class OfferToAvailabilityRelationshipImpl implements OfferToAvailabilityRelationship.LinkTo {

	private final OfferIdentifier offerIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier offerIdentifier
	 */
	@Inject
	public OfferToAvailabilityRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier) {
		this.offerIdentifier = offerIdentifier;
	}

	@Override
	public Observable<AvailabilityForOfferIdentifier> onLinkTo() {
		return Observable.just(AvailabilityForOfferIdentifier.builder()
				.withOffer(offerIdentifier)
				.build());
	}
}
