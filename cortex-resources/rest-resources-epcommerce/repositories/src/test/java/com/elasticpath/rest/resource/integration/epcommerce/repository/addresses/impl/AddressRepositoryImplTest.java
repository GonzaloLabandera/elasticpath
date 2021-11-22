/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.CartOrdersDefaultAddressPopulator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.customer.AddressService;

/**
 * Test for {@link AddressRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddressRepositoryImplTest {

	private static final String NO_VALID_ADDRESS_FIELDS = "No valid address fields specified.";
	private static final String STORE_CODE = "scope";
	private static final String CUSTOMER_ID = "customerId";
	private static final String ADDRESS_ID = "addressId";
	private static final String INVALID_ADDRESS_ID = "invalidAddressId";
	private static final String FIRST_NAME = "John";
	private static final String LAST_NAME = "Smith";
	private static final String STREET_ADDRESS = "123 Main St";
	private static final String EXTENDED_ADDRESS = "1st Ave";
	private static final String REGIONS = "BC";
	private static final String POSTAL_CODE = "V4H 2K5";
	private static final String LOCALITY = "Vancouver";
	private static final String COUNTRY = "CA";
	private static final String PHONE_NUMBER = "6041234567";
	private static final String ORGANIZATION = "Elastic Path";
	private static final long CUSTOMER_UIDPK = 1L;
	private static final long ACCOUNT_UIDPK = 1L;

	@Mock
	private AddressValidator addressValidator;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@Mock
	private CartOrdersDefaultAddressPopulator cartOrdersDefaultAddressPopulator;

	@Mock
	private ConversionService conversionService;

	@Mock
	private CustomerAddress preferredAddress;

	@InjectMocks
	private AddressRepositoryImpl addressRepository;

	private AddressEntity addressEntity;

	private AddressEntity invalidAddressEntity;

	private final List<CustomerAddress> customerAddressList = new ArrayList<>();

	@Mock
	private CustomerAddress customerAddress;

	@Mock
	private Customer customer;

	@Mock
	private Customer account;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AccountBillingAddressesIdentifier supportedIdentifier;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AccountAddressesIdentifier accountAddressesIdentifier;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ResourceIdentifier unsupportedIdentifier;

	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private AddressService addressService;


	@Before
	public void setUp() {
		customerAddressList.add(customerAddress);
		addressRepository.setReactiveAdapter(reactiveAdapter);
		addressEntity = getAddressEntity();
		when(addressValidator.validate(addressEntity)).thenReturn(Completable.complete());
		when(addressValidator.validate(invalidAddressEntity))
				.thenReturn(Completable.error(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS)));
		when(customerRepository.getCustomer(CUSTOMER_ID)).thenReturn(Single.just(customer));
		when(customerRepository.update(customer)).thenReturn(Single.just(customer));
		when(customerRepository.update(account)).thenReturn(Single.just(account));
		when(customer.getUidPk()).thenReturn(CUSTOMER_UIDPK);
		when(account.getUidPk()).thenReturn(ACCOUNT_UIDPK);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_ID);
		when(addressService.findByCustomer(CUSTOMER_UIDPK)).thenReturn(customerAddressList);
		when(addressService.findByCustomerAndAddressGuid(CUSTOMER_UIDPK, ADDRESS_ID)).thenReturn(customerAddress);
		when(addressService.findByCustomerAndAddressGuid(ACCOUNT_UIDPK, ADDRESS_ID)).thenReturn(customerAddress);
		when(addressService.remove(customer, customerAddress)).thenReturn(customer);
		when(supportedIdentifier.getAccountAddresses()).thenReturn(accountAddressesIdentifier);
		when(conversionService.convert(customerAddress, AddressEntity.class)).thenReturn(addressEntity);
		when(conversionService.convert(addressEntity, CustomerAddress.class)).thenReturn(customerAddress);
		when(cartOrdersDefaultAddressPopulator.updateAllCartOrdersAddresses(Mockito.any(Customer.class), Mockito.any(CustomerAddress.class),
				Mockito.anyString(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(Completable.complete());
		when(cartOrdersDefaultAddressPopulator.updateAccountCartOrdersAddresses(Mockito.any(Customer.class), Mockito.any(CustomerAddress.class),
				Mockito.anyString(),
				Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(Completable.complete());
	}

	@Test
	public void updateValidAddress() {
		when(customerRepository.updateAddress(customer, customerAddress)).thenReturn(Completable.complete());
		addressRepository.update(CUSTOMER_ID, ADDRESS_ID, addressEntity)
				.test()
				.assertNoErrors();
	}

	@Test
	public void updateInvalidAddress() {
		addressRepository.update(CUSTOMER_ID, ADDRESS_ID, invalidAddressEntity)
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS));
	}

	@Test
	public void getExistingAddressByGuidSuccessful() {
		addressRepository.getExistingAddressByGuid(ADDRESS_ID, customer)
				.test()
				.assertNoErrors();
	}

	@Test
	public void getExistingAddressByGuidUnsuccessful() {
		addressRepository.getExistingAddressByGuid(INVALID_ADDRESS_ID, customer)
				.test()
				.assertError(ResourceOperationFailure.notFound(addressRepository.ADDRESS_NOT_FOUND));
	}

	@Test
	public void getAddressEntitySuccessful() {
		assertThat(addressRepository.getAddressEntity(ADDRESS_ID, CUSTOMER_ID).blockingGet()).isEqualTo(addressEntity);
	}

	@Test
	public void getAddressEntityReturnsErrorIfAddressDoesNotExist() {
		addressRepository.getAddressEntity(INVALID_ADDRESS_ID, CUSTOMER_ID)
				.test()
				.assertError(ResourceOperationFailure.notFound(addressRepository.ADDRESS_NOT_FOUND));
	}

	@Test
	public void findAllAddresses() {
		TestObserver<CustomerAddress> findAllAddressesResultObserver = addressRepository.findAllAddresses(CUSTOMER_ID)
				.test()
				.assertNoErrors();
		assertThat(findAllAddressesResultObserver.values()).isEqualTo(customerAddressList);
	}

	@Test
	public void addAddressTest() {
		when(customerRepository.addAddress(customer, customerAddress)).thenReturn(Single.just(customer));

		addressRepository.addAddress(customerAddress, customer, STORE_CODE)
				.test()
				.assertNoErrors();
	}

	@Test
	public void addAccountAddressTest() {
		when(customerRepository.addAddress(account, customerAddress)).thenReturn(Single.just(account));

		addressRepository.addAccountAddress(customerAddress, account, STORE_CODE);
		addressRepository.addAccountAddress(customerAddress, account, STORE_CODE)
				.test()
				.assertNoErrors();
	}

	@Test
	public void getExistingAddressMatchingAddressSuccessful() {
		when(addressService.findByAddress(customer.getUidPk(), customerAddress)).thenReturn(customerAddress);

		Optional<CustomerAddress> existingAddressMatchingAddress = addressRepository.getExistingAddressMatchingAddress(customerAddress, customer);
		assertThat(existingAddressMatchingAddress.isPresent()).isTrue();
		assertThat(existingAddressMatchingAddress.get()).isEqualTo(customerAddress);
	}

	@Test
	public void getExistingAddressMatchingAddressUnsuccessful() {
		Optional<CustomerAddress> existingAddressMatchingAddress = addressRepository.getExistingAddressMatchingAddress(new CustomerAddressImpl(),
				customer);

		assertThat(existingAddressMatchingAddress.isPresent()).isFalse();
	}

	@Test
	public void addCustomerPreferredAddressWithNoPreferredAddresses() {
		//Given a customer with no preferred addresses
		Customer customerWithNoPreferredAddresses = new CustomerImpl();
		when(customerRepository.update(customerWithNoPreferredAddresses)).thenReturn(Single.just(customerWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setCustomerPreferredAddress(STORE_CODE, customerWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address has been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(customerAddress);
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(customerAddress);
	}

	@Test
	public void addCustomerPreferredAddressWithPreferredAddresses() {
		//Given a customer with preferred addresses
		Customer customerWithNoPreferredAddresses = new CustomerImpl();
		customerWithNoPreferredAddresses.setPreferredBillingAddress(preferredAddress);
		customerWithNoPreferredAddresses.setPreferredShippingAddress(preferredAddress);
		when(customerRepository.update(customerWithNoPreferredAddresses)).thenReturn(Single.just(customerWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setCustomerPreferredAddress(STORE_CODE, customerWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address hasn't been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(preferredAddress);
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(preferredAddress);
	}

	@Test
	public void addCustomerPreferredAddressWithPreferredShippingAddress() {
		//Given a customer with preferred addresses
		Customer customerWithNoPreferredAddresses = new CustomerImpl();
		customerWithNoPreferredAddresses.setPreferredShippingAddress(preferredAddress);
		when(customerRepository.update(customerWithNoPreferredAddresses)).thenReturn(Single.just(customerWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setCustomerPreferredAddress(STORE_CODE, customerWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address hasn't been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(customerAddress);
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(preferredAddress);
	}

	@Test
	public void addCustomerPreferredAddressWithPreferredBillingAddress() {
		//Given a customer with preferred addresses
		Customer customerWithNoPreferredAddresses = new CustomerImpl();
		customerWithNoPreferredAddresses.setPreferredBillingAddress(preferredAddress);
		when(customerRepository.update(customerWithNoPreferredAddresses)).thenReturn(Single.just(customerWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setCustomerPreferredAddress(STORE_CODE, customerWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address hasn't been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(customerAddress);
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(preferredAddress);
	}

	@Test
	public void addAccountPreferredAddressWithNoPreferredAddresses() {
		//Given a customer with no preferred addresses
		Customer accountWithNoPreferredAddresses = new CustomerImpl();
		when(customerRepository.update(accountWithNoPreferredAddresses)).thenReturn(Single.just(accountWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setAccountPreferredAddress(STORE_CODE, accountWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address has been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(customerAddress);
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(customerAddress);
	}

	@Test
	public void addAccountPreferredAddressWithPreferredAddresses() {
		//Given a customer with preferred addresses
		Customer accountWithNoPreferredAddresses = new CustomerImpl();
		accountWithNoPreferredAddresses.setPreferredBillingAddress(preferredAddress);
		accountWithNoPreferredAddresses.setPreferredShippingAddress(preferredAddress);
		when(customerRepository.update(accountWithNoPreferredAddresses)).thenReturn(Single.just(accountWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setAccountPreferredAddress(STORE_CODE, accountWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address hasn't been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(preferredAddress);
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(preferredAddress);
	}

	@Test
	public void addAccountPreferredAddressWithPreferredShippingAddress() {
		//Given a customer with preferred addresses
		Customer accountWithNoPreferredAddresses = new CustomerImpl();
		accountWithNoPreferredAddresses.setPreferredShippingAddress(preferredAddress);
		when(customerRepository.update(accountWithNoPreferredAddresses)).thenReturn(Single.just(accountWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setAccountPreferredAddress(STORE_CODE, accountWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address hasn't been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(customerAddress);
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(preferredAddress);
	}

	@Test
	public void addAccountPreferredAddressWithPreferredBillingAddress() {
		//Given a customer with preferred addresses
		Customer accountWithNoPreferredAddresses = new CustomerImpl();
		accountWithNoPreferredAddresses.setPreferredBillingAddress(preferredAddress);
		when(customerRepository.update(accountWithNoPreferredAddresses)).thenReturn(Single.just(accountWithNoPreferredAddresses));

		//When adding a new address
		Customer updatedCustomer =
				addressRepository.setAccountPreferredAddress(STORE_CODE, accountWithNoPreferredAddresses, customerAddress).blockingGet();

		//Then the new address hasn't been set as the preferred addresses
		assertThat(updatedCustomer.getPreferredShippingAddress()).isEqualTo(customerAddress);
		assertThat(updatedCustomer.getPreferredBillingAddress()).isEqualTo(preferredAddress);
	}

	@Test
	public void convertCustomerAddressToAddressEntityTest() {
		assertThat(addressRepository.convertCustomerAddressToAddressEntity(customerAddress)).isEqualTo(addressEntity);
	}

	@Test
	public void testValidAddress() {
		addressRepository.validateAddressEntity(addressEntity)
				.test()
				.assertNoErrors();
	}

	@Test
	public void testInvalidAddress() {
		addressRepository.validateAddressEntity(invalidAddressEntity)
				.test()
				.assertError(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS));
	}

	@Test
	public void deleteAddressTest() {
		addressRepository.deleteAddress(ADDRESS_ID, CUSTOMER_ID)
				.test()
				.assertNoErrors();
	}

	@Test
	public void convertAddressEntityTest() {
		assertThat(addressRepository.convertAddressEntityToCustomerAddress(addressEntity))
				.isEqualTo(customerAddress);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getAccountAddressesIdentifierWithNoResourceIdentifier() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.empty());
		addressRepository.getAccountAddressesIdentifier(resourceOperationContext);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void getAccountAddressesIdentifierFailsWithUnsupportedResourceIdentifier() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(unsupportedIdentifier));
		addressRepository.getAccountAddressesIdentifier(resourceOperationContext);
	}

	@Test
	public void getAccountAddressesIdentifierWithSupportedResourceIdentifier() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(supportedIdentifier));
		assertThat(addressRepository.getAccountAddressesIdentifier(resourceOperationContext))
				.isEqualTo(accountAddressesIdentifier);
	}

	@Test
	public void getAccountAddressesIdentifierWithAccountAddressesIdentifier() {
		when(resourceOperationContext.getResourceIdentifier()).thenReturn(Optional.of(accountAddressesIdentifier));
		assertThat(addressRepository.getAccountAddressesIdentifier(resourceOperationContext))
				.isEqualTo(accountAddressesIdentifier);
	}

	private AddressEntity getAddressEntity() {
		return AddressEntity.builder()
				.withAddress(getBaseAddressEntity())
				.withName(getNameEntity())
				.withAddressId(ADDRESS_ID)
				.withOrganization(ORGANIZATION)
				.withPhoneNumber(PHONE_NUMBER)
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
