/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.xpf.connectivity.entity.XPFAddress;

/**
 * Converts {@code com.elasticpath.domain.customer.Address} to {@code com.elasticpath.xpf.connectivity.context.Address}.
 */
public class AddressConverter implements Converter<Address, XPFAddress> {

	@Override
	public XPFAddress convert(final Address address) {
		if (address != null) {
			return new XPFAddress(
					address.getStreet1(),
					address.getStreet2(),
					address.getCity(),
					address.getCountry(),
					address.getSubCountry(),
					address.getZipOrPostalCode()
			);
		}
		return null;
	}
}
