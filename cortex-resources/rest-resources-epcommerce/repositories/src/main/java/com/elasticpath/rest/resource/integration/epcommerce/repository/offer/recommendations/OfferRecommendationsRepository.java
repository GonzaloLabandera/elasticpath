/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.recommendations;

import io.reactivex.Single;

import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;

/**
 * Recommendation repository for offers.
 */
public interface OfferRecommendationsRepository {

	/**
	 * Get a collection of recommendation groups for an offer.
	 *
	 * @param store               the store
	 * @param sourceProductCode   the source product for the recommendations
	 * @param recommendationGroup the name of the recommendation group
	 * @param pageNumber          current page number
	 * @return Collection of paginated recommendation groups for an offer.
	 */
	Single<PaginatedResult> getRecommendedOffersFromGroup(Store store, String sourceProductCode, String recommendationGroup, int pageNumber);

}
