/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Objects;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.settings.domain.SettingValue;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValue;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValueTypeEnum;

/**
 * Converts {@code com.elasticpath.settings.domain.SettingValue} to {@code com.elasticpath.xpf.connectivity.context.SettingValue}.
 */
public class SettingValueConverter implements Converter<SettingValue, XPFSettingValue> {
	@Override
	public XPFSettingValue convert(final SettingValue settingValue) {

		return new XPFSettingValue(
				Objects.requireNonNull(settingValue.getValue(), "Required field value of core entity SettingValue is missing."),
				XPFSettingValueTypeEnum.valueForTypeKey(
						Objects.requireNonNull(settingValue.getValueType(), "Required field valueType of core entity SettingValue is missing.")));
	}
}
