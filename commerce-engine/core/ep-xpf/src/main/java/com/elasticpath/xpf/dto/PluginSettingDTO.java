/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import lombok.Data;

import com.elasticpath.xpf.exception.InvalidConfigurationException;

/**
 * Represents a XPF plugin/extension setting.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class PluginSettingDTO {

	private final String settingKey;
	private final SettingDataTypeDTO dataType;
	private final SettingCollectionTypeDTO collectionType;
	private final List<PluginSettingValueDTO> settingValues;

	/**
	 * Constructor.
	 *
	 * @param settingKey     the setting key
	 * @param dataType       the data type
	 * @param collectionType the collection type
	 * @param settingValues  the settings values
	 */
	public PluginSettingDTO(final String settingKey, final SettingDataTypeDTO dataType, final SettingCollectionTypeDTO collectionType,
							final List<PluginSettingValueDTO> settingValues) {
		this.settingKey = settingKey;
		this.dataType = dataType;
		this.collectionType = collectionType;
		this.settingValues = settingValues;
	}

	/**
	 * Get Values.
	 *
	 * @return the values
	 */
	public Object getValues() {

		switch (getCollectionType()) {
			case SINGLE:
				return getSettingValues().get(0);
			case LIST:
				return getSettingValues();
			case SET:
				return new HashSet<>(getSettingValues());
			case MAP:
				return getSettingValues().stream().collect(HashMap::new,
						(map, value) -> map.put(value.getMapKey(), value.getValue(getDataType())),
						HashMap::putAll);
			default:
				throw new InvalidConfigurationException("Unrecognized setting collection type: " + getCollectionType());
		}
	}
}
