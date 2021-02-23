/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.tools.epcoretool.client.parsers;

import com.elasticpath.epcoretool.LoggerFacade;
import com.elasticpath.epcoretool.logic.AbstractSetSettingMetadata;
import com.elasticpath.epcoretool.logic.dto.EpSetting;
import com.elasticpath.tools.epcoretool.client.CliDB;
import com.elasticpath.tools.epcoretool.client.CliParser;

/**
 * SetSettingMetadataParser wapper around the logic classes.
 */
public class SetSettingMetadataParser extends CliDB implements CliParser {

	@Override
	public void execute(final String param) {
		final AbstractSetSettingMetadata logic = new AbstractSetSettingMetadata(getUrl(), getUsername(), getPassword(), getDriverClass(),
				getMinIdle(), getMaxIdle()) {

			@Override
			protected LoggerFacade getLogger() {
				return getLoggerFacade();
			}
		};

		final EpSetting setting = logic.parseSettingString(param);
		logic.execute(setting.getName(), setting.getContext(), setting.getValue());
	}

	@Override
	public String help() {
		return "\nset-setting-metadata <setting>@<name>=<value>\n"
				+ "\tUpdates the setting metadata value in the Elastic Path database. If a\n" + "\tvalue already exists, it will be replaced.\n";
	}
}
