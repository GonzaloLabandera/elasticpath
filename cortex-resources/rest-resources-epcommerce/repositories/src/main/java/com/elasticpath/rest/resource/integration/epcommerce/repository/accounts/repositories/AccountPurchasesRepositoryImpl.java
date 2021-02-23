/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.purchases.AccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;

/**
 * Account Purchases Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountPurchasesRepositoryImpl<E extends AccountEntity, I extends PaginatedAccountPurchasesIdentifier>
		implements LinksRepository<PurchaseIdentifier, PaginatedAccountPurchasesIdentifier> {

	private static final Integer FIRST_PAGE = 1;
	private OrderRepository orderRepository;

	@Override
	public Observable<PaginatedAccountPurchasesIdentifier> getElements(final PurchaseIdentifier identifier) {
		final IdentifierPart<String> scope = identifier.getPurchases().getScope();
		Maybe<String> accountGuidMaybe = orderRepository.getAccountGuidAssociatedWithOrderNumber(identifier.getPurchaseId().getValue());
		return accountGuidMaybe.flatMapObservable(accountGuid -> buildAccountPurchasesIdentifier(scope, accountGuid));
	}

	private Observable<PaginatedAccountPurchasesIdentifier> buildAccountPurchasesIdentifier(final IdentifierPart<String> scope, final String guid) {

		return Observable.just(PaginatedAccountPurchasesIdentifier.builder()
				.withAccountPurchases(AccountPurchasesIdentifier.builder()
						.withPurchases(PurchasesIdentifier.builder()
								.withScope(scope).build())
						.withAccountId(StringIdentifier.of(guid))
						.build())
				.withPageId(IntegerIdentifier.of(FIRST_PAGE))
				.build());
	}

	@Reference
	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}
}
