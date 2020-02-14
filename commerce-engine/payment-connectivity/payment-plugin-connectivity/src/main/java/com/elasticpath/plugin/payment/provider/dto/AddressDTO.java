/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

/**
 * DTO for Customer Addresses for use in Payment Gateways.
 */
public class AddressDTO {
	private String guid;
	private String lastName;
	private String firstName;
	private String phoneNumber;
	private String street1;
	private String street2;
	private String city;
	private String subCountry;
	private String zipOrPostalCode;
	private String country;

	/**
	 * Get Address guid.
	 *
	 * @return address guid.
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Set Address guid.
	 *
	 * @param addressGuid addressGuid.
	 */
	public void setGuid(final String addressGuid) {
		this.guid = addressGuid;
	}

	/**
	 * Gets the last name associated with this <code>Address</code>.
	 *
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name associated with this <code>Address</code>.
	 *
	 * @param lastName the last name.
	 */
	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Gets the first name associated with this <code>Address</code>.
	 *
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name associated with this <code>Address</code>.
	 *
	 * @param firstName the first name.
	 */
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the phone number associated with this <code>Address</code>.
	 *
	 * @return the phone number.
	 */
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Sets the phone number associated with this <code>Address</code>.
	 *
	 * @param phoneNumber the new phone number.
	 */
	public void setPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Gets the streetname of this <code>Address</code>.
	 *
	 * @return the streetname.
	 */
	public String getStreet1() {
		return street1;
	}

	/**
	 * Sets the streetname of this <code>Address</code>.
	 *
	 * @param street the new streetname.
	 */
	public void setStreet1(final String street) {
		this.street1 = street;
	}

	/**
	 * Gets the streetname of this <code>Address</code>.
	 *
	 * @return the streetname.
	 */
	public String getStreet2() {
		return street2;
	}

	/**
	 * Sets the streetname of this <code>Address</code>.
	 *
	 * @param street the new streetname.
	 */
	public void setStreet2(final String street) {
		this.street2 = street;
	}

	/**
	 * Gets the city of this <code>Address</code>.
	 *
	 * @return the city.
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Sets the city of this <code>Address</code>.
	 *
	 * @param city the new city.
	 */
	public void setCity(final String city) {
		this.city = city;
	}

	/**
	 * Gets the state, province, or other region of the country for this <code>Address</code>.
	 *
	 * @return the state, province, or other region of the country
	 */
	public String getSubCountry() {
		return subCountry;
	}

	/**
	 * Sets the state, province, or other region of the country for this <code>Address</code>.
	 *
	 * @param subCountry the new state, province, or other region
	 */
	public void setSubCountry(final String subCountry) {
		this.subCountry = subCountry;
	}

	/**
	 * Gets the zip/postal code of this <code>Address</code>.
	 *
	 * @return the zip/postal code.
	 */
	public String getZipOrPostalCode() {
		return zipOrPostalCode;
	}

	/**
	 * Sets the zip/postal code of this <code>Address</code>.
	 *
	 * @param zipOrPostalCode the new zip/postal code.
	 */
	public void setZipOrPostalCode(final String zipOrPostalCode) {
		this.zipOrPostalCode = zipOrPostalCode;
	}

	/**
	 * Gets the country of this <code>Address</code>.
	 *
	 * @return the country.
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * Sets the country of this <code>Address</code>.
	 *
	 * @param country the new country.
	 */
	public void setCountry(final String country) {
		this.country = country;
	}

}
