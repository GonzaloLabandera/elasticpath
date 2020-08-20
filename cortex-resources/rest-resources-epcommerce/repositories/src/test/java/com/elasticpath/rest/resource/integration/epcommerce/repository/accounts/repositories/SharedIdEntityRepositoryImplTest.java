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
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.definition.accounts.SharedAccountIdIdentifier;
import com.elasticpath.rest.definition.accounts.SharedIdEntity;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.service.customer.CustomerService;

@RunWith(MockitoJUnitRunner.class)
public class SharedIdEntityRepositoryImplTest {

	private static final String GUID = "guid";
	private static final String SHARED_ID = "sharedId";
	private static final String SCOPE = "scope";

	@Mock
	private CustomerService customerService;

	@InjectMocks
	private SharedIdEntityRepositoryImpl repository;

	@Mock
	private Customer customer;

	@Before
	public void setUp() {
		when(customerService.findByGuid(GUID)).thenReturn(customer);
		when(customer.getUserId()).thenReturn(SHARED_ID);
	}

	@Test
	public void testFindOne() {
		//Given an account identifier
		SharedAccountIdIdentifier sharedAccountIdIdentifier = SharedAccountIdIdentifier.builder()
				.withAccount(AccountIdentifier.builder().withAccountId(StringIdentifier.of(GUID))
						.withAccounts(AccountsIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build()).build())
				.build();

		//When findOne()
		Single<SharedIdEntity> entitySingle = repository.findOne(sharedAccountIdIdentifier);

		//Then entity contains expected shared-id
		assertThat(entitySingle.blockingGet().getSharedId()).isEqualTo(SHARED_ID);
	}

}
