/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offers.CodeForOfferIdentifier;
import com.elasticpath.rest.definition.offers.CodeForOfferRelationship;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Code for offer relationship implementation.
 */
public class CodeForOfferRelationshipImpl implements CodeForOfferRelationship.LinkTo {


	private final OfferIdentifier offerIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier offerIdentifier
	 */
	@Inject
	public CodeForOfferRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier) {
		this.offerIdentifier = offerIdentifier;
	}

	@Override
	public Observable<CodeForOfferIdentifier> onLinkTo() {
		return Observable.just(CodeForOfferIdentifier.builder()
				.withOffer(offerIdentifier)
				.build());
	}
}
