/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountDefaultBillingAddressIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;

/**
 * Test mechanism for {@link AccountDefaultBillingAddressRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AccountDefaultBillingAddressRepositoryImplTest {

	private static final String ACCOUNT_GUID = "accountGuid";
	private static final String ADDRESS_GUID = "addressGuid";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private AccountDefaultBillingAddressRepositoryImpl repository;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AccountDefaultBillingAddressIdentifier accountDefaultBillingAddressIdentifier;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AccountAddressesIdentifier accountAddressesIdentifier;

	@Mock
	private Customer customer;

	@Mock
	private CustomerAddress customerAddress;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		when(accountDefaultBillingAddressIdentifier.getAccountBillingAddresses().getAccountAddresses()).thenReturn(accountAddressesIdentifier);
		when(accountAddressesIdentifier.getAccountId().getValue()).thenReturn(ACCOUNT_GUID);
		when(customerRepository.getCustomer(ACCOUNT_GUID)).thenReturn(Single.just(customer));
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);

	}

	@Test
	public void testPreferredBillingAddress() {
		when(customer.getPreferredBillingAddress()).thenReturn(customerAddress);

		Object result = repository.resolve(accountDefaultBillingAddressIdentifier).blockingGet();

		assertThat(result).isInstanceOf(AccountAddressIdentifier.class);
		AccountAddressIdentifier identifier = (AccountAddressIdentifier) result;

		assertThat(identifier.getAccountAddressId().getValue()).isEqualTo(ADDRESS_GUID);
	}

	@Test
	public void testNoPreferredBillingAddress() {
		when(customer.getPreferredBillingAddress()).thenReturn(null);

		repository.resolve(accountDefaultBillingAddressIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

}
