/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.shipping.connectivity.dto.ShippingOption;
import com.elasticpath.xpf.connectivity.entity.XPFShippingOption;

/**
 * Converts {@code com.elasticpath.domain.shopper.ShippingOption} to {@code com.elasticpath.xpf.connectivity.context.ShippingOption}.
 */
public class ShippingOptionConverter implements Converter<ShippingOption, XPFShippingOption> {

	@Override
	public XPFShippingOption convert(final ShippingOption shippingOption) {
		return new XPFShippingOption(shippingOption.getCode(), shippingOption.getCarrierCode().orElse(null));
	}
}
