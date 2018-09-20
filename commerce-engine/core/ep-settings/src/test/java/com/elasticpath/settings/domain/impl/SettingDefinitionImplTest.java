/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.settings.domain.impl;

import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.settings.domain.SettingDefinition;

/**
 * Test case for <code>SettingsDefinitionImpl</code>. 
 */
public class SettingDefinitionImplTest {
	
	private SettingDefinitionImpl settingDefinitionImpl;
	
	private static final String PATH = "COMMERCE/STORE/storeAdminEmailAddress/";
	
	private static final String DEFAULT_VALUE = "admin@demo.elasticpath.com";
	
	private static final String TYPE = "String";
	
	
	/**
	 * Setup the test invariants.
	 * @throws Exception in case of error
	 */
	@Before
	public void setUp() throws Exception {
		this.settingDefinitionImpl = new SettingDefinitionImpl();
	}
	
	private SettingDefinitionImpl createSettingDefinition(final String path, final String type, final String defaultValue) {
		SettingDefinitionImpl def = new SettingDefinitionImpl();
		def.setPath(path);
		def.setValueType(type);
		def.setDefaultValue(defaultValue);
		return def;
	}

	/**
	 * Test that you get the same path that you set.
	 */
	@Test
	public void testGetSetPath() {
		this.settingDefinitionImpl.setPath(PATH);
		assertThat(this.settingDefinitionImpl.getPath()).isEqualTo(PATH);
	}
		
	/**
	 * Test that you get the same default value that you set.
	 */
	@Test
	public void testGetDefaultValue() {
		this.settingDefinitionImpl.setDefaultValue(DEFAULT_VALUE);
		assertThat(this.settingDefinitionImpl.getDefaultValue()).isEqualTo(DEFAULT_VALUE);
	}
	
	/**
	 * Test that you get the same type that you set.
	 */
	@Test
	public void testGetSetType() {
		this.settingDefinitionImpl.setValueType(TYPE);
		assertThat(this.settingDefinitionImpl.getValueType()).isEqualTo(TYPE);
	}
	
	/**
	 * Test that you get the same description that you set.
	 */
	@Test
	public void testGetSetDescription() {
		final String desc = "MyDescription";
		this.settingDefinitionImpl.setDescription(desc);
		assertThat(this.settingDefinitionImpl.getDescription())
			.isEqualTo(desc)
			.as("The retrieved description should be the same as the one that was set.");
	}
	
	/**
	 * Test that you get the same max override value that you set.
	 */
	@Test
	public void testGetMaxOverrideValues() {
		final int overrideValue = -1;
		this.settingDefinitionImpl.setMaxOverrideValues(overrideValue);
		assertThat(this.settingDefinitionImpl.getMaxOverrideValues())
			.isEqualTo(overrideValue)
			.as("The retrieved maxOverrideValues should be the same as the one that was set.");
	}
	
	/**
	 * Test that you get null when the setting definition has not been persisted yet.
	 */
	@Test
	public void testGetLastModifiedDateNull() {
		assertThat(this.settingDefinitionImpl.getLastModifiedDate()).isNull();
	}
	
	/**
	 * Test that you get the same last modified date as the one that you set.
	 */
	@Test
	public void testSetLastModifiedDate() {
		Date lastModified = new Date(currentTimeMillis());
		this.settingDefinitionImpl.setLastModifiedDate(lastModified);
		assertThat(this.settingDefinitionImpl.getLastModifiedDate())
			.isEqualTo(lastModified)
			.as("The retrieved lastModifiedDate should be the same as the one that was set.");
	}
	
	/**
	 * Test that two SettingDefinition objects are equal if they have the same Path.
	 */
	@Test
	public void testEquals() {

		final String path = "COMMERCE/STORE/storeAdminEmailAddress/";
		final String newpath = "COMMERCE/STORE/theme/";
		final String type1 = "String";
		final String type2 = "XML";
		final String defaultValue1 = "admin@demo.elasticpath.com";
		final String defaultValue2 = " 	 ../assets";

		final SettingDefinition settingDefinitionOne = createSettingDefinition(path, type1, defaultValue1);
		final SettingDefinition settingDefinitionTwo = createSettingDefinition(path, type2, defaultValue2);

		assertThat(settingDefinitionTwo)
			.isEqualTo(settingDefinitionOne)
			.as("These two settingDefinitions should be equal since they both have the same path");

		final SettingDefinition settingsDefinitionThree = createSettingDefinition(path, type1, defaultValue1);
		final SettingDefinition settingsDefinitionFour = createSettingDefinition(newpath, type2, defaultValue2);

		assertThat(settingsDefinitionThree)
			.as("These two settingDefinitions should not be equal since they have different paths")
			.isNotEqualTo(settingsDefinitionFour);
	}
	
	/**
	 * Test that compareTo compares the SettingDefinition's paths.
	 */
	@Test
	public void testCompareTo() {
		SettingDefinitionImpl definition1 = new SettingDefinitionImpl();
		definition1.setPath("A");
		SettingDefinitionImpl definition2 = new SettingDefinitionImpl();
		definition2.setPath("B");

		assertThat(definition1).isLessThan(definition2);
		assertThat(definition2).isGreaterThan(definition1);
	}
}
