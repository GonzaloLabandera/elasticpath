/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider;

/**
 * Plugin configuration key.
 */
public class PluginConfigurationKey {
	private String key;
	private String description;

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the value.
	 *
	 * @param description the value.
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key the key.
	 */
	public void setKey(final String key) {
		this.key = key;
	}
}
