/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.settings;

import com.elasticpath.settings.domain.SettingMetadata;

/**
 * A SettingMetadataFactory is responsible for creating new SettingMetadata objects.
 */
public interface SettingMetadataFactory {

	/**
	 * Create a new {@link SettingMetadataImpl} implementation of the SettingMetadata interface.
	 * @param key the metadata key
	 * @param value the metadata value
	 * @return a new SettingMetadata object
	 */
	SettingMetadata createSettingMetadata(String key, String value);
}
