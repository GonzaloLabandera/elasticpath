/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
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
 * Test mechanism for {@link AccountBillingAddressSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AccountBillingAddressSelectorRepositoryImplTest {

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
	private Repository<AddressEntity, AccountAddressIdentifier> accountAddressEntityRepository;

	@Mock
	private Customer account;

	@Mock
	private CustomerAddress customerAddress;

	@Mock
	private AddressEntity addressEntity;

	@Mock
	private CartOrder cartOrder;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private AddressRepository addressRepository;

	@InjectMocks
	private AccountBillingAddressSelectorRepositoryImpl<AccountBillingAddressSelectorIdentifier,
			AccountBillingAddressSelectorChoiceIdentifier> prototype;

	private AccountBillingAddressSelectorIdentifier accountBillingAddressSelectorIdentifier;

	private AccountAddressIdentifier address1Identifier;

	private AccountAddressIdentifier address2Identifier;

	@Mock
	private AccountAddressesIdentifier accountAddressesIdentifier;

	@Before
	public void setUp() {
		when(addressRepository.getAccountAddressesIdentifier(resourceOperationContext)).thenReturn(accountAddressesIdentifier);
		when(accountAddressesIdentifier.getAccountId()).thenReturn(StringIdentifier.of(ACCOUNT_GUID));
		when(customerRepository.getCustomer(ACCOUNT_GUID)).thenReturn(Single.just(account));
		when(customerAddress.getGuid()).thenReturn(ADDRESS1);
		when(customerRepository.update(account)).thenReturn(Single.just(account));
		when(account.getStoreCode()).thenReturn(STORE_CODE);
		when(account.getGuid()).thenReturn(ACCOUNT_GUID);
		when(account.getPreferredBillingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		when(cartOrderRepository.saveCartOrder(cartOrder)).thenReturn(Single.just(cartOrder));

		AccountBillingAddressesIdentifier accountBillingAddressIdentifier = AccountBillingAddressesIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
						.withAddresses(AddressesIdentifier.builder()
								.withScope(SCOPEIDENTIFIER)
								.build()
						)
						.build())
				.build();
		accountBillingAddressSelectorIdentifier = AccountBillingAddressSelectorIdentifier.builder()
				.withAccountBillingAddresses(accountBillingAddressIdentifier).build();

		when(addressEntity.getAddressId()).thenReturn(ADDRESS1);

		address1Identifier = AccountAddressIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
						.withAddresses(AddressesIdentifier.builder()
								.withScope(SCOPEIDENTIFIER)
								.build()
						)
						.build())
				.withAccountAddressId(ADDRESS1IDENTIFIER)
				.build();

		address2Identifier = AccountAddressIdentifier.builder()
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAccountId(StringIdentifier.of(ACCOUNT_GUID))
						.withAddresses(AddressesIdentifier.builder()
								.withScope(SCOPEIDENTIFIER)
								.build()
						)
						.build())
				.withAccountAddressId(ADDRESS2IDENTIFIER)
				.build();
	}

	@Test
	public void testChoicesNoAddressesFound() {

		when(accountAddressEntityRepository.findAll(SCOPEIDENTIFIER)).thenAnswer(invocationOnMock ->
				Observable.fromIterable(Collections.emptyList()));

		prototype.getChoices(accountBillingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void testChoicesSingleAddressFound() {

		when(accountAddressEntityRepository.findAll(SCOPEIDENTIFIER))
				.thenReturn(Observable.fromIterable(Collections.singletonList(address1Identifier)));

		prototype.getChoices(accountBillingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice ->
						ADDRESS1.equals(((AccountBillingAddressSelectorChoiceIdentifier) choice.getChoice())
								.getAccountAddress().getAccountAddressId().getValue()));
	}

	@Test
	public void testChoicesMultipleAddressesFound() {

		when(accountAddressEntityRepository.findAll(SCOPEIDENTIFIER)).thenReturn(Observable.fromIterable(Arrays.asList(address1Identifier,
				address2Identifier)));

		prototype.getChoices(accountBillingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValueAt(0, choice ->
						ADDRESS1.equals(((AccountBillingAddressSelectorChoiceIdentifier) choice.getChoice())
								.getAccountAddress().getAccountAddressId().getValue()))
				.assertValueAt(1, choice ->
						ADDRESS2.equals(((AccountBillingAddressSelectorChoiceIdentifier) choice.getChoice())
								.getAccountAddress().getAccountAddressId().getValue()));
	}

	@Test
	public void testSelectAddressFound() {

		when(accountAddressEntityRepository.findOne(address1Identifier)).thenReturn(Single.just(addressEntity));

		AccountBillingAddressSelectorChoiceIdentifier accountBillingAddressSelectorChoiceIdentifier =
				mock(AccountBillingAddressSelectorChoiceIdentifier.class);
		when(accountBillingAddressSelectorChoiceIdentifier.getAccountBillingAddressSelector()).thenReturn(accountBillingAddressSelectorIdentifier);
		when(accountBillingAddressSelectorChoiceIdentifier.getAccountAddress()).thenReturn(address1Identifier);

		prototype.getChoice(accountBillingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice -> ADDRESS1.equals(((AccountAddressIdentifier) choice.getDescription()).getAccountAddressId().getValue()));
	}

	@Test
	public void testSelector() {
		when(addressRepository.getExistingAddressByGuid(ADDRESS1, account)).thenReturn(Single.just(customerAddress));
		AccountBillingAddressSelectorChoiceIdentifier accountBillingAddressSelectorChoiceIdentifier =
				mock(AccountBillingAddressSelectorChoiceIdentifier.class);
		Observable<String> cartOrderGuidObservable = Observable.just(CART_ORDER_GUID);
		when(cartOrderRepository.findCartOrderGuidsByAccount(STORE_CODE, ACCOUNT_GUID)).thenReturn(cartOrderGuidObservable);
		when(cartOrderRepository.findByGuid(STORE_CODE, cartOrderGuidObservable.blockingSingle())).thenReturn(Single.just(cartOrder));
		when(accountBillingAddressSelectorChoiceIdentifier.getAccountBillingAddressSelector()).thenReturn(accountBillingAddressSelectorIdentifier);
		when(accountBillingAddressSelectorChoiceIdentifier.getAccountAddress()).thenReturn(address1Identifier);

		prototype.selectChoice(accountBillingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors();
	}

	@Test
	public void testSelectStatusExisting() {
		when(account.getPreferredBillingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		SelectStatus selectStatus = prototype.setBillingAddress(account, ADDRESS_GUID).blockingGet();
		assertThat(selectStatus).isEqualTo(SelectStatus.EXISTING);
	}

}
