/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountStatusEntity;
import com.elasticpath.rest.definition.accounts.AccountStatusIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.service.customer.CustomerService;

@RunWith(MockitoJUnitRunner.class)
public class AccountStatusEntityRepositoryImplTest {

	private static final String GUID = "guid";
	private static final String SCOPE = "scope";
	public static final int ACTIVE_STATUS = 1;
	public static final String ACTIVE_STRING = "ACTIVE";

	@Mock
	private CustomerService customerService;

	@Mock
	private AccountStatusIdToAccountStatusMapHolder accountStatusIdToAccountStatusMapHolder;

	@InjectMocks
	private AccountStatusEntityRepositoryImpl repository;

	@Mock
	private Customer customer;

	@Before
	public void setUp() {
		when(customerService.findByGuid(GUID)).thenReturn(customer);
		when(customer.getStatus()).thenReturn(ACTIVE_STATUS);
		when(accountStatusIdToAccountStatusMapHolder.getAccountStatusById(ACTIVE_STATUS)).thenReturn(ACTIVE_STRING);
	}

	@Test
	public void testFindOne() {
		//Given an account identifier
		AccountStatusIdentifier accountStatusIdentifier = AccountStatusIdentifier.builder()
				.withAccount(AccountIdentifier.builder().withAccountId(StringIdentifier.of(GUID))
						.withAccounts(AccountsIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build()).build())
				.build();

		//When findOne()
		Single<AccountStatusEntity> entitySingle = repository.findOne(accountStatusIdentifier);

		//Then entity contains expected shared-id
		assertThat(entitySingle.blockingGet().getStatus()).isEqualTo(ACTIVE_STRING);
	}


}
