/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.builder.customer;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.DomainObjectBuilder;
import com.elasticpath.domain.customer.CustomerAddress;

/**
 * A builder that builds {@link CustomerAddress}s for testing purposes.
 */
public class CustomerAddressBuilder implements DomainObjectBuilder<CustomerAddress> {

	@Autowired
    private BeanFactory beanFactory;

	private long uidPk;
	private String firstName;
	private String lastName;
	private String city;
	private String country;
	private String faxNumber;
	private String phoneNumber;
	private String subCountry;
	private String street1;
	private String street2;
	private String zipOrPostalCode;
	private boolean commercialAddress;
	private String guid;
	private String organization;

	public CustomerAddressBuilder withUidPk(final Long uidPk) {
		this.uidPk = uidPk;
		return this;
	}

	public CustomerAddressBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

    public CustomerAddressBuilder withFirstName(final String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CustomerAddressBuilder withLastName(final String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public CustomerAddressBuilder withCity(final String city) {
        this.city = city;
        return this;
    }

    public CustomerAddressBuilder withCountry(final String country) {
        this.country = country;
        return this;
    }
    
    public CustomerAddressBuilder withFaxNumber(final String faxNumber) {
        this.faxNumber = faxNumber;
        return this;
    }
    
    public CustomerAddressBuilder withPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    
    public CustomerAddressBuilder withSubCountry(final String subCountry) {
		this.subCountry = subCountry;
		return this;
	}

    public CustomerAddressBuilder withStreet1(final String street1) {
        this.street1 = street1;
        return this;
    }

    public CustomerAddressBuilder withStreet2(final String street2) {
        this.street2 = street2;
        return this;
    }
    
    public CustomerAddressBuilder withZipOrPostalCode(final String zipOrPostalCode) {
        this.zipOrPostalCode = zipOrPostalCode;
        return this;
    }

    public CustomerAddressBuilder withCommercialAddress(final Boolean commercialAddress) {
        this.commercialAddress = commercialAddress;
        return this;
    }
    
    public CustomerAddressBuilder withOrganization(final String organization) {
        this.organization = organization;
        return this;
    }

    @Override
    public CustomerAddress build() {
    	CustomerAddress customerAddress = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER_ADDRESS, CustomerAddress.class);
		customerAddress.setGuid(ObjectUtils.defaultIfNull(guid, "testGuid"));
		customerAddress.setUidPk(ObjectUtils.defaultIfNull(uidPk, 0L));
		customerAddress.setFirstName(ObjectUtils.defaultIfNull(firstName, "James"));
		customerAddress.setLastName(ObjectUtils.defaultIfNull(lastName, "Bond"));
        customerAddress.setCity(ObjectUtils.defaultIfNull(city, "Vancouver"));
        customerAddress.setCountry(ObjectUtils.defaultIfNull(country, "CA"));
        customerAddress.setSubCountry(ObjectUtils.defaultIfNull(subCountry, "BC"));
        customerAddress.setStreet1(ObjectUtils.defaultIfNull(street1, "1 Test Street"));
        customerAddress.setStreet2(ObjectUtils.defaultIfNull(street2, "Street2"));
        customerAddress.setCommercialAddress(ObjectUtils.defaultIfNull(commercialAddress, Boolean.FALSE));
        customerAddress.setZipOrPostalCode(ObjectUtils.defaultIfNull(zipOrPostalCode, "V1V 0C0"));
        customerAddress.setOrganization(ObjectUtils.defaultIfNull(organization, "Organization"));
        customerAddress.setPhoneNumber(ObjectUtils.defaultIfNull(phoneNumber, "6045555555"));
        customerAddress.setFaxNumber(ObjectUtils.defaultIfNull(faxNumber, "6045555555"));

        return customerAddress;
    }
}
