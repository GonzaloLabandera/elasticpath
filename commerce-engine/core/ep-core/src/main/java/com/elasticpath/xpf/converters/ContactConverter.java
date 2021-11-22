/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.xpf.connectivity.entity.XPFContact;

/**
 * Converts {@code com.elasticpath.domain.customer.Address} to {@code com.elasticpath.xpf.connectivity.context.Contact}.
 */
public class ContactConverter implements Converter<Address, XPFContact> {

	@Override
	public XPFContact convert(final Address address) {
		if (address != null) {
			return new XPFContact(address.getFirstName(),
					address.getLastName(),
					address.getOrganization(),
					address.getPhoneNumber(),
					address.getFaxNumber());
		}
		return null;
	}
}

