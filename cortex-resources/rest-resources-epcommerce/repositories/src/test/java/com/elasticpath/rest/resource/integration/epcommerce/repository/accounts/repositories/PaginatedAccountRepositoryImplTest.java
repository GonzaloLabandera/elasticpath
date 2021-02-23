/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.accounts.ChildAccountsIdentifier;
import com.elasticpath.rest.definition.accounts.PaginatedChildAccountsIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

@RunWith(MockitoJUnitRunner.class)
public class PaginatedAccountRepositoryImplTest {
	private static final String ACCOUNT_ID = "accountId";
	private static final String CHILD_GUID = "childGuid";
	private static final List<String> CHILD_GUIDS = Collections.singletonList(CHILD_GUID);
	private static final int PAGE_SIZE = 5;
	private static final int PAGE_START_INDEX = 1;
	private static final String SCOPE = "mobee";

	@Mock
	private SearchRepository searchRepository;
	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private PaginatedAccountRepositoryImpl repository;

	@Before
	public void setUp() {
		when(customerRepository.findDescendants(ACCOUNT_ID)).thenReturn(CHILD_GUIDS);
		when(customerRepository.findPaginatedChildren(ACCOUNT_ID, 0, PAGE_SIZE)).thenReturn(CHILD_GUIDS);
		when(searchRepository.getDefaultPageSize(SCOPE)).thenReturn(Single.just(PAGE_SIZE));
	}

	@Test
	public void getPaginationInfoTest() {
		PaginatedChildAccountsIdentifier identifier = getPaginatedChildAccountsIdentifier();

		PaginationEntity result = (PaginationEntity) repository.getPaginationInfo(identifier).blockingGet();

		assertThat(result.getCurrent()).isEqualTo(1);
		assertThat(result.getPages()).isEqualTo(1);
		assertThat(result.getPageSize()).isEqualTo(PAGE_SIZE);
		assertThat(result.getResults()).isEqualTo(1);
		assertThat(result.getResultsOnPage()).isEqualTo(1);
	}

	@Test
	public void getElementsTest() {
		PaginatedChildAccountsIdentifier identifier = getPaginatedChildAccountsIdentifier();

		List result = (List) repository.getElements(identifier).toList().blockingGet();

		assertThat(result.size()).isEqualTo(1);
		assertThat(((AccountIdentifier) result.get(0)).getAccountId().getValue()).isEqualTo(CHILD_GUID);
		assertThat(((AccountIdentifier) result.get(0)).getAccounts().getScope().getValue()).isEqualTo(SCOPE);
	}

	@Test
	public void getPagingLinksDoesNotHaveNextPageTest() {
		PaginatedChildAccountsIdentifier identifier = getPaginatedChildAccountsIdentifier();

		assertThat(repository.getPagingLinks(identifier)).isEqualTo(Observable.empty());
	}

	private PaginatedChildAccountsIdentifier getPaginatedChildAccountsIdentifier() {
		AccountsIdentifier accountsIdentifier = AccountsIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build();
		AccountIdentifier accountIdentifier = AccountIdentifier.builder()
				.withAccountId(StringIdentifier.of(ACCOUNT_ID))
				.withAccounts(accountsIdentifier)
				.build();
		ChildAccountsIdentifier childAccountsIdentifier = ChildAccountsIdentifier.builder().withAccount(accountIdentifier).build();
		return PaginatedChildAccountsIdentifier.builder()
				.withChildAccounts(childAccountsIdentifier)
				.withPageId(IntegerIdentifier.of(PAGE_START_INDEX))
				.build();
	}
}
