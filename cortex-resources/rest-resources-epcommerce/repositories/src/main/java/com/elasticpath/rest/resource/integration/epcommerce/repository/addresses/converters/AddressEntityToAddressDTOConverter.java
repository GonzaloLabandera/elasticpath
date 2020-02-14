/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.converters;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;

/**
 * Converter for AddressEntity.
 */
@Singleton
@Named
public class AddressEntityToAddressDTOConverter implements Converter<AddressEntity, AddressDTO> {

	private final BeanFactory coreBeanFactory;

	/**
	 * Constructor.
	 *
	 * @param coreBeanFactory coreBeanFactory
	 */
	@Inject
	public AddressEntityToAddressDTOConverter(
			@Named("coreBeanFactory") final BeanFactory coreBeanFactory) {
		this.coreBeanFactory = coreBeanFactory;
	}

	@Override
	public AddressDTO convert(final AddressEntity addressEntity) {
		AddressDTO addressDTO = coreBeanFactory.getPrototypeBean(ContextIdNames.BASE_ADDRESS_DTO, AddressDTO.class);
		com.elasticpath.rest.definition.base.AddressEntity address = addressEntity.getAddress();
		if (address != null) {
			String countryName = address.getCountryName();
			String extendedAddress = address.getExtendedAddress();
			String locality = address.getLocality();
			String postalCode = address.getPostalCode();
			String region = address.getRegion();
			String streetAddress = address.getStreetAddress();

			addressDTO.setCountry(countryName);
			addressDTO.setZipOrPostalCode(postalCode);
			addressDTO.setStreet1(streetAddress);
			addressDTO.setStreet2(extendedAddress);
			addressDTO.setCity(locality);
			addressDTO.setSubCountry(region);
		}

		NameEntity addressEntityName = addressEntity.getName();
		if (addressEntityName != null) {
			addressDTO.setLastName(addressEntityName.getFamilyName());
			addressDTO.setFirstName(addressEntityName.getGivenName());
		}

		addressDTO.setPhoneNumber(addressEntity.getPhoneNumber());
		addressDTO.setOrganization(addressEntity.getOrganization());

		return addressDTO;
	}
}
