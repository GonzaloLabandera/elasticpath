/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierField;
import com.elasticpath.xpf.connectivity.entity.XPFModifierFieldOption;

/**
 * Converts {@code com.elasticpath.domain.modifier.ModifierField} to {@code com.elasticpath.xpf.connectivity.context.XPFModifierField}.
 */
public class ModifierFieldConverter implements Converter<ModifierField, XPFModifierField> {

	private ModifierFieldOptionConverter xpfModifierFieldOptionConverter;

	@Override
	public XPFModifierField convert(final ModifierField modifierField) {
		Set<XPFModifierFieldOption> modifierFieldOptions = modifierField.getModifierFieldOptions().stream()
				.map(xpfModifierFieldOptionConverter::convert)
				.collect(Collectors.toSet());
		return new XPFModifierField(modifierField.getCode(), modifierField.isRequired(), modifierField.getMaxSize(),
				modifierField.getFieldType().getName(), modifierFieldOptions);
	}

	public void setXpfModifierFieldOptionConverter(final ModifierFieldOptionConverter xpfModifierFieldOptionConverter) {
		this.xpfModifierFieldOptionConverter = xpfModifierFieldOptionConverter;
	}
}
