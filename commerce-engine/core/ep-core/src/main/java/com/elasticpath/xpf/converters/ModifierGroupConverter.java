/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierGroup;

/**
 * Converts {@code com.elasticpath.domain.modifier.ModifierGroup} to {@code com.elasticpath.xpf.connectivity.context.XPFModifierGroup}.
 */
public class ModifierGroupConverter implements Converter<ModifierGroup, XPFModifierGroup> {

	private ModifierFieldConverter xpfModifierFieldConverter;

	@Override
	public XPFModifierGroup convert(final ModifierGroup modifierGroup) {
		List<XPFModifierField> modifierFieldsResult = Optional.ofNullable(modifierGroup.getModifierFields())
				.map(modifierFields ->
						modifierFields.stream()
							.map(xpfModifierFieldConverter::convert)
							.collect(Collectors.toList())
				).orElse(Collections.emptyList());

		return new XPFModifierGroup(modifierFieldsResult);
	}

	public void setXpfModifierFieldConverter(final ModifierFieldConverter xpfModifierFieldConverter) {
		this.xpfModifierFieldConverter = xpfModifierFieldConverter;
	}
}
