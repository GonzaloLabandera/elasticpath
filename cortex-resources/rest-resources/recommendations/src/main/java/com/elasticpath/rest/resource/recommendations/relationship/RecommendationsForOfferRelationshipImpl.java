/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.recommendations.relationship;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupsIdentifier;
import com.elasticpath.rest.definition.recommendations.RecommendationsForOfferRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from Offer to the its recommendation.
 */
public class RecommendationsForOfferRelationshipImpl implements RecommendationsForOfferRelationship.LinkTo {

	private final OfferIdentifier offerIdentifier;

	/**
	 * Constructor.
	 *
	 * @param offerIdentifier offer identifier
	 */
	@Inject
	public RecommendationsForOfferRelationshipImpl(@RequestIdentifier final OfferIdentifier offerIdentifier) {
		this.offerIdentifier = offerIdentifier;
	}

	@Override
	public Observable<OfferRecommendationGroupsIdentifier> onLinkTo() {
		return Observable.just(OfferRecommendationGroupsIdentifier.builder()
				.withOffer(offerIdentifier)
				.build());
	}
}
