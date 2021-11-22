/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
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
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.selector.SelectStatus;

/**
 * Test mechanism for {@link BillingAddressSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BillingAddressSelectorRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final IdentifierPart<String> SCOPEIDENTIFIER = StringIdentifier.of(SCOPE);
	private static final String CUSTOMERGUID = "customerguid";
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
	private AddressRepository addressRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private Repository<AddressEntity, AddressIdentifier> addressEntityRepository;

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
	private BillingAddressSelectorRepositoryImpl<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> prototype;

	private BillingAddressSelectorIdentifier billingAddressSelectorIdentifier;

	private AddressIdentifier address1Identifier;

	private AddressIdentifier address2Identifier;

	@Before
	public void setUp() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(CUSTOMERGUID);
		when(customerRepository.getCustomer(CUSTOMERGUID)).thenReturn(Single.just(customer));
		when(customerAddress.getGuid()).thenReturn(ADDRESS1);

		BillingAddressesIdentifier billingAddressIdentifier = BillingAddressesIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(SCOPEIDENTIFIER)
						.build())
				.build();
		billingAddressSelectorIdentifier = BillingAddressSelectorIdentifier.builder()
				.withBillingAddresses(billingAddressIdentifier).build();

		when(address1Entity.getAddressId()).thenReturn(ADDRESS1);

		address1Identifier = AddressIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(SCOPEIDENTIFIER)
						.build())
				.withAddressId(ADDRESS1IDENTIFIER)
				.build();

		address2Identifier = AddressIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(SCOPEIDENTIFIER)
						.build())
				.withAddressId(ADDRESS2IDENTIFIER)
				.build();
	}

	@Test
	public void testChoicesNoAddressesFound() {

		when(addressEntityRepository.findAll(SCOPEIDENTIFIER)).thenAnswer(invocationOnMock ->
				Observable.fromIterable(Collections.emptyList()));

		prototype.getChoices(billingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void testChoicesSingleAddressFound() {

		when(addressEntityRepository.findAll(SCOPEIDENTIFIER)).thenReturn(Observable.fromIterable(Collections.singletonList(address1Identifier)));

		prototype.getChoices(billingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice ->
						ADDRESS1.equals(((BillingAddressSelectorChoiceIdentifier) choice.getChoice()).getAddress().getAddressId().getValue()));
	}

	@Test
	public void testChoicesMultipleAddressesFound() {

		when(addressEntityRepository.findAll(SCOPEIDENTIFIER)).thenReturn(Observable.fromIterable(Arrays.asList(address1Identifier,
				address2Identifier)));

		prototype.getChoices(billingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValueAt(0, choice ->
						ADDRESS1.equals(((BillingAddressSelectorChoiceIdentifier) choice.getChoice()).getAddress().getAddressId().getValue()))
				.assertValueAt(1, choice ->
						ADDRESS2.equals(((BillingAddressSelectorChoiceIdentifier) choice.getChoice()).getAddress().getAddressId().getValue()));
	}

	@Test
	public void testSelectAddressFound() {

		when(addressEntityRepository.findOne(address1Identifier)).thenReturn(Single.just(address1Entity));

		BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier = mock(BillingAddressSelectorChoiceIdentifier.class);
		when(billingAddressSelectorChoiceIdentifier.getBillingAddressSelector()).thenReturn(billingAddressSelectorIdentifier);
		when(billingAddressSelectorChoiceIdentifier.getAddress()).thenReturn(address1Identifier);

		prototype.getChoice(billingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice -> ADDRESS1.equals(((AddressIdentifier) choice.getDescription()).getAddressId().getValue()));
	}

	@Test
	public void testSelector() {

		BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier = mock(BillingAddressSelectorChoiceIdentifier.class);
		Observable<String> cartOrderGuidObservable = Observable.just(CART_ORDER_GUID);
		when(addressRepository.getExistingAddressByGuid(ADDRESS1, customer)).thenReturn(Single.just(customerAddress));
		when(billingAddressSelectorChoiceIdentifier.getBillingAddressSelector()).thenReturn(billingAddressSelectorIdentifier);
		when(billingAddressSelectorChoiceIdentifier.getAddress()).thenReturn(address1Identifier);
		when(customerRepository.update(customer)).thenReturn(Single.just(customer));
		when(customer.getStoreCode()).thenReturn(STORE_CODE);
		when(customer.getGuid()).thenReturn(CUSTOMERGUID);
		when(customer.getPreferredBillingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		when(cartOrderRepository.findCartOrderGuidsByCustomer(STORE_CODE, CUSTOMERGUID)).thenReturn(cartOrderGuidObservable);
		when(cartOrderRepository.findByGuid(STORE_CODE, cartOrderGuidObservable.blockingSingle())).thenReturn(Single.just(cartOrder));
		when(cartOrderRepository.saveCartOrder(cartOrder)).thenReturn(Single.just(cartOrder));

		prototype.selectChoice(billingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors();

	}

	@Test
	public void testSelectStatusExisting() {
		when(customer.getPreferredBillingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		SelectStatus selectStatus = prototype.setBillingAddress(customer, ADDRESS_GUID).blockingGet();
		assertThat(selectStatus).isEqualTo(SelectStatus.EXISTING);
	}
}
