/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.definition.profiles.ProfileEntity;

/**
 * Converter for ProfileEntity.
 */
@Singleton
@Named
public class ProfileEntityConverter implements Converter<Customer, ProfileEntity> {

	@Override
	public ProfileEntity convert(final Customer customer) {
		return ProfileEntity.builder()
				.withProfileId(customer.getGuid())
				.withGivenName(ObjectUtils.defaultIfNull(customer.getFirstName(), ""))
				.withFamilyName(ObjectUtils.defaultIfNull(customer.getLastName(), ""))
				.build();
	}

}