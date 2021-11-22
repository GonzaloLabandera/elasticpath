/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.setting;

import java.util.Optional;

import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFSettingValueRetrievalContext;
import com.elasticpath.xpf.connectivity.entity.XPFSettingValue;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SettingValueRetrievalStrategy;
import com.elasticpath.xpf.converters.SettingValueConverter;

/**
 * Implementation of the {@link SettingValueRetrievalStrategy} interface that uses {@link com.elasticpath.settings.impl.CachedSettingsReaderImpl}.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SETTING_VALUE_RETRIEVAL, priority = 1050)
public class DBSettingValueRetrievalStrategy extends XPFExtensionPointImpl implements SettingValueRetrievalStrategy {
	private static final Logger LOG = LoggerFactory.getLogger(DBSettingValueRetrievalStrategy.class);

	@Autowired
	private SettingsReader settingsReader;

	@Autowired
	private SettingValueConverter xpfSettingValueConverter;

	@Override
	public Optional<XPFSettingValue> getSettingValue(final XPFSettingValueRetrievalContext context) {
		try {
			return Optional.ofNullable(settingsReader.getSettingValue(context.getSettingPath(), context.getSettingContext()))
					.map(xpfSettingValueConverter::convert);
		} catch (EpServiceException e) {
			LOG.debug("Unable to find a setting value in the database for path and context: '{}', '{}'", context.getSettingPath(),
					context.getSettingContext(), e);

			return Optional.empty();
		}
	}
}
