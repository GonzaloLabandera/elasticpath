/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.recommendations.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupIdentifier;
import com.elasticpath.rest.definition.recommendations.OfferRecommendationGroupResource;
import com.elasticpath.rest.definition.recommendations.PaginatedOffersRecommendationsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;

/**
 * Read operation for the offer recommendation group.
 */
public class ReadOfferRecommendationGroupImpl implements OfferRecommendationGroupResource.Read {

	private static final Integer FIRST_PAGE = 1;
	private final OfferRecommendationGroupIdentifier groupIdentifier;

	/**
	 * Constructor.
	 *
	 * @param groupIdentifier group identifier
	 */
	@Inject
	public ReadOfferRecommendationGroupImpl(@RequestIdentifier final OfferRecommendationGroupIdentifier groupIdentifier) {
		this.groupIdentifier = groupIdentifier;
	}

	@Override
	public Single<PaginatedOffersRecommendationsIdentifier> onRead() {
		return Single.just(PaginatedOffersRecommendationsIdentifier.builder()
				.withOfferRecommendationGroup(groupIdentifier)
				.withRecommendationPageId(IntegerIdentifier.of(FIRST_PAGE))
				.build());
	}
}
