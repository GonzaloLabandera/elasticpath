/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.shoppingcart.CartType;
import com.elasticpath.xpf.connectivity.entity.XPFCartType;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;

/**
 * Converts {@code com.elasticpath.domain.catalog.CartType} to {@code com.elasticpath.xpf.connectivity.context.XPFCartType}.
 */
public class CartTypeConverter implements Converter<CartType, XPFCartType> {

	private ModifierGroupConverter xpfModifierGroupConverter;

	@Override
	public XPFCartType convert(final CartType cartType) {
		Set<XPFModifierGroup> modifierGroups = Optional.ofNullable(cartType.getModifiers()).map(modifiers ->
				modifiers.stream()
						.map(xpfModifierGroupConverter::convert)
						.collect(Collectors.toSet())
		).orElse(Collections.emptySet());

		return new XPFCartType(cartType.getName(), modifierGroups);
	}

	public void setXpfModifierGroupConverter(final ModifierGroupConverter xpfModifierGroupConverter) {
		this.xpfModifierGroupConverter = xpfModifierGroupConverter;
	}
}
