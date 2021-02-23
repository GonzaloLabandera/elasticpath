/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.epcoretool.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.elasticpath.epcoretool.LoggerFacade;
import com.elasticpath.epcoretool.logic.AbstractSetSettingMetadata;

/**
 * Updates the setting metadata value in the Elastic Path database. If a value already exists, it will be replaced.
 */
@Mojo(name = "set-setting-metadata", threadSafe = true)
public class SetSettingMetadataMojo extends AbstractEpCoreMojo {

	/**
	 * Name of setting.
	 */
	@Parameter(property = "settingName", required = true)
	private String settingName;

	/**
	 * Name of metadata.
	 */
	@Parameter(property = "metadataName", required = true)
	private String metadataName;

	/**
	 * Value of metadata.
	 */
	@Parameter(property = "metadataValue", required = true)
	private String metadataValue;

	/**
	 * Look up a {@code SettingsService}, then fetch the specified setting+metadata.
	 *
	 * @throws MojoExecutionException the mojo execution exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		final AbstractSetSettingMetadata setSettingMetadata = new AbstractSetSettingMetadata(getJdbcUrl(), getJdbcUsername(), getJdbcPassword(),
				getJdbcDriverClass(), getJdbcConnectionPoolMinIdle(), getJdbcConnectionPoolMaxIdle()) {
			@Override
			protected LoggerFacade getLogger() {
				return getLoggerFacade();
			}
		};
		try {
			setSettingMetadata.execute(settingName, metadataName, metadataValue);
		} catch (RuntimeException ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		} finally {
			setSettingMetadata.close();
		}
	}
}
