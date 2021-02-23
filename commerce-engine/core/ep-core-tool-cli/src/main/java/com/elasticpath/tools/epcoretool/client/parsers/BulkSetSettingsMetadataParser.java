/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.tools.epcoretool.client.parsers;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.elasticpath.epcoretool.LoggerFacade;
import com.elasticpath.epcoretool.logic.AbstractBulkSetSettingMetadata;
import com.elasticpath.epcoretool.logic.dto.EpSetting;
import com.elasticpath.tools.epcoretool.client.CliDB;
import com.elasticpath.tools.epcoretool.client.CliParser;
import com.elasticpath.tools.epcoretool.client.LoadPropertiesHelper;

/**
 * BulkSetSettingsMetadataParser wapper around the logic classes.
 */
public class BulkSetSettingsMetadataParser extends CliDB implements CliParser {

	@Override
	public void execute(final String param) {
		final AbstractBulkSetSettingMetadata logic = new AbstractBulkSetSettingMetadata(getUrl(), getUsername(), getPassword(), getDriverClass(),
				getMinIdle(), getMaxIdle()) {
			@Override
			protected LoggerFacade getLogger() {
				return getLoggerFacade();
			}
		};

		// Use the properties config file parser used by the CliDB
		final Properties configProperties = LoadPropertiesHelper.loadProperties(param);

		final Set<EpSetting> settings = new HashSet<>(configProperties.size());
		for (Map.Entry<Object, Object> setting : configProperties.entrySet()) {
			settings.add(logic.parseSettingString((String) setting.getKey(), (String) setting.getValue()));
		}
		logic.execute(settings);
	}

	@Override
	public String help() {
		return "\nbulk-set-settings-metadata <bulk-settings.properties>\n" + "\tUpdates the setting metadata value in the Elastic Path database. \t"
				+ "\nIf a value already exists, it will be replaced.\n";
	}
}
