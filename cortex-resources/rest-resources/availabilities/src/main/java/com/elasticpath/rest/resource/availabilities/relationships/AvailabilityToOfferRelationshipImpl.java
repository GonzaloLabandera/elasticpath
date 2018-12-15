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
 * Availability to offer link.
 */
public class AvailabilityToOfferRelationshipImpl implements OfferToAvailabilityRelationship.LinkFrom {

	private final AvailabilityForOfferIdentifier availabilityForOfferIdentifier;

	/**
	 * Constructor.
	 *
	 * @param availabilityForOfferIdentifier availabilityForOfferIdentifier
	 */
	@Inject
	public AvailabilityToOfferRelationshipImpl(@RequestIdentifier final AvailabilityForOfferIdentifier availabilityForOfferIdentifier) {
		this.availabilityForOfferIdentifier = availabilityForOfferIdentifier;
	}

	@Override
	public Observable<OfferIdentifier> onLinkFrom() {
		return Observable.just(availabilityForOfferIdentifier.getOffer());
	}
}
