/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.recommendations.relationship.groups;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.domain.catalog.ProductAssociationType;
import com.elasticpath.rest.definition.recommendations.ItemRecommendationGroupIdentifier;
import com.elasticpath.rest.definition.recommendations.ItemRecommendationGroupsIdentifier;
import com.elasticpath.rest.definition.recommendations.RecommendationsForItemRecommendationRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Link from recommendations groups to a specific group.
 */
public class RecommendationsForItemRecommendationRelationshipImpl implements RecommendationsForItemRecommendationRelationship.LinkTo {

	private final ItemRecommendationGroupsIdentifier groupsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param groupsIdentifier groups identifier
	 */
	@Inject
	public RecommendationsForItemRecommendationRelationshipImpl(@RequestIdentifier final ItemRecommendationGroupsIdentifier groupsIdentifier) {
		this.groupsIdentifier = groupsIdentifier;
	}

	@Override
	public Observable<ItemRecommendationGroupIdentifier> onLinkTo() {
		return Observable.just(ItemRecommendationGroupIdentifier.builder()
				.withItemRecommendationGroups(groupsIdentifier)
				.withGroupId(StringIdentifier.of(ProductAssociationType.RECOMMENDATION.getName()))
				.build());
	}
}
