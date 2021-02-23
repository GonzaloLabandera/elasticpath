/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
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
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test for {@link AccountAddressEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountAddressEntityRepositoryImplTest {

	private static final String NO_VALID_ADDRESS_FIELDS = "No valid address fields specified.";

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

	@Mock
	private CustomerAddress customerAddress;

	@Mock
	private Customer accountCustomer;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private AddressRepository addressRepository;

	private AddressEntity addressEntity;

	@Mock
	private AccountAddressesIdentifier accountAddressesIdentifier;

	@Mock
	private AccountAddressIdentifier accountAddressIdentifier;

	@InjectMocks
	private AccountAddressEntityRepositoryImpl<AddressEntity, AccountAddressIdentifier> repository;

	@Before
	public void setUp() {
		addressEntity = getAddressEntity();
		when(accountAddressIdentifier.getAccountAddressId()).thenReturn(StringIdentifier.of(ADDRESS_ID));
		when(addressRepository.getAccountAddressesIdentifier(resourceOperationContext)).thenReturn(accountAddressesIdentifier);
		when(accountAddressesIdentifier.getAccountId()).thenReturn(StringIdentifier.of(ACCOUNT_ID));
		when(customerAddress.getGuid()).thenReturn(ADDRESS_ID);
		when(customerRepository.getCustomer(ACCOUNT_ID)).thenReturn(Single.just(accountCustomer));
		when(addressRepository.convertAddressEntityToCustomerAddress(addressEntity)).thenReturn(customerAddress);
		when(addressRepository.update(ACCOUNT_ID, ADDRESS_ID, addressEntity)).thenReturn(Completable.complete());
	}

	@Test
	public void updateSuccessful() {
		repository.update(addressEntity, accountAddressIdentifier)
				.test()
				.assertNoErrors();
	}

	@Test
	public void shouldCreateNewCustomerAddressWhenItDoesNotExist() {

		when(addressRepository.validateAddressEntity(addressEntity)).thenReturn(Completable.complete());
		when(addressRepository.addAccountAddress(customerAddress, accountCustomer, STORE_CODE)).thenReturn(Single.just(customerAddress));
		when(addressRepository.getExistingAddressMatchingAddress(customerAddress, accountCustomer)).thenReturn(Optional.empty());

		TestObserver<SubmitResult<AccountAddressIdentifier>> submitResultTestObserver =
				repository.submit(addressEntity, StringIdentifier.of(STORE_CODE))
						.test()
						.assertNoErrors();

		assertThat(getResultStatus(submitResultTestObserver)).isEqualTo("CREATED");
	}


	@Test
	public void shouldNotCreateNewCustomerAddressWhenItAlreadyExists() {
		AddressEntity addressEntity = getAddressEntity();
		when(addressRepository.validateAddressEntity(addressEntity)).thenReturn(Completable.complete());
		when(addressRepository.getExistingAddressMatchingAddress(customerAddress, accountCustomer)).thenReturn(Optional.of(customerAddress));

		TestObserver<SubmitResult<AccountAddressIdentifier>> submitResultTestObserver =
				repository.submit(addressEntity, StringIdentifier.of(STORE_CODE))
						.test()
						.assertNoErrors();

		assertThat(getResultStatus(submitResultTestObserver)).isEqualTo("EXISTING");
	}

	@Test
	public void shouldNotCreateNewCustomerAddressWhenAddressIsInvalid() {
		when(addressRepository.addAccountAddress(customerAddress, accountCustomer, STORE_CODE)).thenReturn(Single.just(customerAddress));
		when(addressRepository.getExistingAddressMatchingAddress(customerAddress, accountCustomer)).thenReturn(Optional.empty());
		when(addressRepository.validateAddressEntity(addressEntity))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS)));

		repository.submit(addressEntity, StringIdentifier.of(STORE_CODE))
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS));
	}

	@Test
	public void findOneTest() {
		when(addressRepository.getAddressEntity(ADDRESS_ID, ACCOUNT_ID)).thenReturn(Single.just(getAddressEntity()));
		repository.findOne(getAccountAddressIdentifier())
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

	@Test
	public void deleteTest() {
		when(addressRepository.deleteAddress(ADDRESS_ID, ACCOUNT_ID)).thenReturn(Completable.complete());
		repository.delete(accountAddressIdentifier)
				.test()
				.assertNoErrors();
	}

	private AccountAddressIdentifier getAccountAddressIdentifier() {
		return AccountAddressIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAddresses(AddressesIdentifier.builder()
								.withScope(StringIdentifier.of(STORE_CODE)).build())
						.withAccountId(StringIdentifier.of(ACCOUNT_ID))
						.build())
				.withAccountAddressId(StringIdentifier.of(ADDRESS_ID))
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

	private String getResultStatus(final TestObserver<SubmitResult<AccountAddressIdentifier>> submitResultTestObserver) {
		return submitResultTestObserver.getEvents().get(0).get(0).toString().split(",")[0];
	}
}
