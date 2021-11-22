/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.recommendations.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupsResource;
import com.elasticpath.rest.resource.recommendations.constants.RecommendationsResourceConstants;

/**
 * Offer recommendation groups prototype for Info operation.
 */
public class InfoOfferRecommendationGroupsImpl implements OfferRecommendationGroupsResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(RecommendationsResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
