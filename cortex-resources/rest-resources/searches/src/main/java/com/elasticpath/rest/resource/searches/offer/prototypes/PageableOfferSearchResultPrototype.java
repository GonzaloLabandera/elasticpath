/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.searches.offer.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;

/**
 * Pageable prototype for offer search result resource.
 */
public class PageableOfferSearchResultPrototype implements OfferSearchResultResource.Pageable {

	private final OfferSearchResultIdentifier searchResultIdentifier;

	private final PaginationRepository<OfferSearchResultIdentifier, OfferIdentifier> paginationRepository;


	/**
	 * Constructor.
	 *
	 * @param searchResultIdentifier KeywordSearchResultIdentifier
	 * @param paginationRepository   PaginationRepository
	 */
	@Inject
	public PageableOfferSearchResultPrototype(@RequestIdentifier final OfferSearchResultIdentifier searchResultIdentifier,
											  @ResourceRepository final PaginationRepository<OfferSearchResultIdentifier, OfferIdentifier> paginationRepository) {
		this.searchResultIdentifier = searchResultIdentifier;
		this.paginationRepository = paginationRepository;
	}

	@Override
	public Single<PaginationEntity> onRead() {
		return paginationRepository.getPaginationInfo(searchResultIdentifier);
	}

	@Override
	public Observable<OfferIdentifier> elements() {
		return paginationRepository.getElements(searchResultIdentifier);
	}

	@Override
	public Observable<PagingLink<OfferSearchResultIdentifier>> pagingLinks() {
		return paginationRepository.getPagingLinks(searchResultIdentifier);
	}
}
