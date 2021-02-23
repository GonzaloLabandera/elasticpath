/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.epcoretool.logic;

import java.util.Set;

import com.elasticpath.epcoretool.logic.dto.EpSetting;

/**
 * The Class AbstractBulkSetSettingMetadata.
 */
public abstract class AbstractBulkSetSettingMetadata extends AbstractBaseSettings {

	/**
	 * Instantiates a new abstract bulk set setting metadata.
	 *
	 * @param jdbcUrl the jdbc url
	 * @param jdbcUsername the jdbc username
	 * @param jdbcPassword the jdbc password
	 * @param jdbcDriverClass the jdbc driver class
	 * @param jdbcConnectionPoolMinIdle the jdbc connection pool min idle
	 * @param jdbcConnectionPoolMaxIdle the jdbc connection pool max idle
	 */
	public AbstractBulkSetSettingMetadata(final String jdbcUrl, final String jdbcUsername, final String jdbcPassword, final String jdbcDriverClass,
								  final Integer jdbcConnectionPoolMinIdle, final Integer jdbcConnectionPoolMaxIdle) {
		super(jdbcUrl, jdbcUsername, jdbcPassword, jdbcDriverClass, jdbcConnectionPoolMinIdle, jdbcConnectionPoolMaxIdle);
	}

	/**
	 * Execute.
	 *
	 * @param settings the settings
	 */
	public void execute(final Set<EpSetting> settings) {
		for (EpSetting setting : settings) {
			setSettingMetadata(setting.getName(), setting.getContext(), setting.getValue());
		}
	}
}
