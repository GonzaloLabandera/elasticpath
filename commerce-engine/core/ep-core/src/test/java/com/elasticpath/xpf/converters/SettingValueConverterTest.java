/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.elasticpath.settings.domain.SettingDefinition;
import com.elasticpath.settings.domain.impl.SettingDefinitionImpl;
import com.elasticpath.settings.domain.impl.SettingValueImpl;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValueTypeEnum;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValue;
import com.elasticpath.xpf.connectivity.exception.XPFPluginRuntimeException;

public class SettingValueConverterTest {

	public static final int TEST_UID_PK = 3;
	private final SettingValueConverter settingValueConverter = new SettingValueConverter();

	@Rule
	public ExpectedException exceptionThrown = ExpectedException.none();

	@Test
	public void testConvertWithFullInputs() {
		String value = "1";
		String valueType = "Integer";
		SettingDefinition settingDefinition = new SettingDefinitionImpl();
		settingDefinition.setDefaultValue("2");
		settingDefinition.setDescription("test description");
		settingDefinition.setValueType(valueType);
		settingDefinition.setMetadata(new HashMap<>());
		settingDefinition.setPath("COMMERCE/TEST/isTest");
		settingDefinition.setMaxOverrideValues(0);
		settingDefinition.setUidPk(TEST_UID_PK);

		SettingValueImpl settingValue = new SettingValueImpl();
		settingValue.setValue(value);
		settingValue.setContext("MOBEE");
		settingValue.setUidPk(1);
		settingValue.setSettingDefinition(settingDefinition);

		XPFSettingValue xpfSettingValue = settingValueConverter.convert(settingValue);

		assertEquals(value, xpfSettingValue.getValue());
		assertEquals(XPFSettingValueTypeEnum.INTEGER, xpfSettingValue.getValueType());
	}

	@Test
	public void testConvertWithMinInputs() {
		String value = "1";
		String valueType = "Integer";
		SettingDefinition settingDefinition = new SettingDefinitionImpl();
		settingDefinition.setValueType(valueType);
		SettingValueImpl settingValue = new SettingValueImpl();
		settingValue.setSettingDefinition(settingDefinition);
		settingValue.setValue(value);

		XPFSettingValue xpfSettingValue = settingValueConverter.convert(settingValue);

		assertEquals(value, xpfSettingValue.getValue());
		assertEquals(XPFSettingValueTypeEnum.INTEGER, xpfSettingValue.getValueType());
	}

	@Test
	public void testConvertWithMissingRequiredValue() {
		SettingDefinition settingDefinition = new SettingDefinitionImpl();
		settingDefinition.setValueType("Integer");
		SettingValueImpl settingValue = new SettingValueImpl();
		settingValue.setSettingDefinition(settingDefinition);

		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field value of core entity SettingValue is missing.");

		settingValueConverter.convert(settingValue);
	}

	@Test
	public void testConvertWithMissingRequiredValueType() {
		SettingDefinition settingDefinition = new SettingDefinitionImpl();
		SettingValueImpl settingValue = new SettingValueImpl();
		settingValue.setSettingDefinition(settingDefinition);
		settingValue.setValue("test");

		exceptionThrown.expect(NullPointerException.class);
		exceptionThrown.expectMessage("Required field valueType of core entity SettingValue is missing.");

		settingValueConverter.convert(settingValue);
	}

	@Test
	public void testConvertWithIncorrectValueType() {
		String value = "1";
		String valueTypeKey = "foo";
		SettingDefinition settingDefinition = new SettingDefinitionImpl();
		settingDefinition.setValueType(valueTypeKey);
		SettingValueImpl settingValue = new SettingValueImpl();
		settingValue.setSettingDefinition(settingDefinition);
		settingValue.setValue(value);

		exceptionThrown.expect(XPFPluginRuntimeException.class);
		exceptionThrown.expectMessage("No valid SettingValueEnum found for valueType key " + valueTypeKey);

		settingValueConverter.convert(settingValue);
	}
}