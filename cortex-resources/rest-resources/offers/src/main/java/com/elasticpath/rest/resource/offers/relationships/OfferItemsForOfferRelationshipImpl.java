/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offers.OfferItemsForOfferRelationship;
import com.elasticpath.rest.definition.offers.OfferItemsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * OfferItems for Offer Relationship link.
 */
public class OfferItemsForOfferRelationshipImpl implements OfferItemsForOfferRelationship.LinkTo {

	private final OfferIdentifier offerIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier OfferIdentifier
	 */
	@Inject
	public OfferItemsForOfferRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier) {
		this.offerIdentifier = offerIdentifier;
	}

	@Override
	public Observable<OfferItemsIdentifier> onLinkTo() {
		return Observable.just(OfferItemsIdentifier.builder().withOffer(offerIdentifier).build());
	}
}
