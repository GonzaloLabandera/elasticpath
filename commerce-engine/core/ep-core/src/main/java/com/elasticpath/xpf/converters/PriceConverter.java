/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.catalog.Price;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;

/**
 * Converts {@code com.elasticpath.domain.catalog.Price} to {@code com.elasticpath.xpf.connectivity.context.Price}.
 */
public class PriceConverter implements Converter<Price, XPFPrice> {
	@Override
	public XPFPrice convert(final Price price) {
		if (price == null) {
			return null;
		}
		return new XPFPrice(
				price.getListPrice() == null ? null : price.getListPrice().getAmount(),
				price.getSalePrice() == null ? null : price.getSalePrice().getAmount(),
				price.getCurrency());
	}
}