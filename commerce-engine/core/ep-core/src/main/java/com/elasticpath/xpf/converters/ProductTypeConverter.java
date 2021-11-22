/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;

/**
 * Converts {@code com.elasticpath.domain.catalog.ProductType} to {@code com.elasticpath.xpf.connectivity.context.XPFProductType}.
 */
public class ProductTypeConverter implements Converter<ProductType, XPFProductType> {

	private ModifierGroupConverter xpfModifierGroupConverter;

	@Override
	public XPFProductType convert(final ProductType productType) {
		Set<XPFModifierGroup> xpfModifierGroups = productType.getModifierGroups().stream()
				.map(xpfModifierGroupConverter::convert)
				.collect(Collectors.toSet());

		return new XPFProductType(xpfModifierGroups);
	}

	public void setXpfModifierGroupConverter(final ModifierGroupConverter xpfModifierGroupConverter) {
		this.xpfModifierGroupConverter = xpfModifierGroupConverter;
	}
}
