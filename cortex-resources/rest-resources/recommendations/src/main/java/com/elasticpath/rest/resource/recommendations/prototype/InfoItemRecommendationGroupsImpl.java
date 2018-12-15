/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.recommendations.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.recommendations.ItemRecommendationGroupsResource;
import com.elasticpath.rest.resource.recommendations.constants.RecommendationsResourceConstants;

/**
 * Item recommendation groups prototype for Info operation.
 */
public class InfoItemRecommendationGroupsImpl implements ItemRecommendationGroupsResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(RecommendationsResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
