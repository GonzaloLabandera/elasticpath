/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

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
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Test mechanism for {@link ShippingAddressSelectorRepositoryImpl}.
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.Silent.class)
public class ShippingAddressSelectorRepositoryImplTest {

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
	private ShippingAddressSelectorRepositoryImpl<ShippingAddressSelectorIdentifier,
				ShippingAddressSelectorChoiceIdentifier> prototype;

	private ShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier;

	private AddressIdentifier address1Identifier;

	private AddressIdentifier address2Identifier;

	@Before
	public void setUp() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(CUSTOMERGUID);
		when(customerRepository.getCustomer(CUSTOMERGUID)).thenReturn(Single.just(customer));
		when(customerAddress.getGuid()).thenReturn(ADDRESS1);

		ShippingAddressesIdentifier shippingAddressIdentifier = ShippingAddressesIdentifier.builder()
				.withAddresses(AddressesIdentifier.builder()
						.withScope(SCOPEIDENTIFIER)
						.build())
				.build();
		shippingAddressSelectorIdentifier = ShippingAddressSelectorIdentifier.builder()
				.withShippingAddresses(shippingAddressIdentifier).build();

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

		prototype.getChoices(shippingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void testChoicesSingleAddressFound() {

		when(addressEntityRepository.findAll(SCOPEIDENTIFIER)).thenReturn(Observable.fromIterable(Collections.singletonList(address1Identifier)));

		prototype.getChoices(shippingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice ->
						ADDRESS1.equals(((ShippingAddressSelectorChoiceIdentifier) choice.getChoice()).getAddress().getAddressId().getValue()));
	}

	@Test
	public void testChoicesMultipleAddressesFound() {

		when(addressEntityRepository.findAll(SCOPEIDENTIFIER)).thenAnswer(invocationOnMock ->
				Observable.fromIterable(Arrays.asList(address1Identifier, address2Identifier)));

		prototype.getChoices(shippingAddressSelectorIdentifier)
				.test()
				.assertNoErrors()
				.assertValueAt(0, choice ->
						ADDRESS1.equals(((ShippingAddressSelectorChoiceIdentifier) choice.getChoice()).getAddress().getAddressId().getValue()))
				.assertValueAt(1, choice ->
						ADDRESS2.equals(((ShippingAddressSelectorChoiceIdentifier) choice.getChoice()).getAddress().getAddressId().getValue()));
	}

	@Test
	public void testSelectAddressFound() {

		when(addressEntityRepository.findOne(address1Identifier)).thenReturn(Single.just(address1Entity));

		ShippingAddressSelectorChoiceIdentifier shippingAddressSelectorChoiceIdentifier = mock(ShippingAddressSelectorChoiceIdentifier.class);
		when(shippingAddressSelectorChoiceIdentifier.getShippingAddressSelector()).thenReturn(shippingAddressSelectorIdentifier);
		when(shippingAddressSelectorChoiceIdentifier.getAddress()).thenReturn(address1Identifier);

		prototype.getChoice(shippingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(choice -> ADDRESS1.equals(((AddressIdentifier) choice.getDescription()).getAddressId().getValue()));
	}

	@Test
	public void testSelector() {
		Observable<String> cartOrderGuidObservable = Observable.just(CART_ORDER_GUID);
		ShippingAddressSelectorChoiceIdentifier shippingAddressSelectorChoiceIdentifier = mock(ShippingAddressSelectorChoiceIdentifier.class);
		when(shippingAddressSelectorChoiceIdentifier.getShippingAddressSelector()).thenReturn(shippingAddressSelectorIdentifier);
		when(shippingAddressSelectorChoiceIdentifier.getAddress()).thenReturn(address1Identifier);
		when(customerRepository.update(customer)).thenReturn(Single.just(customer));
		when(customer.getStoreCode()).thenReturn(STORE_CODE);
		when(customer.getGuid()).thenReturn(CUSTOMERGUID);
		when(customer.getPreferredShippingAddress()).thenReturn(customerAddress);
		when(customerAddress.getGuid()).thenReturn(ADDRESS_GUID);
		when(cartOrder.getGuid()).thenReturn(CART_ORDER_GUID);
		when(cartOrderRepository.findCartOrderGuidsByCustomer(STORE_CODE, CUSTOMERGUID)).thenReturn(cartOrderGuidObservable);
		when(cartOrderRepository.updateShippingAddressOnCartOrder(ADDRESS_GUID, CART_ORDER_GUID, STORE_CODE))
				.thenReturn(Single.just(Boolean.TRUE));

		prototype.selectChoice(shippingAddressSelectorChoiceIdentifier)
				.test()
				.assertNoErrors();

		/* Check that we call the appropriate update and retrieval routines */
		verify(customerRepository, times(1)).update(customer);
	}
}
