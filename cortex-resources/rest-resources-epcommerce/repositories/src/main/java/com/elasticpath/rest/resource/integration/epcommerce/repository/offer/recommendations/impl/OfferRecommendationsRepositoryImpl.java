/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.recommendations.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.offer.recommendations.OfferRecommendationsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.xpf.bridges.ProductRecommendationXPFBridge;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendation;
import com.elasticpath.xpf.connectivity.entity.XPFProductRecommendations;

/**
 * Repository for gathering sourceProduct associations.
 */
@Singleton
@Named("offerRecommendationsRepository")
public class OfferRecommendationsRepositoryImpl implements OfferRecommendationsRepository {

	/**
	 * Error that indicated the failure when searching for recommendation offer ids.
	 */
	static final String OFFER_ID_SEARCH_FAILURE = "Server error when searching for offer ids";
	private static final Logger LOG = LoggerFactory.getLogger(OfferRecommendationsRepositoryImpl.class);
	private final RecommendedOfferPageSizeResolver pageSizeResolver;
	private final ProductRecommendationXPFBridge productRecommendationXPFBridge;

	/**
	 * Creates instance with needed services wired in.
	 *
	 * @param pageSizeResolver               the resolver for page size
	 * @param productRecommendationXPFBridge the product recommendation bridge
	 */
	@Inject
	public OfferRecommendationsRepositoryImpl(
			@Named("recommendedOffersPageSizeResolver") final RecommendedOfferPageSizeResolver pageSizeResolver,
			@Named("productRecommendationXPFBridge") final ProductRecommendationXPFBridge productRecommendationXPFBridge) {
		this.pageSizeResolver = pageSizeResolver;
		this.productRecommendationXPFBridge = productRecommendationXPFBridge;
	}

	@Override
	public Single<PaginatedResult> getRecommendedOffersFromGroup(final Store store, final String sourceProductCode,
																 final String recommendationGroup, final int pageNumber) {
		Single<PaginatedResult> result;
		try {
			int pageSize = pageSizeResolver.getPageSize();
			final XPFProductRecommendations productRecommendations = productRecommendationXPFBridge.getPaginatedResult(store, sourceProductCode,
					recommendationGroup, pageNumber, pageSize);

			result = Observable.fromIterable(productRecommendations.getRecommendations())
					.map(XPFProductRecommendation::getTargetProductCode)
					.toList()
					.map(itemIds -> new PaginatedResult(itemIds, pageNumber, pageSize, productRecommendations.getTotalResultsCount()));
		} catch (Exception exception) {
			LOG.error("Error when searching for offer associations", exception);
			result = Single.error(ResourceOperationFailure.serverError(OFFER_ID_SEARCH_FAILURE));
		}
		return result;
	}

}
