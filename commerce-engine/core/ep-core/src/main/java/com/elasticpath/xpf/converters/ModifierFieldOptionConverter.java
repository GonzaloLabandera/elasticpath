/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.modifier.ModifierFieldOption;
import com.elasticpath.xpf.connectivity.entity.XPFModifierFieldOption;

/**
 * Converts {@code com.elasticpath.domain.modifier.ModifierFieldOption} to {@code com.elasticpath.xpf.connectivity.context.XPFModifierFieldOption}.
 */
public class ModifierFieldOptionConverter implements Converter<ModifierFieldOption, XPFModifierFieldOption> {

	@Override
	public XPFModifierFieldOption convert(final ModifierFieldOption modifierFieldOption) {
		return new XPFModifierFieldOption(modifierFieldOption.getValue());
	}
}
