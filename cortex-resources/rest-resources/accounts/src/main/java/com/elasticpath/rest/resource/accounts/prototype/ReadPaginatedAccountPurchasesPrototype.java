/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.definition.accounts.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.definition.accounts.PaginatedAccountPurchasesResource;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;

/**
 * Read account purchases in a form of pages.
 */
public class ReadPaginatedAccountPurchasesPrototype implements PaginatedAccountPurchasesResource.Pageable {

	private final PaginatedAccountPurchasesIdentifier paginatedAccountPurchasesIdentifier;

	private final PaginationRepository<PaginatedAccountPurchasesIdentifier, PurchaseIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param paginatedAccountPurchasesIdentifier paginated account purchases identifier
	 * @param repository                          repository to get purchases
	 */
	@Inject
	public ReadPaginatedAccountPurchasesPrototype(
			@RequestIdentifier final PaginatedAccountPurchasesIdentifier paginatedAccountPurchasesIdentifier,
			@ResourceRepository final PaginationRepository<PaginatedAccountPurchasesIdentifier, PurchaseIdentifier> repository) {

		this.paginatedAccountPurchasesIdentifier = paginatedAccountPurchasesIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaginationEntity> onRead() {
		return repository.getPaginationInfo(paginatedAccountPurchasesIdentifier);
	}

	@Override
	public Observable<PurchaseIdentifier> elements() {
		return repository.getElements(paginatedAccountPurchasesIdentifier);
	}

	@Override
	public Observable<PagingLink<PaginatedAccountPurchasesIdentifier>> pagingLinks() {
		return repository.getPagingLinks(paginatedAccountPurchasesIdentifier);
	}
}
