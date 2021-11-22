/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.recommendations.prototype;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.recommendations.PaginatedOffersRecommendationsIdentifier;
import com.elasticpath.rest.definition.recommendations.PaginatedOffersRecommendationsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;

/**
 * Read offers that are recommended in a form of pages.
 */
public class ReadPaginatedOfferRecommendationsImpl implements PaginatedOffersRecommendationsResource.Pageable {

	private final PaginatedOffersRecommendationsIdentifier paginatedOffersRecommendationsIdentifier;

	private final PaginationRepository<PaginatedOffersRecommendationsIdentifier, OfferIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param paginatedOffersRecommendationsIdentifier paginated recommendation identifier
	 * @param repository repository to get offers
	 */
	@Inject
	public ReadPaginatedOfferRecommendationsImpl(
			@RequestIdentifier final PaginatedOffersRecommendationsIdentifier paginatedOffersRecommendationsIdentifier,
			@ResourceRepository final PaginationRepository<PaginatedOffersRecommendationsIdentifier, OfferIdentifier> repository) {

		this.paginatedOffersRecommendationsIdentifier = paginatedOffersRecommendationsIdentifier;
		this.repository = repository;
	}


	@Override
	public Single<PaginationEntity> onRead() {
		return repository.getPaginationInfo(paginatedOffersRecommendationsIdentifier);
	}

	@Override
	public Observable<OfferIdentifier> elements() {
		return repository.getElements(paginatedOffersRecommendationsIdentifier);
	}

	@Override
	public Observable<PagingLink<PaginatedOffersRecommendationsIdentifier>> pagingLinks() {
		return repository.getPagingLinks(paginatedOffersRecommendationsIdentifier);
	}
}
