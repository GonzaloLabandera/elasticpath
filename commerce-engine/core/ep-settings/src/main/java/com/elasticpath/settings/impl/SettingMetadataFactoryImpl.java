/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.settings.impl;

import com.elasticpath.settings.SettingMetadataFactory;
import com.elasticpath.settings.domain.SettingMetadata;
import com.elasticpath.settings.domain.impl.SettingMetadataImpl;

/**
 * Creates new SettingMetadata objects with a key and a value.
 */
public class SettingMetadataFactoryImpl implements SettingMetadataFactory {

	/**
	 * Create a new {@link SettingMetadataImpl} implementation of the SettingMetadata interface.
	 * @param key the metadata key
	 * @param value the metadata value
	 * @return a new SettingMetadata object
	 */
	@Override
	public SettingMetadata createSettingMetadata(final String key, final String value) {
		SettingMetadataImpl settingMetadataImpl = new SettingMetadataImpl();
		settingMetadataImpl.setKey(key);
		settingMetadataImpl.setValue(value);
		return settingMetadataImpl;
	}
}
