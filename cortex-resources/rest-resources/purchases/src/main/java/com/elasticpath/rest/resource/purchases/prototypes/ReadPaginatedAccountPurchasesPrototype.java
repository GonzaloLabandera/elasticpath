/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.purchases.prototypes;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.purchases.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.PaginatedAccountPurchasesResource;
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

	/*
		This unused field is required to access the accountEntityPurchaseRepository in AccountIdParameterStrategy.
		Note: Injecting an OSGi service in non-prototype classes (e.g. a PermissionParameterStrategy) with @ResourceRepository or @ResourceService
		will not work unless the services are already injected in a prototype class.  See "Data Injectors" in the cortex documentation.
	*/
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final Repository<AccountEntity, AccountIdentifier> accountEntityPurchaseRepository;


	/**
	 * Constructor.
	 *
	 * @param paginatedAccountPurchasesIdentifier paginated account purchases identifier
	 * @param repository                          repository to get purchases
	 * @param accountEntityPurchaseRepository     account entity purchase repository
	 */
	@Inject
	public ReadPaginatedAccountPurchasesPrototype(
			@RequestIdentifier final PaginatedAccountPurchasesIdentifier paginatedAccountPurchasesIdentifier,
			@ResourceRepository final PaginationRepository<PaginatedAccountPurchasesIdentifier, PurchaseIdentifier> repository,
			@ResourceRepository final Repository<AccountEntity, AccountIdentifier> accountEntityPurchaseRepository) {

		this.paginatedAccountPurchasesIdentifier = paginatedAccountPurchasesIdentifier;
		this.repository = repository;
		this.accountEntityPurchaseRepository = accountEntityPurchaseRepository;
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
