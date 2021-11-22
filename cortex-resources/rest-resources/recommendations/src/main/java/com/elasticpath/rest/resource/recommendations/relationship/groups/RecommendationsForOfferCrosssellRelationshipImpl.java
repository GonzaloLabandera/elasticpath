/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.recommendations.relationship.groups;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupIdentifier;
import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupsIdentifier;
import com.elasticpath.rest.definition.recommendations.RecommendationsForOfferCrosssellRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Link from recommendations groups to a specific group.
 */
public class RecommendationsForOfferCrosssellRelationshipImpl implements RecommendationsForOfferCrosssellRelationship.LinkTo {

	private final OfferRecommendationGroupsIdentifier groupsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param groupsIdentifier groups identifier
	 */
	@Inject
	public RecommendationsForOfferCrosssellRelationshipImpl(@RequestIdentifier final OfferRecommendationGroupsIdentifier groupsIdentifier) {
		this.groupsIdentifier = groupsIdentifier;
	}

	@Override
	public Observable<OfferRecommendationGroupIdentifier> onLinkTo() {
		return Observable.just(OfferRecommendationGroupIdentifier.builder()
				.withOfferRecommendationGroups(groupsIdentifier)
				.withRecommendationGroupId(StringIdentifier.of(ProductAssociationType.CROSS_SELL.getName()))
				.build());
	}
}
