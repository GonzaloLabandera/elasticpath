/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.bridges;

import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;

/**
 * Bridge between Core Entity concepts and Extension Point Entity concepts for product recommendation.
 */
public interface ProductRecommendationXPFBridge {

	/**
	 * Returns paginated list of product codes.
	 *
	 * @param store               the store code
	 * @param sourceProductCode   the source product code
	 * @param recommendationGroup the recommendation group
	 * @param pageNumber          the page number
	 * @param pageSize            the page size
	 * @return product recommendations
	 */
	XPFProductRecommendations getPaginatedResult(Store store, String sourceProductCode, String recommendationGroup, int pageNumber,
												 int pageSize);
}
