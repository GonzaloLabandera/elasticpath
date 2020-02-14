/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

/**
 * An address DTO builder.
 */
public final class AddressDTOBuilder {
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

	private AddressDTOBuilder() {
	}

	/**
	 * An address builder.
	 *
	 * @return the builder
	 */
	public static AddressDTOBuilder builder() {
		return new AddressDTOBuilder();
	}

	/**
	 * With guid builder.
	 *
	 * @param guid the last name.
	 * @return the builder
	 */
	public AddressDTOBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With lastName builder.
	 *
	 * @param lastName the last name.
	 * @return the builder
	 */
	public AddressDTOBuilder withLastName(final String lastName) {
		this.lastName = lastName;
		return this;
	}

	/**
	 * With firstName builder.
	 *
	 * @param firstName the first name.
	 * @return the builder
	 */
	public AddressDTOBuilder withFirstName(final String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * With phoneNumber builder.
	 *
	 * @param phoneNumber the new phone number.
	 * @return the builder
	 */
	public AddressDTOBuilder withPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	/**
	 * With street1 builder.
	 *
	 * @param street the new streetname.
	 * @return the builder
	 */
	public AddressDTOBuilder withStreet1(final String street) {
		this.street1 = street;
		return this;
	}

	/**
	 * With street2 builder.
	 *
	 * @param street the new streetname.
	 * @return the builder
	 */
	public AddressDTOBuilder withStreet2(final String street) {
		this.street2 = street;
		return this;
	}

	/**
	 * With city builder.
	 *
	 * @param city the new city.
	 * @return the builder
	 */
	public AddressDTOBuilder withCity(final String city) {
		this.city = city;
		return this;
	}

	/**
	 * With subCountry builder.
	 *
	 * @param subCountry the new state, province, or other region
	 * @return the builder
	 */
	public AddressDTOBuilder withSubCountry(final String subCountry) {
		this.subCountry = subCountry;
		return this;
	}

	/**
	 * With zipOrPostalCode builder.
	 *
	 * @param zipOrPostalCode the new zip/postal code.
	 * @return the builder
	 */
	public AddressDTOBuilder withZipOrPostalCode(final String zipOrPostalCode) {
		this.zipOrPostalCode = zipOrPostalCode;
		return this;
	}

	/**
	 * With country builder.
	 *
	 * @param country the new country.
	 * @return the builder
	 */
	public AddressDTOBuilder withCountry(final String country) {
		this.country = country;
		return this;
	}

	/**
	 * Build address DTO.
	 *
	 * @param prototype bean prototype
	 * @return address DTO
	 */
	public AddressDTO build(final AddressDTO prototype) {
		if (guid == null) {
			throw new IllegalStateException("Builder is not fully initialized, guid is missing");
		}
		prototype.setGuid(guid);
		prototype.setCity(city);
		prototype.setCountry(country);
		prototype.setFirstName(firstName);
		prototype.setLastName(lastName);
		prototype.setPhoneNumber(phoneNumber);
		prototype.setStreet1(street1);
		prototype.setStreet2(street2);
		prototype.setSubCountry(subCountry);
		prototype.setZipOrPostalCode(zipOrPostalCode);
		return prototype;
	}
}
