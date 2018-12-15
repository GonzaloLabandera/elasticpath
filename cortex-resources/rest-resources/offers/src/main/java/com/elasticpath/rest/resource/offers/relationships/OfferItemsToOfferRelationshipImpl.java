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
 * Offer for OfferItems Relationship link
 */
public class OfferItemsToOfferRelationshipImpl implements OfferItemsForOfferRelationship.LinkFrom {

	private final OfferItemsIdentifier offerItemsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerItemsIdentifier OfferIdentifier
	 */
	@Inject
	public OfferItemsToOfferRelationshipImpl(@RequestIdentifier final OfferItemsIdentifier offerItemsIdentifier) {
		this.offerItemsIdentifier = offerItemsIdentifier;
	}

	@Override
	public Observable<OfferIdentifier> onLinkFrom() {
		return Observable.just(offerItemsIdentifier.getOffer());
	}
}
