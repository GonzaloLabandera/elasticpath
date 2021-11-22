/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.xpf.json.SettingValue;

/**
 * Setting value parser from plugins and extensions.
 */
public class XPFSettingValueParser implements EnvironmentAware {

	private Environment environment;
	private List<String> propertySources;

	/**
	 * Resolve placeholders.
	 *
	 * @param settingValue setting to resolve placeholders
	 */
	public void resolvePlaceholder(final SettingValue settingValue) {
		Matcher patternMatch = Pattern.compile("\\$\\{(.*)}").matcher(settingValue.getValue().toString());
		if (patternMatch.matches()) {
			String placeholder = patternMatch.group(1);

			String placeholderValue = environment.getProperty(placeholder);

			if (placeholderValue == null) {
				placeholderValue = readPlaceholderValueFromPropertyFiles(placeholder);
			}

			if (placeholderValue == null) {
				throw new EpSystemException("Value for the " + settingValue.getValue() + " placeholder not found.");
			}

            settingValue.setValue(placeholderValue);
		}
	}

	private String readPlaceholderValueFromPropertyFiles(final String placeholder) {
		Properties prop = new Properties();
		for (String propertySource : propertySources) {
			try (InputStream input = new FileInputStream(propertySource)) {
				prop.load(input);

				Object placeholderValue = prop.get(placeholder);
				if (placeholderValue != null) {
					return placeholderValue.toString();
				}
			} catch (IOException e) {
				// no need an action
			}
		}

		return null;
	}

	@Override
	public void setEnvironment(final Environment environment) {
		this.environment = environment;
	}

	public void setPropertySources(final List<String> propertySources) {
		this.propertySources = propertySources;
	}
}