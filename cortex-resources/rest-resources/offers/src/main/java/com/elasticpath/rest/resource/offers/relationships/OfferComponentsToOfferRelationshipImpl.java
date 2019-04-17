/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offers.OfferComponentsForOfferRelationship;
import com.elasticpath.rest.definition.offers.OfferComponentsIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * OfferComponents to Offer Relationship link.
 */
public class OfferComponentsToOfferRelationshipImpl implements OfferComponentsForOfferRelationship.LinkFrom {

	private final OfferComponentsIdentifier offerComponentsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerComponentsIdentifier offer components identifier
	 */
	@Inject
	public OfferComponentsToOfferRelationshipImpl(@RequestIdentifier final OfferComponentsIdentifier offerComponentsIdentifier) {
		this.offerComponentsIdentifier = offerComponentsIdentifier;
	}

	@Override
	public Observable<OfferIdentifier> onLinkFrom() {
		return Observable.just(offerComponentsIdentifier.getOffer());
	}
}
