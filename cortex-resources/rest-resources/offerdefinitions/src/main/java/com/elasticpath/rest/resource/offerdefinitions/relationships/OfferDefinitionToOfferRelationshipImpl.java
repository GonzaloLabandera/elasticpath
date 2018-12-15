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
 * Offer definition to offer link.
 */
public class OfferDefinitionToOfferRelationshipImpl implements OfferDefinitionForOfferRelationship.LinkFrom {

	private final OfferDefinitionIdentifier offerDefinitionIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerDefinitionIdentifier offerDefinitionIdentifier
	 */
	@Inject
	public OfferDefinitionToOfferRelationshipImpl(@RequestIdentifier final OfferDefinitionIdentifier offerDefinitionIdentifier) {
		this.offerDefinitionIdentifier = offerDefinitionIdentifier;
	}

	@Override
	public Observable<OfferIdentifier> onLinkFrom() {
		return Observable.just(OfferIdentifier.builder()
				.withOfferId(offerDefinitionIdentifier.getOfferId())
				.withScope(offerDefinitionIdentifier.getScope())
				.build());
	}
}
