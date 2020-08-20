/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.definition.accounts.AccountAttributesEntity;
import com.elasticpath.rest.definition.accounts.AccountAttributesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.customer.CustomerService;

@RunWith(MockitoJUnitRunner.class)
public class AccountAttributesEntityRepositoryImplTest {

	private static final String GUID = "guid";
	private static final String SCOPE = "scope";
	private static final String ATTRIBUTE_KEY = "key";
	private static final String ATTRIBUTE_VALUE = "value";

	@Mock
	private CustomerService customerService;

	@Mock
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Mock
	private CustomerProfileValueTransformer customerProfileValueTransformer;

	@Mock
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@InjectMocks
	private AccountAttributesEntityRepositoryImpl repository;

	@Mock
	private Customer account;

	@Mock
	Map<String, Optional<CustomerProfileValue>> attributes;

	@Mock
	CustomerProfileValue customerProfileValue;

	@Before
	public void setUp() {
		when(customerService.findByGuid(GUID)).thenReturn(account);
		when(customerProfileAttributeService.getAccountReadOnlyAttributes(SCOPE, account)).thenReturn(attributes);
		when(attributes.keySet()).thenReturn(Collections.singleton(ATTRIBUTE_KEY));
		when(attributes.get(ATTRIBUTE_KEY)).thenReturn(Optional.of(customerProfileValue));
		when(customerProfileValueTransformer.transformToString(customerProfileValue)).thenReturn(ATTRIBUTE_VALUE);
		when(profileAttributeFieldTransformer.transformToFieldName(ATTRIBUTE_KEY)).thenReturn(ATTRIBUTE_KEY);
	}

	@Test
	public void testFindOne() {
		//Given an account attributes identifier
		AccountAttributesIdentifier accountAttributesIdentifier = AccountAttributesIdentifier.builder()
				.withAccount(AccountIdentifier.builder().withAccountId(StringIdentifier.of(GUID))
						.withAccounts(AccountsIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build()).build())
				.build();

		//When findOne()
		Single<AccountAttributesEntity> entitySingle = repository.findOne(accountAttributesIdentifier);

		//Then entity contains expected attributes
		assertThat(entitySingle.blockingGet().getDynamicProperties()).containsKey(ATTRIBUTE_KEY);
		assertThat(entitySingle.blockingGet().getDynamicProperties().get(ATTRIBUTE_KEY)).isEqualTo(ATTRIBUTE_VALUE);
	}

}
