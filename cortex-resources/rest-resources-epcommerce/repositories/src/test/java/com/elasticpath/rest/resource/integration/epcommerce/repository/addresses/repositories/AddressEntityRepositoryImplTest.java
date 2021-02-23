/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test for {@link AddressEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddressEntityRepositoryImplTest {

	private static final String NO_VALID_ADDRESS_FIELDS = "No valid address fields specified.";

	private static final String STORE_CODE = "scope";
	private static final String CUSTOMER_ID = "customerId";
	private static final String ADDRESS_ID = "addressId";
	private static final String FIRST_NAME = "John";
	private static final String LAST_NAME = "Smith";
	private static final String STREET_ADDRESS = "123 Main St";
	private static final String EXTENDED_ADDRESS = "1st Ave";
	private static final String REGIONS = "BC";
	private static final String POSTAL_CODE = "V4H 2K5";
	private static final String LOCALITY = "Vancouver";
	private static final String COUNTRY = "CA";

	@Mock
	private CustomerAddress customerAddress;

	@Mock
	private Customer customer;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private AddressRepository addressRepository;

	private AddressEntity addressEntity;

	@InjectMocks
	private AddressEntityRepositoryImpl<AddressEntity, AddressIdentifier> repository;

	@Before
	public void setUp() {
		addressEntity = getAddressEntity();
		when(resourceOperationContext.getUserIdentifier()).thenReturn(CUSTOMER_ID);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_ID);
		when(customerRepository.getCustomer(CUSTOMER_ID)).thenReturn(Single.just(customer));
		when(addressRepository.convertAddressEntityToCustomerAddress(addressEntity)).thenReturn(customerAddress);
	}

	@Test
	public void shouldCreateNewCustomerAddressWhenItDoesNotExist() {

		when(addressRepository.validateAddressEntity(addressEntity)).thenReturn(Completable.complete());
		when(addressRepository.addAddress(customerAddress, customer, STORE_CODE)).thenReturn(Single.just(customerAddress));
		when(addressRepository.getExistingAddressMatchingAddress(customerAddress, customer)).thenReturn(Optional.empty());

		TestObserver<SubmitResult<AddressIdentifier>> submitResultTestObserver = repository.submit(addressEntity, StringIdentifier.of(STORE_CODE))
				.test()
				.assertNoErrors();

		assertThat(getResultStatus(submitResultTestObserver)).isEqualTo("CREATED");
	}


	@Test
	public void shouldNotCreateNewCustomerAddressWhenItAlreadyExists() {
		AddressEntity addressEntity = getAddressEntity();
		when(addressRepository.validateAddressEntity(addressEntity)).thenReturn(Completable.complete());
		when(addressRepository.getExistingAddressMatchingAddress(customerAddress, customer)).thenReturn(Optional.of(customerAddress));

		TestObserver<SubmitResult<AddressIdentifier>> submitResultTestObserver = repository.submit(addressEntity, StringIdentifier.of(STORE_CODE))
				.test()
				.assertNoErrors();

		assertThat(getResultStatus(submitResultTestObserver)).isEqualTo("EXISTING");
	}

	@Test
	public void shouldNotCreateNewCustomerAddressWhenAddressIsInvalid() {
		when(addressRepository.addAddress(customerAddress, customer, STORE_CODE)).thenReturn(Single.just(customerAddress));
		when(addressRepository.getExistingAddressMatchingAddress(customerAddress, customer)).thenReturn(Optional.empty());
		when(addressRepository.validateAddressEntity(addressEntity))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS)));

		repository.submit(addressEntity, StringIdentifier.of(STORE_CODE))
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS));
	}

	@Test
	public void findOneTest() {
		when(addressRepository.getAddressEntity(ADDRESS_ID, CUSTOMER_ID)).thenReturn(Single.just(getAddressEntity()));
		repository.findOne(getAddressIdentifier())
				.test()
				.assertNoErrors();
	}

	@Test
	public void findAllTest() {
		when(addressRepository.findAllAddresses(CUSTOMER_ID)).thenReturn(Observable.just(customerAddress));
		repository.findAll(StringIdentifier.of(STORE_CODE))
				.test()
				.assertNoErrors();
	}

	private AddressIdentifier getAddressIdentifier() {
		return AddressIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(StringIdentifier.of(STORE_CODE))
						.build())
				.withAddressId(StringIdentifier.of(ADDRESS_ID))
				.build();
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

	private String getResultStatus(final TestObserver<SubmitResult<AddressIdentifier>> submitResultTestObserver) {
		return submitResultTestObserver.getEvents().get(0).get(0).toString().split(",")[0];
	}
}
