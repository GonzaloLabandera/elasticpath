/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import static org.mockito.Mockito.when;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.addresses.ContextAwareAddressIdentifier;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test mechanism for {@link ContextAwareAddressEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextAwareAddressEntityRepositoryImplTest {
	private static final String STORE_CODE = "scope";
	private static final String ACCOUNT_ID = "accountId";
	private static final String ADDRESS_ID = "addressId";
	private static final String FIRST_NAME = "John";
	private static final String LAST_NAME = "Smith";
	private static final String STREET_ADDRESS = "123 Main St";
	private static final String EXTENDED_ADDRESS = "1st Ave";
	private static final String REGIONS = "BC";
	private static final String POSTAL_CODE = "V4H 2K5";
	private static final String LOCALITY = "Vancouver";
	private static final String COUNTRY = "CA";
	private static final String USER_ID = "userId";

	@Mock
	private CustomerAddress customerAddress;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private Subject subject;


	@InjectMocks
	private ContextAwareAddressEntityRepositoryImpl<AddressEntity, ContextAwareAddressIdentifier> repository;

	@Before
	public void setUp() {
		when(customerAddress.getGuid()).thenReturn(ADDRESS_ID);
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(customerRepository.getCustomerGuid(USER_ID, subject)).thenReturn(ACCOUNT_ID);
	}

	@Test
	public void findOneTest() {
		when(addressRepository.getAddressEntity(ADDRESS_ID, ACCOUNT_ID)).thenReturn(Single.just(getAddressEntity()));
		repository.findOne(getAddressIdentifier())
				.test()
				.assertNoErrors();
	}

	@Test
	public void findAllTest() {
		when(addressRepository.findAllAddresses(ACCOUNT_ID)).thenReturn(Observable.just(customerAddress));
		repository.findAll(StringIdentifier.of(STORE_CODE))
				.test()
				.assertNoErrors();
	}


	private ContextAwareAddressIdentifier getAddressIdentifier() {
		return new ContextAwareAddressIdentifier(AddressIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(StringIdentifier.of(STORE_CODE)).build())
				.withAddressId(StringIdentifier.of(ADDRESS_ID))
				.build());
	}

	private AddressEntity getAddressEntity() {
		return AddressEntity.builder()
				.withAddress(getBaseAddressEntity())
				.withName(getNameEntity())
				.withAddressId(ADDRESS_ID)
				.build();
	}

	private com.elasticpath.rest.definition.base.AddressEntity getBaseAddressEntity() {
		return com.elasticpath.rest.definition.base.AddressEntity.builder()
				.withStreetAddress(STREET_ADDRESS)
				.withExtendedAddress(EXTENDED_ADDRESS)
				.withRegion(REGIONS)
				.withPostalCode(POSTAL_CODE)
				.withLocality(LOCALITY)
				.withCountryName(COUNTRY)
				.build();
	}

	private NameEntity getNameEntity() {
		return NameEntity.builder()
				.withGivenName(FIRST_NAME)
				.withFamilyName(LAST_NAME)
				.build();
	}
}
