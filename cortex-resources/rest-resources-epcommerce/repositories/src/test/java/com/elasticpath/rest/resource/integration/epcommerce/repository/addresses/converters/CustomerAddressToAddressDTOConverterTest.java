/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;

/**
 * Tests for CustomerAddressToAddressDTOConverter.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerAddressToAddressDTOConverterTest {

	private static final String GUID = "guid";
	private static final Date CREATION_DATE = new Date();
	private static final Date LAST_MODIFIED_DATE = new Date();
	private static final String COUNTRY_NAME = "US";
	private static final String STREET_ADDRESS = "streetAddress";
	private static final String EXTENDED_ADDREDD = "extendedAddress";
	private static final String CITY = "city";
	private static final String POSTAL_CODE = "postalCode";
	private static final String REGION = "CA";
	private static final String LAST_NAME = "lastName";
	private static final String FIRST_NAME = "firstName";
	private static final String PHONE_NUMBER = "phoneNumber";
	private static final String ORGANIZATION = "organization";

	@Mock
	private BeanFactory coreBeanFactory;

	@InjectMocks
	private CustomerAddressToAddressDTOConverter customerAddressToAddressDTOConverter;

	@Before
	public void setUp() {
		when(coreBeanFactory.getPrototypeBean(ContextIdNames.BASE_ADDRESS_DTO, AddressDTO.class)).thenReturn(new AddressDTO());
	}

	@Test
	public void addressDtoShouldContainValuesSameAsCustomerAddress() {
		final CustomerAddress customerAddress = mockCustomerAddress();

		final AddressDTO addressDTO = customerAddressToAddressDTOConverter.convert(customerAddress);

		assertThat(addressDTO.getGuid()).isEqualTo(GUID);
		assertThat(addressDTO.getCreationDate()).isEqualTo(CREATION_DATE);
		assertThat(addressDTO.getLastModifiedDate()).isEqualTo(LAST_MODIFIED_DATE);
		assertThat(addressDTO.getCountry()).isEqualTo(COUNTRY_NAME);
		assertThat(addressDTO.getZipOrPostalCode()).isEqualTo(POSTAL_CODE);
		assertThat(addressDTO.getStreet1()).isEqualTo(STREET_ADDRESS);
		assertThat(addressDTO.getStreet2()).isEqualTo(EXTENDED_ADDREDD);
		assertThat(addressDTO.getCity()).isEqualTo(CITY);
		assertThat(addressDTO.getSubCountry()).isEqualTo(REGION);
		assertThat(addressDTO.getLastName()).isEqualTo(LAST_NAME);
		assertThat(addressDTO.getFirstName()).isEqualTo(FIRST_NAME);
		assertThat(addressDTO.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
		assertThat(addressDTO.getOrganization()).isEqualTo(ORGANIZATION);
	}

	private CustomerAddress mockCustomerAddress() {
		final CustomerAddress customerAddress = new CustomerAddressImpl();

		customerAddress.setGuid(GUID);
		customerAddress.setCreationDate(CREATION_DATE);
		customerAddress.setLastModifiedDate(LAST_MODIFIED_DATE);
		customerAddress.setCountry(COUNTRY_NAME);
		customerAddress.setZipOrPostalCode(POSTAL_CODE);
		customerAddress.setStreet1(STREET_ADDRESS);
		customerAddress.setStreet2(EXTENDED_ADDREDD);
		customerAddress.setCity(CITY);
		customerAddress.setSubCountry(REGION);
		customerAddress.setLastName(LAST_NAME);
		customerAddress.setFirstName(FIRST_NAME);
		customerAddress.setPhoneNumber(PHONE_NUMBER);
		customerAddress.setOrganization(ORGANIZATION);

		return customerAddress;
	}

}