/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.xpf.connectivity.entity.XPFContact;

@RunWith(MockitoJUnitRunner.class)
public class ContactConverterTest {

	private static final String FIRST_NAME = "FirstName";
	private static final String LAST_NAME = "LastName";
	private static final String ORGANIZATION = "organization";
	private static final String PHONE_NUMBER = "phone number";
	private static final String FAX_NUMBER = "fax number";

	@Mock
	private Address address;

	private final ContactConverter contactConverter = new ContactConverter();

	@Test
	public void testConvertWithFullInputs() {
		when(address.getFirstName()).thenReturn(FIRST_NAME);
		when(address.getLastName()).thenReturn(LAST_NAME);
		when(address.getOrganization()).thenReturn(ORGANIZATION);
		when(address.getPhoneNumber()).thenReturn(PHONE_NUMBER);
		when(address.getFaxNumber()).thenReturn(FAX_NUMBER);

		XPFContact contextContact = contactConverter.convert(address);
		assertEquals(FIRST_NAME, contextContact.getFirstName());
		assertEquals(LAST_NAME, contextContact.getLastName());
		assertEquals(ORGANIZATION, contextContact.getOrganization());
		assertEquals(PHONE_NUMBER, contextContact.getPhoneNumber());
		assertEquals(FAX_NUMBER, contextContact.getFaxNumber());
	}

	@Test
	public void testConvertWithMinInputs() {
		when(address.getFirstName()).thenReturn(null);
		when(address.getLastName()).thenReturn(null);
		when(address.getOrganization()).thenReturn(null);
		when(address.getPhoneNumber()).thenReturn(null);
		when(address.getFaxNumber()).thenReturn(null);

		XPFContact contextContact = contactConverter.convert(address);
		assertNull(contextContact.getFirstName());
		assertNull(contextContact.getLastName());
		assertNull(contextContact.getOrganization());
		assertNull(contextContact.getPhoneNumber());
		assertNull(contextContact.getFaxNumber());
	}
}