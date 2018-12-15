/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offerdefinitions.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionForOfferRelationship;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Offer to offer definition link.
 */
public class OfferToOfferDefinitionRelationshipImpl implements OfferDefinitionForOfferRelationship.LinkTo {

	private final OfferIdentifier offerIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier offerIdentifier
	 */
	@Inject
	public OfferToOfferDefinitionRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier) {
		this.offerIdentifier = offerIdentifier;
	}

	@Override
	public Observable<OfferDefinitionIdentifier> onLinkTo() {
		return Observable.just(OfferDefinitionIdentifier.builder()
				.withOfferId(offerIdentifier.getOfferId())
				.withScope(offerIdentifier.getScope())
				.build());
	}
}
