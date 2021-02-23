/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.selector.SelectStatus;

/**
 * Test mechanism for {@link AccountShippingAddressSelectorRepositoryImpl}.
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.Silent.class)
public class AccountShippingAddressSelectorRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final IdentifierPart<String> SCOPEIDENTIFIER = StringIdentifier.of(SCOPE);
	private static final String ACCOUNT_GUID = "accountguid";
	private static final String ADDRESS1 = "address1";
	private static final IdentifierPart<String> ADDRESS1IDENTIFIER = StringIdentifier.of(ADDRESS1);
	private static final String ADDRESS2 = "address2";
	private static final IdentifierPart<String> ADDRESS2IDENTIFIER = StringIdentifier.of(ADDRESS2);
	private static final String STORE_CODE = "storeCode";
	private static final String CART_ORDER_GUID = "cartOrderGuid";
	private static final String ADDRESS_GUID = "addressGuid";

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private AddressRepository addressRepository;

	@Mock
	private Repository<AddressEntity, AccountAddressIdentifier> accountAddressIdentifierRepository;

	@Mock
	private Customer customer;

	@Mock
	private CustomerAddress customerAddress;

	@Mock
	private AddressEntity address1Entity;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@InjectMocks
	private AccountShippingAddressSelectorRepositoryImpl<AccountShippingAddressSelectorIdentifier,
			AccountShippingAddressSelectorChoiceIdentifier> prototype;

	private AccountShippingAddressSelectorIdentifier accountShippingAddressSelectorIdentifier;

	private AccountAddressIdentifier address1Identifier;

	private AccountAddressIdentifier address2Identifier;

	@Mock
	private AccountAddressesIdentifier accountAddressesIdentifier;

	@Before
	public void setUp() {
		when(addressRepository.getAccountAddressesIdentifier(resourceOperationContext)).thenReturn(accountAddressesIdentifier);
		when(accountAddressesIdentifier.getAccountId()).thenReturn(StringIdentifier.of(ACCOUNT_GUID));
		when(customerRepository.getCustomer(ACCOUNT_GUID)).thenReturn(Single.just(customer));
		when(customerAddress.getGuid()).thenReturn(ADDRESS1);

		AccountShippingAddressesIdentifier shippingAddressIdentifier = AccountShippingAddressesIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
						.withAddresses(
								AddressesIdentifier.builder()
										.withScope(SCOPEIDENTIFIER)
										.build())
						.build())
				.build();

		accountShippingAddressSelectorIdentifier = AccountShippingAddressSelectorIdentifier.builder()
				.withAccountShippingAddresses(shippingAddressIdentifier).build();

		when(address1Entity.getAddressId()).thenReturn(ADDRESS1);

		address1Identifier = AccountAddressIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAddresses(AddressesIdentifier.builder()
								.withScope(SCOPEIDENTIFIER)
								.build())
						.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
						.build())
				.withAccountAddressId(ADDRESS1IDENTIFIER)
				.build();

		address2Identifier = AccountAddressIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAddresses(AddressesIdentifier.builder()
								.withScope(SCOPEIDENTIFIER)
								.build())
						.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
						.build())
				.withAccountAddressId(ADDRESS2IDENTIFIER)
				.build();
	}

	@Test
	public void testChoicesNoAddressesFound() {

		when(accountAddressIdentifierRepository.findAll(SCOPEIDENTIFIER)).thenAnswer(invocationOnMock ->
				Observable.fromIterable(Collections.emptyList()));

		prototype.getChoices(accountShippingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void testChoicesSingleAddressFound() {

		when(accountAddressIdentifierRepository.findAll(SCOPEIDENTIFIER))
				.thenReturn(Observable.fromIterable(Collections.singletonList(address1Identifier)));

		prototype.getChoices(accountShippingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice ->
						ADDRESS1.equals(((AccountShippingAddressSelectorChoiceIdentifier) choice.getChoice())
								.getAccountAddress().getAccountAddressId().getValue()));
	}

	@Test
	public void testChoicesMultipleAddressesFound() {

		when(accountAddressIdentifierRepository.findAll(SCOPEIDENTIFIER)).thenAnswer(invocationOnMock ->
				Observable.fromIterable(Arrays.asList(address1Identifier, address2Identifier)));

		prototype.getChoices(accountShippingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValueAt(0, choice ->
						ADDRESS1.equals(((AccountShippingAddressSelectorChoiceIdentifier) choice.getChoice())
								.getAccountAddress().getAccountAddressId().getValue()))
				.assertValueAt(1, choice ->
						ADDRESS2.equals(((AccountShippingAddressSelectorChoiceIdentifier) choice.getChoice())
								.getAccountAddress().getAccountAddressId().getValue()));
	}

	@Test
	public void testSelectAddressFound() {

		when(accountAddressIdentifierRepository.findOne(address1Identifier)).thenReturn(Single.just(address1Entity));

		AccountShippingAddressSelectorChoiceIdentifier accountShippingAddressSelectorChoiceIdentifier =
				mock(AccountShippingAddressSelectorChoiceIdentifier.class);
		when(accountShippingAddressSelectorChoiceIdentifier.getAccountShippingAddressSelector()).thenReturn(accountShippingAddressSelectorIdentifier);
		when(accountShippingAddressSelectorChoiceIdentifier.getAccountAddress()).thenReturn(address1Identifier);

		prototype.getChoice(accountShippingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice -> ADDRESS1.equals(((AccountAddressIdentifier) choice.getDescription()).getAccountAddressId().getValue()));
	}

	@Test
	public void testSelector() {
		Observable<String> cartOrderGuidObservable = Observable.just(CART_ORDER_GUID);
		AccountShippingAddressSelectorChoiceIdentifier accountShippingAddressSelectorChoiceIdentifier =
				mock(AccountShippingAddressSelectorChoiceIdentifier.class);
		when(accountShippingAddressSelectorChoiceIdentifier.getAccountShippingAddressSelector()).thenReturn(accountShippingAddressSelectorIdentifier);
		when(accountShippingAddressSelectorChoiceIdentifier.getAccountAddress()).thenReturn(address1Identifier);
		when(customerRepository.update(customer)).thenReturn(Single.just(customer));
		when(customer.getStoreCode()).thenReturn(STORE_CODE);
		when(customer.getGuid()).thenReturn(ACCOUNT_GUID);
		when(customer.getPreferredShippingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		when(cartOrder.getGuid()).thenReturn(CART_ORDER_GUID);
		when(cartOrderRepository.findCartOrderGuidsByAccount(STORE_CODE, ACCOUNT_GUID)).thenReturn(cartOrderGuidObservable);
		when(cartOrderRepository.updateShippingAddressOnCartOrder(ADDRESS_GUID, CART_ORDER_GUID, STORE_CODE))
				.thenReturn(Single.just(Boolean.TRUE));

		prototype.selectChoice(accountShippingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors();

		/* Check that we call the appropriate update and retrieval routines */
		verify(customerRepository, times(1)).update(customer);
	}

	@Test
	public void testSelectStatusExisting() {
		when(customer.getPreferredShippingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		SelectStatus selectStatus = prototype.setAccountShippingAddress(customer, ADDRESS_GUID).blockingGet();
		assertThat(selectStatus).isEqualTo(SelectStatus.EXISTING);
	}

}
