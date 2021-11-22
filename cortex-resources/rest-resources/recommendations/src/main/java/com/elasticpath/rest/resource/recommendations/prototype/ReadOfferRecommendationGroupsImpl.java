/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.recommendations.prototype;

import io.reactivex.Completable;

import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupsResource;

/**
 * Read operation for the offer recommendation groups.
 */
public class ReadOfferRecommendationGroupsImpl implements OfferRecommendationGroupsResource.Read {

	@Override
	public Completable onRead() {
		return Completable.complete();
	}
}