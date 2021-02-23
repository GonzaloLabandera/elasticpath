/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.purchases.PaginatedAccountPurchasesIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;

@RunWith(MockitoJUnitRunner.class)
public class AccountPurchasesRepositoryImplTest {
	private static final String ACCOUNT_GUID = "account_guid";
	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	private static final IdentifierPart<String> PURCHASE_ID = StringIdentifier.of("purchase_id");

	@Mock
	private PurchaseIdentifier purchaseIdentifier;

	@Mock
	private PurchasesIdentifier purchasesIdentifier;

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private AccountPurchasesRepositoryImpl<AccountEntity, PaginatedAccountPurchasesIdentifier> repository;

	@Before
	public void setUp() {
		when(orderRepository.getAccountGuidAssociatedWithOrderNumber(anyString())).thenReturn(Maybe.just(ACCOUNT_GUID));
		when(purchaseIdentifier.getPurchases()).thenReturn(purchasesIdentifier);
		when(purchaseIdentifier.getPurchaseId()).thenReturn(PURCHASE_ID);
		when(purchasesIdentifier.getScope()).thenReturn(SCOPE);
	}

	@Test
	public void testThatGetElementsPaginatedReturnsAccountPurchasesIdentifier() {
		final Observable<PaginatedAccountPurchasesIdentifier> accountPurchasesIdentifierObservable = repository.getElements(purchaseIdentifier);

		assertThat(accountPurchasesIdentifierObservable.blockingIterable())
				.hasSize(1)
				.hasOnlyOneElementSatisfying(identifier ->
						assertThat(identifier.getAccountPurchases().getAccountId().getValue())
								.isEqualTo(ACCOUNT_GUID));
	}

}
