/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.converters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.xpf.connectivity.entity.XPFAddress;

@RunWith(MockitoJUnitRunner.class)
public class AddressConverterTest {

	private static final String STREET_ADDRESS1 = "street address 1";
	private static final String STREET_ADDRESS2 = "street address 2";
	private static final String CITY = "city";
	private static final String COUNTRY = "country";
	private static final String SUB_COUNTRY = "subcountry";
	private static final String ZIP_CODE = "zip code";

	@Mock
	private Address address;

	private final AddressConverter addressConverter = new AddressConverter();

	@Test
	public void testConvertWithFullInputs() {
		when(address.getStreet1()).thenReturn(STREET_ADDRESS1);
		when(address.getStreet2()).thenReturn(STREET_ADDRESS2);
		when(address.getCity()).thenReturn(CITY);
		when(address.getCountry()).thenReturn(COUNTRY);
		when(address.getSubCountry()).thenReturn(SUB_COUNTRY);
		when(address.getZipOrPostalCode()).thenReturn(ZIP_CODE);

		XPFAddress contextAddress = addressConverter.convert(address);
		assertEquals(STREET_ADDRESS1, contextAddress.getStreet1());
		assertEquals(STREET_ADDRESS2, contextAddress.getStreet2());
		assertEquals(CITY, contextAddress.getCity());
		assertEquals(COUNTRY, contextAddress.getCountry());
		assertEquals(SUB_COUNTRY, contextAddress.getSubCountry());
		assertEquals(ZIP_CODE, contextAddress.getZipOrPostalCode());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(address.getStreet1()).thenReturn(STREET_ADDRESS1);
		when(address.getStreet2()).thenReturn(null);
		when(address.getCity()).thenReturn(CITY);
		when(address.getCountry()).thenReturn(COUNTRY);
		when(address.getSubCountry()).thenReturn(null);
		when(address.getZipOrPostalCode()).thenReturn(ZIP_CODE);

		XPFAddress contextAddress = addressConverter.convert(address);
		assertThat(contextAddress.getStreet1()).isEqualTo(STREET_ADDRESS1);
		assertNull(contextAddress.getStreet2());
		assertThat(contextAddress.getCity()).isEqualTo(CITY);
		assertThat(contextAddress.getCountry()).isEqualTo(COUNTRY);
		assertNull(contextAddress.getSubCountry());
		assertThat(contextAddress.getZipOrPostalCode()).isEqualTo(ZIP_CODE);
	}
}