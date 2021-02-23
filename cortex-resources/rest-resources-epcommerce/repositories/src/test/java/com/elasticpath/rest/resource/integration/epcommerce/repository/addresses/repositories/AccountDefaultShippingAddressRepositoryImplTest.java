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
import com.elasticpath.rest.definition.addresses.AccountDefaultShippingAddressIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;

/**
 * Test mechanism for {@link AccountDefaultShippingAddressRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AccountDefaultShippingAddressRepositoryImplTest {

	private static final String ACCOUNT_GUID = "accountGuid";
	private static final String ADDRESS_GUID = "addressGuid";

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private AccountDefaultShippingAddressRepositoryImpl repository;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AccountDefaultShippingAddressIdentifier accountDefaultShippingAddressIdentifier;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AccountAddressesIdentifier accountAddressesIdentifier;

	@Mock
	private Customer customer;

	@Mock
	private CustomerAddress customerAddress;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		when(accountDefaultShippingAddressIdentifier.getAccountShippingAddresses().getAccountAddresses()).thenReturn(accountAddressesIdentifier);
		when(accountAddressesIdentifier.getAccountId().getValue()).thenReturn(ACCOUNT_GUID);
		when(customerRepository.getCustomer(ACCOUNT_GUID)).thenReturn(Single.just(customer));
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);

	}

	@Test
	public void testPreferredShippingAddress() {
		when(customer.getPreferredShippingAddress()).thenReturn(customerAddress);

		Object result = repository.resolve(accountDefaultShippingAddressIdentifier).blockingGet();

		assertThat(result).isInstanceOf(AccountAddressIdentifier.class);
		AccountAddressIdentifier identifier = (AccountAddressIdentifier) result;

		assertThat(identifier.getAccountAddressId().getValue()).isEqualTo(ADDRESS_GUID);
	}

	@Test
	public void testNoPreferredShippingAddress() {
		when(customer.getPreferredShippingAddress()).thenReturn(null);

		repository.resolve(accountDefaultShippingAddressIdentifier)
				.test()
				.assertError(ResourceOperationFailure.notFound());
	}

}
