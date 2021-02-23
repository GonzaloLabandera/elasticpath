/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.accounts.PaginatedChildAccountsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Paginated links entity repo for the child accounts.
 *
 * @param <R> paginated child accounts identifier
 * @param <I> account identifier
 */
@Component(service = PaginationRepository.class)
public class PaginatedAccountRepositoryImpl<R extends PaginatedChildAccountsIdentifier, I extends AccountIdentifier>
		extends PaginationRepository<PaginatedChildAccountsIdentifier, AccountIdentifier> {
	private static final Logger LOG = LoggerFactory.getLogger(PaginatedAccountRepositoryImpl.class);
	private static final int FIRST_PAGE_ID = 1;

	private SearchRepository searchRepository;
	private CustomerRepository customerRepository;

	@Override
	public Single<PaginationEntity> getPaginationInfo(final PaginatedChildAccountsIdentifier identifier) {
		String accountId = identifier.getChildAccounts().getAccount().getAccountId().getValue();
		int result = customerRepository.findDescendants(accountId).size();
		int pageSize = getPageSize(identifier);
		int pageId = identifier.getPageId().getValue();
		int pageStartIndex = (pageId - 1) * pageSize;
		int onPage = customerRepository.findPaginatedChildren(accountId, pageStartIndex, pageSize).size();
		int current = identifier.getPageId().getValue();
		int maximumPageSize = (int) Math.ceil(result / (double) pageSize);
		return Single.just(PaginationEntity.builder()
				.withCurrent(current)
				.withPageSize(pageSize)
				.withPages(maximumPageSize)
				.withResultsOnPage(onPage)
				.withResults(result)
				.build());
	}

	@Override
	public Observable<AccountIdentifier> getElements(final PaginatedChildAccountsIdentifier identifier) {
		int pageId = identifier.getPageId().getValue();
		String accountId = identifier.getChildAccounts().getAccount().getAccountId().getValue();
		int result = customerRepository.findDescendants(accountId).size();
		int pageSize = getPageSize(identifier);
		int maximumPageId = (int) Math.ceil(result / (double) pageSize);

		if (pageId < FIRST_PAGE_ID || pageId > Math.max(FIRST_PAGE_ID, maximumPageId)) {
			LOG.error("Unknown page id {}, the page id should be between {} and {}", pageId, FIRST_PAGE_ID, maximumPageId);
			return Observable.error(ResourceOperationFailure.notFound());
		}

		IdentifierPart<String> scope = identifier.getChildAccounts().getAccount().getAccounts().getScope();

		int pageStartIndex = (pageId - 1) * pageSize;

		List<AccountIdentifier> purchaseIdentifiers = customerRepository.findPaginatedChildren(accountId, pageStartIndex, pageSize)
				.stream()
				.map(guid -> AccountIdentifier.builder()
						.withAccountId(StringIdentifier.of(guid))
						.withAccounts(AccountsIdentifier.builder().withScope(scope).build())
						.build())
				.collect(Collectors.toList());

		return Observable.fromIterable(purchaseIdentifiers);
	}

	@Override
	public Observable<PagingLink<PaginatedChildAccountsIdentifier>> getPagingLinks(final PaginatedChildAccountsIdentifier identifier) {
		String accountId = identifier.getChildAccounts().getAccount().getAccountId().getValue();

		int pageId = identifier.getPageId().getValue();
		int result = customerRepository.findDescendants(accountId).size();
		int maximumPageSize = (int) Math.ceil(result / (double) getPageSize(identifier));

		return createPagingLinks(pageId, maximumPageSize, identifier);
	}

	@Override
	protected PaginatedChildAccountsIdentifier buildPageIdentifier(final PaginatedChildAccountsIdentifier identifier,
																   final IdentifierPart<Integer> pageId) {

		return PaginatedChildAccountsIdentifier.builderFrom(identifier)
				.withPageId(pageId)
				.build();
	}

	private Integer getPageSize(final PaginatedChildAccountsIdentifier identifier) {
		return searchRepository.getDefaultPageSize(getScope(identifier)).blockingGet();
	}

	private String getScope(final PaginatedChildAccountsIdentifier identifier) {
		return identifier.getChildAccounts().getAccount().getAccounts().getScope().getValue();
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}
}
