/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Test of the public API of <code>CustomerAddressImpl</code>.
 */
public class AddressImplTest {

	private final CustomerAddressImpl addressImpl = new CustomerAddressImpl();

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getCountry()'.
	 */
	@Test
	public void testGetFirstName() {
		assertThat(addressImpl.getFirstName()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setCountry(String)'.
	 */
	@Test
	public void testSetFirstName() {
		final String name = "test name";
		addressImpl.setFirstName(name);
		assertThat(addressImpl.getFirstName()).isEqualTo(name);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getCountry()'.
	 */
	@Test
	public void testGetLastName() {
		assertThat(addressImpl.getLastName()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setCountry(String)'.
	 */
	@Test
	public void testSetLastName() {
		final String name = "test name";
		addressImpl.setLastName(name);
		assertThat(addressImpl.getLastName()).isEqualTo(name);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getStreet()'.
	 */
	@Test
	public void testGetStreet1() {
		assertThat(addressImpl.getStreet1()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setStreet(String)'.
	 */
	@Test
	public void testSetStreet1() {
		final String street = "test street";
		addressImpl.setStreet1(street);
		assertThat(addressImpl.getStreet1()).isEqualTo(street);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getStreet()'.
	 */
	@Test
	public void testGetStreet2() {
		assertThat(addressImpl.getStreet2()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setStreet(String)'.
	 */
	@Test
	public void testSetStreet2() {
		final String street = "test street";
		addressImpl.setStreet2(street);
		assertThat(addressImpl.getStreet2()).isEqualTo(street);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getCountry()'.
	 */
	@Test
	public void testGetCountry() {
		assertThat(addressImpl.getCountry()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setCountry(String)'.
	 */
	@Test
	public void testSetCountry() {
		final String country = "test country";
		addressImpl.setCountry(country);
		assertThat(addressImpl.getCountry()).isEqualToIgnoringCase(country);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getPhoneNumber()'.
	 */
	@Test
	public void testGetPhoneNumber() {
		assertThat(addressImpl.getPhoneNumber()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setPhoneNumber(String)'.
	 */
	@Test
	public void testSetPhoneNumber() {
		final String phoneNumber = "888-8888-888";
		addressImpl.setPhoneNumber(phoneNumber);
		assertThat(addressImpl.getPhoneNumber()).isEqualTo(phoneNumber);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getFaxNumber()'.
	 */
	@Test
	public void testGetFaxNumber() {
		assertThat(addressImpl.getFaxNumber()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setFaxNumber(String)'.
	 */
	@Test
	public void testSetFaxNumber() {
		final String faxNumber = "999-9999-999";
		addressImpl.setFaxNumber(faxNumber);
		assertThat(addressImpl.getFaxNumber()).as("Check set fax number").isEqualTo(faxNumber);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getCity()'.
	 */
	@Test
	public void testGetCity() {
		assertThat(addressImpl.getCity()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setCity(String)'.
	 */
	@Test
	public void testSetCity() {
		final String city = "test city";
		addressImpl.setCity(city);
		assertThat(addressImpl.getCity()).isEqualTo(city);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getStateOrProvince()'.
	 */
	@Test
	public void testGetSubCountry() {
		assertThat(addressImpl.getSubCountry()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setStateOrProvince(String)'.
	 */
	@Test
	public void testSetSubCountry() {
		final String stateOrProvince = "test state or province";
		addressImpl.setSubCountry(stateOrProvince);
		assertThat(addressImpl.getSubCountry()).isEqualToIgnoringCase(stateOrProvince);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getZipOrPostalCode()'.
	 */
	@Test
	public void testGetZipOrPostalCode() {
		assertThat(addressImpl.getZipOrPostalCode()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setZipOrPostalCode(String)'.
	 */
	@Test
	public void testSetZipOrPostalCode() {
		final String zipOrPostalCode = "test zip or postal code";
		addressImpl.setZipOrPostalCode(zipOrPostalCode);
		assertThat(addressImpl.getZipOrPostalCode()).isEqualTo(zipOrPostalCode);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getCommercialAddress()' and
	 * 'com.elasticpath.domain.impl.CustomerAddressImpl.setCommercialAddress()'.
	 */
	@Test
	public void testGetCommercialAddress() {
		assertThat(addressImpl.isCommercialAddress()).isFalse();
		addressImpl.setCommercialAddress(true);
		assertThat(addressImpl.isCommercialAddress()).isTrue();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.getOrganization()'.
	 */
	@Test
	public void testGetOrganization() {
		assertThat(addressImpl.getOrganization()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CustomerAddressImpl.setOrganization(String)'.
	 */
	@Test
	public void testSetOrganization() {
		final String organization = "test organization";
		addressImpl.setOrganization(organization);
		assertThat(addressImpl.getOrganization()).isEqualTo(organization);
	}
}
