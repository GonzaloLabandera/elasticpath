/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Single;

import com.google.common.collect.Maps;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.profiles.AttributesIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ProfileAttributeFieldTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.service.customer.CustomerProfileAttributeService;

@RunWith(MockitoJUnitRunner.class)
public class ProfileAttributesEntityRepositoryImplTest {

	private static final String GIVEN_NAME = "given-name";
	private static final String GIVEN_NAME_VALUE = "Sage";
	private static final String MY_STORE = "my-store";

	private AttributesIdentifier attributeIdentifier;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private Customer customer;

	@Mock
	private CustomerProfileAttributeService customerProfileAttributeService;

	@Mock
	private ProfileAttributeFieldTransformer profileAttributeFieldTransformer;

	@Mock
	private CustomerProfileValueTransformer customerProfileValueTransformer;

	@InjectMocks
	private ProfileAttributesEntityRepositoryImpl repository;

	@Mock
	private CustomerProfileValue firstNameCustomerProfileValue;

	private final Map<String, Optional<CustomerProfileValue>> attributeValueMap = Maps.newHashMap();

	@Before
	public void setup() {
		attributeIdentifier = AttributesIdentifier
				.builder()
				.withProfile(ProfileIdentifier.builder().withProfileId(StringIdentifier.of("my-id"))
						.withScope(StringIdentifier.of(MY_STORE)).build())
				.build();
	}

	@Test
	public void shouldFailGetProfileWhenCustomerNotFoundError() {
		when(customerRepository.getCustomer(attributeIdentifier.getProfile().getProfileId().getValue()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.findOne(attributeIdentifier)
				.test()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void shouldGetProfile() {
		attributeValueMap.put(CustomerImpl.ATT_KEY_CP_FIRST_NAME, Optional.of(firstNameCustomerProfileValue));

		when(customerRepository.getCustomer(attributeIdentifier.getProfile().getProfileId().getValue())).thenReturn(Single.just(customer));
		when(customerProfileAttributeService.getCustomerReadOnlyAttributes(MY_STORE, customer))
				.thenReturn(attributeValueMap);

		when(profileAttributeFieldTransformer.transformToFieldName(CustomerImpl.ATT_KEY_CP_FIRST_NAME))
				.thenReturn(GIVEN_NAME);
		when(customerProfileValueTransformer.transformToString(firstNameCustomerProfileValue))
				.thenReturn(GIVEN_NAME_VALUE);

		repository.findOne(attributeIdentifier)
				.test()
				.assertNoErrors();

		verify(profileAttributeFieldTransformer).transformToFieldName(CustomerImpl.ATT_KEY_CP_FIRST_NAME);
		verify(customerProfileValueTransformer).transformToString(firstNameCustomerProfileValue);
	}
}