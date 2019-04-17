/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.settings.provider.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;
import com.elasticpath.settings.impl.OverridingSettingValueImpl;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.settings.provider.converter.SettingValueTypeConverter;

/**
 * Implementation of {@link SettingValueProvider} that delegates to a {@link SettingsReader}.
 *
 * @param <T> the type of value expected to be provided
 */
public class SettingValueProviderImpl<T> implements SettingValueProvider<T> {

	private static final Logger LOG = Logger.getLogger(SettingValueProviderImpl.class);

	private String path;

	private String context;

	private SettingsReader settingsReader;

	private SettingValueTypeConverter settingValueTypeConverter;

	private String systemPropertyOverrideKey;

	private String systemPropertyOverrideValue;

	@Override
	public T get() {
		return get(null);
	}

	@Override
	public T get(final String context) {
		verifyDependencies();

		SettingValue settingValue = getSettingValue(context);

		if (systemPropertyOverrideKey != null) {
			settingValue = applyPossibleSystemPropertyOverride(settingValue);
		}

		return getSettingValueTypeConverter().convert(settingValue);
	}

	@SuppressWarnings("PMD.ConfusingTernary")
	private SettingValue getSettingValue(final String context) {

		SettingValue settingValue;

		if (!StringUtils.isBlank(context)) {
			settingValue = getSettingsReader().getSettingValue(getPath(), context);
		} else if (!StringUtils.isBlank(getContext())) {
			settingValue = getSettingsReader().getSettingValue(getPath(), getContext());
		} else {
			settingValue = getSettingsReader().getSettingValue(getPath());
		}

		return settingValue;
	}

	private SettingValue applyPossibleSystemPropertyOverride(final SettingValue settingValue) {

		if (systemPropertyOverrideValue == null) {
			final String overrideValue = System.getProperty(systemPropertyOverrideKey);
			if (StringUtils.isNotEmpty(overrideValue)) {
				systemPropertyOverrideValue = overrideValue;
				LOG.info(
						String.format("Setting override applied for path: '%s', context: '%s' and systemPropertyOverrideKey: '%s'",
								path,
								context == null ? "" : context,
								systemPropertyOverrideKey));
			}
		}
		if (systemPropertyOverrideValue != null) {
			return new OverridingSettingValueImpl(settingValue, systemPropertyOverrideValue);
		}
		return settingValue;
	}

	private void verifyDependencies() {
		if (getSettingsReader() == null) {
			throw new IllegalStateException("settingsReader field must not be null");
		}

		if (getSettingValueTypeConverter() == null) {
			throw new IllegalStateException("settingsValueTypeConverter field must not be null");
		}

		if (getPath() == null) {
			throw new IllegalStateException("path field must not be null");
		}
	}

	public void setPath(final String path) {
		this.path = path;
	}

	protected String getPath() {
		return path;
	}

	public void setContext(final String context) {
		this.context = context;
	}

	protected String getContext() {
		return context;
	}

	public void setSettingsReader(final SettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}

	protected SettingsReader getSettingsReader() {
		return settingsReader;
	}

	public void setSettingValueTypeConverter(final SettingValueTypeConverter settingValueTypeConverter) {
		this.settingValueTypeConverter = settingValueTypeConverter;
	}

	public SettingValueTypeConverter getSettingValueTypeConverter() {
		return settingValueTypeConverter;
	}

	public void setSystemPropertyOverrideKey(final String systemPropertyOverrideKey) {
		this.systemPropertyOverrideKey = systemPropertyOverrideKey;
	}

}
