/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.json;

import java.beans.ConstructorProperties;
import java.util.List;

import lombok.Data;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class Setting {
	private final String settingKey;
	private final String dataType;
	private final String collectionType;
	private final List<SettingValue> settingValues;

	/**
	 * Constructor.
	 *
	 * @param key            the setting key
	 * @param dataType       the data type
	 * @param collectionType the collection type
	 * @param settingValues  the settings values
	 */
	@ConstructorProperties({"key", "dataType", "collectionType", "values"})
	public Setting(final String key, final String dataType,
				   final String collectionType,
				   final List<SettingValue> settingValues) {
		this.settingKey = key;
		this.dataType = dataType;
		this.collectionType = collectionType;
		this.settingValues = settingValues;
	}
}