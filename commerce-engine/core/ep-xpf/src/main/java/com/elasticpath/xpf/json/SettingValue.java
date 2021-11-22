/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.json;

import java.beans.ConstructorProperties;

import lombok.Data;

/**
 * A class to map configurations from json.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class SettingValue {
	private final String key;
	private Object value;

	/**
	 * Constructor.
	 *
	 * @param key   the setting key
	 * @param value the setting value
	 */
	@ConstructorProperties({"key", "value"})
	public SettingValue(final String key, final Object value) {
		this.key = key;
		this.value = value;
	}

	public void setValue(final Object value) {
		this.value = value;
	}
}
