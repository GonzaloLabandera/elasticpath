/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.epcoretool.mojo;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.elasticpath.epcoretool.LoggerFacade;
import com.elasticpath.epcoretool.logic.AbstractBulkSetSettingMetadata;
import com.elasticpath.epcoretool.logic.dto.EpSetting;

/**
 * Updates the setting metadata value in the Elastic Path database. If a value already exists, it will be replaced.
 */
@Mojo(name = "bulk-set-setting-metadata", threadSafe = true)
public class BulkSetSettingMetadataMojo extends AbstractEpCoreMojo {

	@Parameter(alias = "settings", required = true)
	private String[] settings;

	@SuppressWarnings("PMD.ArrayIsStoredDirectly")
	public void setSettings(final String[] settings) {
		this.settings = settings;
	}

	/**
	 * Look up a {@code SettingsService}, then fetch the specified settings metadata.
	 *
	 * @throws MojoExecutionException the mojo execution exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		final AbstractBulkSetSettingMetadata bulkSetSettingMetadata = new AbstractBulkSetSettingMetadata(getJdbcUrl(), getJdbcUsername(),
				getJdbcPassword(), getJdbcDriverClass(), getJdbcConnectionPoolMinIdle(), getJdbcConnectionPoolMaxIdle()) {
			@Override
			protected LoggerFacade getLogger() {
				return getLoggerFacade();
			}
		};
		try {
			final Set<EpSetting> bulkSettingsMetadata = new HashSet<>(settings.length);
			for (String setting : settings) {
				bulkSettingsMetadata.add(bulkSetSettingMetadata.parseSettingString(setting));
			}
			bulkSetSettingMetadata.execute(bulkSettingsMetadata);

		} catch (RuntimeException ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		} finally {
			bulkSetSettingMetadata.close();
		}
	}
}
