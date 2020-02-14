/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.converters;

import java.util.Date;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerAddress;

/**
 * Converter for AddressEntity.
 */
@Singleton
@Named
public class CustomerAddressToAddressDTOConverter implements Converter<CustomerAddress, AddressDTO> {

	private final BeanFactory coreBeanFactory;

	/**
	 * Constructor.
	 *
	 * @param coreBeanFactory coreBeanFactory
	 */
	@Inject
	public CustomerAddressToAddressDTOConverter(
			@Named("coreBeanFactory") final BeanFactory coreBeanFactory) {
		this.coreBeanFactory = coreBeanFactory;
	}

	@Override
	public AddressDTO convert(final CustomerAddress customerAddress) {
		AddressDTO addressDTO = coreBeanFactory.getPrototypeBean(ContextIdNames.BASE_ADDRESS_DTO, AddressDTO.class);
		String guid = customerAddress.getGuid();
		Date creationDate = customerAddress.getCreationDate();
		Date lastModifiedDate = customerAddress.getLastModifiedDate();
		String countryName = customerAddress.getCountry();
		String streetAddress = customerAddress.getStreet1();
		String extendedAddress = customerAddress.getStreet2();
		String city = customerAddress.getCity();
		String postalCode = customerAddress.getZipOrPostalCode();
		String region = customerAddress.getSubCountry();
		String lastName = customerAddress.getLastName();
		String firstName = customerAddress.getFirstName();
		String phoneNumber = customerAddress.getPhoneNumber();
		String organization = customerAddress.getOrganization();

		addressDTO.setGuid(guid);
		addressDTO.setCreationDate(creationDate);
		addressDTO.setLastModifiedDate(lastModifiedDate);
		addressDTO.setCountry(countryName);
		addressDTO.setZipOrPostalCode(postalCode);
		addressDTO.setStreet1(streetAddress);
		addressDTO.setStreet2(extendedAddress);
		addressDTO.setCity(city);
		addressDTO.setSubCountry(region);
		addressDTO.setLastName(lastName);
		addressDTO.setFirstName(firstName);
		addressDTO.setPhoneNumber(phoneNumber);
		addressDTO.setOrganization(organization);

		return addressDTO;
	}
}
