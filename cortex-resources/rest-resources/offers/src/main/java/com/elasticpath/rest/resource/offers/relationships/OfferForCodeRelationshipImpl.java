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
 * Offer for code relationship implementation.
 */
public class OfferForCodeRelationshipImpl implements CodeForOfferRelationship.LinkFrom {


	private final CodeForOfferIdentifier codeForOfferIdentifier;

	/**
	 * Constructor.
	 *
	 * @param codeForOfferIdentifier codeForOfferIdentifier
	 */
	@Inject
	public OfferForCodeRelationshipImpl(@RequestIdentifier final CodeForOfferIdentifier codeForOfferIdentifier) {
		this.codeForOfferIdentifier = codeForOfferIdentifier;
	}

	@Override
	public Observable<OfferIdentifier> onLinkFrom() {
		return Observable.just(codeForOfferIdentifier.getOffer());
	}
}
