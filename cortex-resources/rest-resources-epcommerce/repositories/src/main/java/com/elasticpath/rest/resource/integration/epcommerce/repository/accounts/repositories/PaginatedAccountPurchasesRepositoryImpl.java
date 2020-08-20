/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.ArrayList;
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
import com.elasticpath.rest.definition.accounts.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Paginated links entity repo for the account purchases.
 *
 * @param <R> paginated account purchases identifier
 * @param <I> purchase identifier
 */
@Component(service = PaginationRepository.class)
public class PaginatedAccountPurchasesRepositoryImpl<R extends PaginatedAccountPurchasesIdentifier, I extends PurchaseIdentifier>
		extends PaginationRepository<PaginatedAccountPurchasesIdentifier, PurchaseIdentifier> {

	private static final Logger LOG = LoggerFactory.getLogger(PaginatedAccountPurchasesRepositoryImpl.class);

	private OrderRepository orderRepository;
	private SearchRepository searchRepository;

	private static final int FIRST_PAGE_ID = 1;


	@Override
	public Single<PaginationEntity> getPaginationInfo(final PaginatedAccountPurchasesIdentifier identifier) {
		String accountId = identifier.getAccountPurchases().getAccount().getAccountId().getValue();
		int result = (new Long(orderRepository.getAccountPurchasesSize(getScope(identifier), accountId))).intValue();
		int pageSize = getPageSize(identifier);
		int current = identifier.getPageId().getValue();
		int maximumPageSize = (int) Math.ceil(result / (double) pageSize);
		return Single.just(PaginationEntity.builder()
				.withCurrent(current)
				.withPageSize(pageSize)
				.withPages(maximumPageSize)
				.withResultsOnPage(pageSize)
				.withResults(result)
				.build());
	}

	@Override
	public Observable<PurchaseIdentifier> getElements(final PaginatedAccountPurchasesIdentifier identifier) {
		int pageId = identifier.getPageId().getValue();
		String accountId = identifier.getAccountPurchases().getAccount().getAccountId().getValue();
		long result = orderRepository.getAccountPurchasesSize(getScope(identifier), accountId);
		int pageSize = getPageSize(identifier);
		int maximumPageId = (int) Math.ceil(result / (double) pageSize);

		if (pageId < FIRST_PAGE_ID || pageId > Math.max(FIRST_PAGE_ID, maximumPageId)) {
			LOG.error("Unknown page id " + pageId + ", the page id should be between " + FIRST_PAGE_ID + " and " + maximumPageId);
			return Observable.error(ResourceOperationFailure.notFound());
		}

		IdentifierPart<String> scope = identifier.getAccountPurchases().getAccount().getAccounts().getScope();

		int pageStartIndex = (pageId - 1) * pageSize;

		List<PurchaseIdentifier> purchaseIdentifiers = getOrderIds(scope.getValue(), accountId, pageStartIndex, pageSize).stream()
				.map(purchaseId -> PurchaseIdentifier.builder()
						.withPurchases(PurchasesIdentifier.builder()
								.withScope(scope)
								.build())
						.withPurchaseId(StringIdentifier.of(purchaseId))
						.build())
				.collect(Collectors.toList());

		return Observable.fromIterable(purchaseIdentifiers);
	}

	private String getScope(final PaginatedAccountPurchasesIdentifier identifier) {
		return identifier.getAccountPurchases().getAccount().getAccounts().getScope().getValue();
	}

	@Override
	public Observable<PagingLink<PaginatedAccountPurchasesIdentifier>> getPagingLinks(final PaginatedAccountPurchasesIdentifier identifier) {
		int pageId = identifier.getPageId().getValue();
		String accountId = identifier.getAccountPurchases().getAccount().getAccountId().getValue();
		long result = orderRepository.getAccountPurchasesSize(getScope(identifier), accountId);
		int maximumPageSize = (int) Math.ceil(result / (double) getPageSize(identifier));

		return createPagingLinks(pageId, maximumPageSize, identifier);
	}

	private Integer getPageSize(final PaginatedAccountPurchasesIdentifier identifier) {
		return searchRepository.getDefaultPageSize(getScope(identifier)).blockingGet();
	}

	@Override
	public PaginatedAccountPurchasesIdentifier buildPageIdentifier(
			final PaginatedAccountPurchasesIdentifier identifier, final IdentifierPart<Integer> pageId) {

		return PaginatedAccountPurchasesIdentifier
				.builderFrom(identifier)
				.withPageId(pageId)
				.build();
	}

	private List<String> getOrderIds(final String scope, final String accountId, final int startIndex, final int maxResults) {
		List<String> orderIdsArray = new ArrayList<>();
		Observable<String> orderIdsByCustomerGuid = orderRepository.findOrderIdsByAccountGuid(scope, accountId, startIndex, maxResults);
		orderIdsByCustomerGuid.blockingIterable().forEach(orderIdsArray::add);
		return orderIdsArray;
	}

	@Reference
	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

}
