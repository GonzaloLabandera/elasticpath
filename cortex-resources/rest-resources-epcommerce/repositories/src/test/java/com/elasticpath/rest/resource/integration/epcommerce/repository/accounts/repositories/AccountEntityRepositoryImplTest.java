/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.service.customer.CustomerProfileAttributeService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.UserAccountAssociationService;

@RunWith(MockitoJUnitRunner.class)
public class AccountEntityRepositoryImplTest {

	private static final String USER_ID = "userId";
	private static final String ACCOUNT_GUID = "account_guid";
	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	private static final String ATTRIBUTE_KEY = "key";
	private static final String ATTRIBUTE_VALUE = "value";

	@Mock
	private UserAccountAssociation userAccountAssociation;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private UserAccountAssociationService userAccountAssociationService;

	@Mock
	private CustomerService customerService;

	@Mock
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Mock
	private CustomerProfileValueTransformer customerProfileValueTransformer;

	@Mock
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@InjectMocks
	private AccountEntityRepositoryImpl repository;

	@Mock
	private Customer accountCustomer;

	@Mock
	private Map<String, Optional<CustomerProfileValue>> attributes;

	@Mock
	private CustomerProfileValue customerProfileValue;

	@Before
	public void setUp() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(userAccountAssociationService.findAssociationsForUser(USER_ID)).thenReturn(new ArrayList<>(Arrays.asList(userAccountAssociation)));
		when(userAccountAssociation.getAccountGuid()).thenReturn(ACCOUNT_GUID);
		when(customerService.findByGuid(ACCOUNT_GUID)).thenReturn(accountCustomer);
		when(customerProfileAttributeService.getAccountEditableAttributes(SCOPE.getValue(), accountCustomer)).thenReturn(attributes);
		when(attributes.keySet()).thenReturn(Collections.singleton(ATTRIBUTE_KEY));
		when(attributes.get(ATTRIBUTE_KEY)).thenReturn(Optional.of(customerProfileValue));
		when(customerProfileValueTransformer.transformToString(customerProfileValue)).thenReturn(ATTRIBUTE_VALUE);
		when(profileAttributeFieldTransformer.transformToFieldName(ATTRIBUTE_KEY)).thenReturn(ATTRIBUTE_KEY);
	}

	@Test
	public void testFindAll() {
		//When findAll()
		final Observable<AccountIdentifier> accountIdentifiersObservable = repository.findAll(SCOPE);

		//Then elements are returned
		assertThat(accountIdentifiersObservable.blockingIterable())
				.hasSize(1)
				.hasOnlyOneElementSatisfying(identifier ->
						assertThat(identifier.getAccountId())
								.isEqualTo(StringIdentifier.of(ACCOUNT_GUID)));
	}

	@Test
	public void testFindOne() {
		//Given an account identifier
		AccountIdentifier accountIdentifier = AccountIdentifier.builder().withAccountId(StringIdentifier.of(ACCOUNT_GUID))
				.withAccounts(AccountsIdentifier.builder().withScope(SCOPE).build()).build();

		//When findOne()
		Single<AccountEntity> entitySingle = repository.findOne(accountIdentifier);

		//Then entity contains expected
		assertThat(entitySingle.blockingGet().getDynamicProperties()).containsKeys(ATTRIBUTE_KEY);
		assertThat(entitySingle.blockingGet().getDynamicProperties().get(ATTRIBUTE_KEY)).isEqualTo(ATTRIBUTE_VALUE);
	}

}
