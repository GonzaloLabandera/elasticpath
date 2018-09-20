/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.settings.domain;

/**
 *  Simple key value pairs to add metadata information to Settings Definitions.
 *
 */
public interface SettingMetadata {
	/**
	 * Get the metadata value.
	 * @return value the string value
	 */
	String getValue();

	/**
	 * Get the metadata value.
	 * @param value the string value
	 */
	void setValue(String value);

	/**
	 * Get the metadata key.
	 * @return key the string key
	 */
	String getKey();

	/**
	 * Set the metadata key.
	 * @param key the key
	 */
	void setKey(String key);
}
