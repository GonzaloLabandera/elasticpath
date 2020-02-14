/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider;

/**
 * An plugin configuration key builder.
 */
public final class PluginConfigurationKeyBuilder {
	private String key;
	private String description;

	private PluginConfigurationKeyBuilder() {
	}

	/**
	 * An plugin configuration key builder.
	 *
	 * @return the builder
	 */
	public static PluginConfigurationKeyBuilder builder() {
		return new PluginConfigurationKeyBuilder();
	}

	/**
	 * With key builder.
	 *
	 * @param key the key.
	 * @return the builder
	 */
	public PluginConfigurationKeyBuilder withKey(final String key) {
		this.key = key;
		return this;
	}

	/**
	 * With description builder.
	 *
	 * @param description the description.
	 * @return the builder
	 */
	public PluginConfigurationKeyBuilder withDescription(final String description) {
		this.description = description;
		return this;
	}

	/**
	 * Build plugin configuration key.
	 *
	 * @return plugin configuration key.
	 */
	public PluginConfigurationKey build() {
		if (key == null) {
			throw new IllegalStateException("Builder is not fully initialized, field key is missing");
		}

		final PluginConfigurationKey pluginConfigurationKey = new PluginConfigurationKey();
		pluginConfigurationKey.setKey(key);
		pluginConfigurationKey.setDescription(description);

		return pluginConfigurationKey;
	}
}
