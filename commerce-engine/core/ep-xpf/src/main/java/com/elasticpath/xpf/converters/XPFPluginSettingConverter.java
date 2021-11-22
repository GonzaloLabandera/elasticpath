/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.xpf.connectivity.context.XPFSettingCollectionType;
import com.elasticpath.xpf.connectivity.context.XPFSettingDataType;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSetting;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSettingValue;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.dto.PluginSettingValueDTO;

/**
 * Converts {@code com.elasticpath.xpf.entity.Setting} to {@code com.elasticpath.xpf.connectivity.context.XPFSetting}.
 */
public class XPFPluginSettingConverter implements Converter<PluginSettingDTO, XPFPluginSetting> {
	@Override
	public XPFPluginSetting convert(final PluginSettingDTO setting) {
		final List<XPFPluginSettingValue> values = setting.getSettingValues().stream().map(this::createValue).collect(Collectors.toList());

		return new XPFPluginSetting(setting.getSettingKey(), XPFSettingDataType.valueOf(setting.getDataType().name()),
				XPFSettingCollectionType.valueOf(setting.getCollectionType().name()), values);
	}

	private XPFPluginSettingValue createValue(final PluginSettingValueDTO value) {

		return new XPFPluginSettingValue(value.getSequence(),
				value.getMapKey(),
				value.getShortTextValue(),
				value.getIntegerValue(),
				value.getDecimalValue(),
				value.getBooleanValue(),
				value.getDateValue());
	}
}
