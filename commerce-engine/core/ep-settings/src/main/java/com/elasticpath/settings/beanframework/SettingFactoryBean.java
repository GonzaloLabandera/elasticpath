/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.settings.beanframework;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;
import com.elasticpath.settings.impl.OverridingSettingValueImpl;

/**
 * Reads EP setting values as a Spring bean.
 */
public class SettingFactoryBean extends AbstractFactoryBean<String> {

	private static final Logger LOG = Logger.getLogger(SettingFactoryBean.class);

	/**
	 * The @Autowired is necessary as otherwise we do not get the settings reader injected in time for createInstance().
	 */
	@Autowired
	@Qualifier("settingsReader")
	private SettingsReader settingsReader;

	private String path;

	private String context;

	private String systemPropertyOverrideKey;

	private String systemPropertyOverrideValue;

	@Override
	public Class<?> getObjectType() {
		return String.class;
	}

	@Override
	protected String createInstance() throws Exception {
		if (path == null) {
			throw new IllegalStateException("Path property must be set prior to invoking this method");
		}

		SettingValue settingValue = getSettingValue();

		if (settingValue == null) {
			throw new IllegalArgumentException("Failed to load setting " + path + " in context " + context);
		}

		if (systemPropertyOverrideKey != null) {
			settingValue = applyPossibleSystemPropertyOverride(settingValue);
		}

		return settingValue.getValue();
	}

	private SettingValue getSettingValue() {
		if (!StringUtils.isBlank(context)) {
			return settingsReader.getSettingValue(path, context);
		}

		return settingsReader.getSettingValue(path);
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

	/**
	 * Sets the path corresponding to the setting value to retrieve. Must be non-{@code null} and non-empty.
	 * 
	 * @param path the settings path (eg. COMMERCE/FOO/bar)
	 * @throws IllegalArgumentException if path is {@code null} or empty
	 */
	public void setPath(final String path) {
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException("Parameter path must be non-null and non-empty");
		}

		this.path = path;
	}

	/**
	 * Sets the context for which to load the setting value. May be null.
	 * 
	 * @param context the context
	 */
	public void setContext(final String context) {
		this.context = context;
	}

	protected void setSettingsReader(final SettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}

	public void setSystemPropertyOverrideKey(final String systemPropertyOverrideKey) {
		this.systemPropertyOverrideKey = systemPropertyOverrideKey;
	}

}