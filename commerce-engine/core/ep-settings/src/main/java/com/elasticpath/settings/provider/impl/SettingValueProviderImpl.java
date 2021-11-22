/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.settings.provider.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.settings.provider.SettingValueProvider;
import com.elasticpath.settings.provider.converter.SettingValueTypeConverter;
import com.elasticpath.xpf.XPFExtensionLookup;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.context.XPFSettingValueRetrievalContext;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValue;
import com.elasticpath.xpf.connectivity.extensionpoint.SettingValueRetrievalStrategy;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;

/**
 * Implementation of {@link SettingValueProvider} that delegates to a {@link com.elasticpath.settings.SettingsReader}.
 *
 * @param <T> the type of value expected to be provided
 */
public class SettingValueProviderImpl<T> implements SettingValueProvider<T> {

	private static final Logger LOG = LogManager.getLogger(SettingValueProviderImpl.class);

	private String path;

	private String context;

	private XPFExtensionLookup xpfExtensionLookup;

	private SettingValueTypeConverter settingValueTypeConverter;

	private String systemPropertyOverrideKey;

	private String systemPropertyOverrideValue;

	private String deprecatedSystemPropertyOverrideKey;

	@Override
	public T get() {
		return get(null);
	}

	@Override
	public T get(final String context) {
		verifyDependencies();

		XPFSettingValue settingValue = getSettingValue(context);

		if (systemPropertyOverrideKey != null || deprecatedSystemPropertyOverrideKey != null) {
			settingValue = applyPossibleSystemPropertyOverride(settingValue);
		}

		return getSettingValueTypeConverter().convert(settingValue);
	}

	private XPFSettingValue getSettingValue(final String context) {
		String settingValueContext = deriveSettingValueContext(context);

		XPFSettingValueRetrievalContext settingValueRetrievalContext = new XPFSettingValueRetrievalContext(getPath(), settingValueContext);

		return getSettingValueRetrievalStrategyList().stream()
				.map(strategy -> strategy.getSettingValue(settingValueRetrievalContext))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseThrow(() -> new EpServiceException("No setting value retrieval strategies were unable to find a value for path and context: '"
						+ path + "', '" + context + "'"));

	}

	@SuppressWarnings("PMD.ConfusingTernary")
	private String deriveSettingValueContext(final String context) {
		String settingValueContext = null;
		if (!StringUtils.isBlank(context)) {
			settingValueContext = context;
		} else if (!StringUtils.isBlank(getContext())) {
			settingValueContext = getContext();
		}
		return settingValueContext;
	}

	private XPFSettingValue applyPossibleSystemPropertyOverride(final XPFSettingValue settingValue) {
		if (StringUtils.isNotEmpty(deprecatedSystemPropertyOverrideKey)) {
			String overrideValue = System.getProperty(deprecatedSystemPropertyOverrideKey);

			if (StringUtils.isNotEmpty(overrideValue)) {
				LOG.warn("Deprecated ‘" + deprecatedSystemPropertyOverrideKey + "’ JVM system property should be replaced with ‘"
						+ systemPropertyOverrideKey + "’.");
				systemPropertyOverrideValue = overrideValue;
				LOG.info(
						String.format("Setting override applied for path: '%s', context: '%s' and deprecatedSystemPropertyOverrideKey: '%s'",
								path,
								StringUtils.defaultString(context),
								deprecatedSystemPropertyOverrideKey));
			}
		}

		if (StringUtils.isNotEmpty(systemPropertyOverrideKey)) {
			String overrideValue = System.getProperty(systemPropertyOverrideKey);
			if (StringUtils.isNotEmpty(overrideValue)) {
				systemPropertyOverrideValue = overrideValue;
				LOG.info(
						String.format("Setting override applied for path: '%s', context: '%s' and systemPropertyOverrideKey: '%s'",
								path,
								StringUtils.defaultString(context),
								systemPropertyOverrideKey));
			}
		}

		if (systemPropertyOverrideValue != null) {
			return new XPFSettingValue(systemPropertyOverrideValue, settingValue.getValueType());
		}
		return settingValue;
	}

	private void verifyDependencies() {
		if (getXpfExtensionLookup() == null) {
			throw new IllegalStateException("ExtensionLookup field must not be null");
		}

		if (getSettingValueTypeConverter() == null) {
			throw new IllegalStateException("settingsValueTypeConverter field must not be null");
		}

		if (getPath() == null) {
			throw new IllegalStateException("path field must not be null");
		}
	}

	private List<SettingValueRetrievalStrategy> getSettingValueRetrievalStrategyList() {
		return xpfExtensionLookup.getMultipleExtensions(SettingValueRetrievalStrategy.class,
				XPFExtensionPointEnum.SETTING_VALUE_RETRIEVAL, new XPFExtensionSelectorAny());
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

	public void setSettingValueTypeConverter(final SettingValueTypeConverter settingValueTypeConverter) {
		this.settingValueTypeConverter = settingValueTypeConverter;
	}

	public SettingValueTypeConverter getSettingValueTypeConverter() {
		return settingValueTypeConverter;
	}

	public void setSystemPropertyOverrideKey(final String systemPropertyOverrideKey) {
		this.systemPropertyOverrideKey = systemPropertyOverrideKey;
	}

	public void setDeprecatedSystemPropertyOverrideKey(final String deprecatedSystemPropertyOverrideKey) {
		this.deprecatedSystemPropertyOverrideKey = deprecatedSystemPropertyOverrideKey;
	}

	public XPFExtensionLookup getXpfExtensionLookup() {
		return xpfExtensionLookup;
	}

	public void setXpfExtensionLookup(final XPFExtensionLookup xpfExtensionLookup) {
		this.xpfExtensionLookup = xpfExtensionLookup;
	}
}
