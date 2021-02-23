/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.epcoretool.logic;

/**
 * The Class AbstractSetSettingMetadata.
 */
public abstract class AbstractSetSettingMetadata extends AbstractBaseSettings {

	/**
	 * Instantiates a new abstract set setting metadata.
	 *
	 * @param jdbcUrl the jdbc url
	 * @param jdbcUsername the jdbc username
	 * @param jdbcPassword the jdbc password
	 * @param jdbcDriverClass the jdbc driver class
	 * @param jdbcConnectionPoolMinIdle the jdbc connection pool min idle
	 * @param jdbcConnectionPoolMaxIdle the jdbc connection pool max idle
	 */
	public AbstractSetSettingMetadata(final String jdbcUrl, final String jdbcUsername, final String jdbcPassword, final String jdbcDriverClass,
							  final Integer jdbcConnectionPoolMinIdle, final Integer jdbcConnectionPoolMaxIdle) {
		super(jdbcUrl, jdbcUsername, jdbcPassword, jdbcDriverClass, jdbcConnectionPoolMinIdle, jdbcConnectionPoolMaxIdle);
	}

	/**
	 * Execute.
	 *
	 * @param settingName the setting name
	 * @param metadataName the metadata name
	 * @param metadataValue the metadata value
	 */
	public void execute(final String settingName, final String metadataName, final String metadataValue) {
		setSettingMetadata(settingName, metadataName, metadataValue);
	}
}
