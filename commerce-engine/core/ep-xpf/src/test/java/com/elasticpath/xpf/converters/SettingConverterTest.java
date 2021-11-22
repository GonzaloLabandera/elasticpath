/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import com.elasticpath.xpf.connectivity.context.XPFSettingCollectionType;
import com.elasticpath.xpf.connectivity.context.XPFSettingDataType;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSetting;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSettingValue;
import com.elasticpath.xpf.dto.PluginSettingDTO;
import com.elasticpath.xpf.dto.PluginSettingValueDTO;
import com.elasticpath.xpf.dto.SettingCollectionTypeDTO;
import com.elasticpath.xpf.dto.SettingDataTypeDTO;

public class SettingConverterTest {

	private static final Date DATE_VALUE = new Date();
	private static final String MAP_KEY = "mapKey";
	private static final String SHORT_TEXT_VALUE = "text";
	private static final String SETTING_KEY = "settingKey";

	@Test
	public void test() {
		PluginSettingDTO setting = createSetting();

		XPFPluginSettingConverter settingConverter = new XPFPluginSettingConverter();
		XPFPluginSetting result = settingConverter.convert(setting);

		assertEquals(XPFSettingCollectionType.SINGLE, result.getCollectionType());
		assertEquals(XPFSettingDataType.DATE, result.getDataType());
		assertEquals(SETTING_KEY, result.getSettingKey());

		assertEquals(1, result.getSettingValues().size());
		XPFPluginSettingValue value = result.getSettingValues().get(0);
		assertEquals(Integer.valueOf(1), value.getIntegerValue());
		assertEquals(1, value.getSequence());
		assertTrue(value.getBooleanValue());
		assertEquals(DATE_VALUE, value.getDateValue());
		assertEquals(BigDecimal.ONE.stripTrailingZeros(), value.getDecimalValue().stripTrailingZeros());
		assertEquals(SHORT_TEXT_VALUE, value.getShortTextValue());
		assertEquals(MAP_KEY, value.getMapKey());
	}

	private PluginSettingDTO createSetting() {
		PluginSettingValueDTO settingValue = new PluginSettingValueDTO(1, MAP_KEY,
				SHORT_TEXT_VALUE, 1, BigDecimal.ONE, true, DATE_VALUE);

		PluginSettingDTO setting = new PluginSettingDTO(
				SETTING_KEY,
				SettingDataTypeDTO.DATE,
				SettingCollectionTypeDTO.SINGLE,
				Collections.singletonList(settingValue)
		);


		return setting;
	}
}
