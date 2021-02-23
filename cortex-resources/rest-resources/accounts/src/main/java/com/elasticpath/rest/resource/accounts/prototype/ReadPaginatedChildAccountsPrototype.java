/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.PaginatedChildAccountsIdentifier;
import com.elasticpath.rest.definition.accounts.PaginatedChildAccountsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;

/**
 * Read Paginated Child Accounts Prototype.
 */
public class ReadPaginatedChildAccountsPrototype implements PaginatedChildAccountsResource.Pageable {
	private final PaginationRepository<PaginatedChildAccountsIdentifier, AccountIdentifier> repository;
	private final PaginatedChildAccountsIdentifier childAccountsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param repository paginated child account repository
	 * @param childAccountsIdentifier child accounts identifier
	 */
	@Inject
	public ReadPaginatedChildAccountsPrototype(
			@ResourceRepository final PaginationRepository<PaginatedChildAccountsIdentifier, AccountIdentifier> repository,
			@RequestIdentifier final PaginatedChildAccountsIdentifier childAccountsIdentifier) {
		this.repository = repository;
		this.childAccountsIdentifier = childAccountsIdentifier;
	}

	@Override
	public Single<PaginationEntity> onRead() {
		return repository.getPaginationInfo(childAccountsIdentifier);
	}

	@Override
	public Observable<AccountIdentifier> elements() {
		return repository.getElements(childAccountsIdentifier);
	}

	@Override
	public Observable<PagingLink<PaginatedChildAccountsIdentifier>> pagingLinks() {
		return repository.getPagingLinks(childAccountsIdentifier);
	}
}
