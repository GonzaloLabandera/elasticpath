/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.definition.profiles.ProfileEntity;

/**
 * Test that the Profile Entity Converter behaves as expected.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileEntityConverterTest {

	@Mock
	private Customer customer;

	private final Converter<Customer, ProfileEntity> converter = new ProfileEntityConverter();

	@Test
	public void ensureConvertChangesNullsToEmptyStrings() {
		when(customer.getGuid()).thenReturn(UUID.randomUUID().toString());
		when(customer.getFirstName()).thenReturn(null);
		when(customer.getLastName()).thenReturn(null);

		ProfileEntity result = converter.convert(customer);
		assertThat(result.getGivenName()).isEmpty();
		assertThat(result.getFamilyName()).isEmpty();
	}
}