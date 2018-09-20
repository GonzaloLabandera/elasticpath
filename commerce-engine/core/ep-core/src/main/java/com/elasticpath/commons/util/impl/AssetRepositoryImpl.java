/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
/**
 * 
 */
package com.elasticpath.commons.util.impl;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.elasticpath.commons.util.AssetRepository;
import com.elasticpath.service.environment.EnvironmentInfoService;
import com.elasticpath.settings.SettingsReader;

/**
 * Provides paths to various Asset storage locations as well the url path for retrieving a resource.
 * This implementation uses the SettingsService to retrieve the various
 * paths, and ensure that the paths are valid for the platform
 * upon which the JVM is running.
 */
public class AssetRepositoryImpl implements AssetRepository {
	private SettingsReader settingsReader;
	
	private EnvironmentInfoService environmentInfoService;
	
	/**
	 * Gets the absolute path to the catalog assets folder on the file system.
	 * This implementation retrieves the path from the settings service.
	 * Calls {@link #isAbsolute(String)} to determine whether the configured path is relative, and if so, calls
	 * {@link #getAbsoluteCatalogAssetPathFromRelativePath(String)} to determine the absolute path.
	 * @return the absolute file system path to catalog assets.
	 * @throws com.elasticpath.base.exception.EpServiceException if for some reason the catalog asset path setting does not exist
	 */
	@Override
	public String getCatalogAssetPath() {
		String definedPath = getCatalogAssetPathFromSettingsService();
		
		if (!isAbsolute(definedPath)) {
			return getAbsoluteCatalogAssetPathFromRelativePath(definedPath);
		}
		return definedPath;
	}
	
	/**
	 * @return the catalog asset path as configured in the settings service
	 */
	String getCatalogAssetPathFromSettingsService() {
		return getSettingsReader().getSettingValue("COMMERCE/SYSTEM/ASSETS/assetLocation").getValue();
	}
	
	/**
	 * Takes in a relative path to the catalog assets directory and converts it
	 * to an absolute path, assuming that the given path is relative to the
	 * web application's root. Calls {{@link #getApplicationRootPath()}.
	 * 
	 * @param relativePath the relative path to the catalog assets directory
	 * @return the absolute path to the catalog assets directory
	 */
	String getAbsoluteCatalogAssetPathFromRelativePath(final String relativePath) {
		String absolutePath = FilenameUtils.concat(getApplicationRootPath(), relativePath);
		if (absolutePath == null) {
			absolutePath = relativePath;
		}
		return absolutePath;
	}
	
	/**
	 * Get the application's absolute root path.
	 * This implementation gets the path to the WEB-INF directory from the ElasticPath
	 * singleton, and strips off the trailing 'WEB-INF' portion to retrieve the root
	 * path to the web application.
	 * @return the root path to the application.
	 */
	String getApplicationRootPath() {
		return getEnvironmentInfoService().getApplicationRootPath();
	}


	/**
	 * @return the file system path to import files.
	 */
	@Override
	public String getImportAssetPath() {
		return FilenameUtils.concat(getCatalogAssetPath(), getImportAssetSubfolder());
	}
	
	/**
	 * @return the name of the directory within which import csv files are stored.
	 */
	String getImportAssetSubfolder() {
		return getSettingsReader().getSettingValue("COMMERCE/SYSTEM/ASSETS/importAssetsSubfolder").getValue();
	}
	
	/**
	 * @return the file system path to store assets.
	 */
	@Override
	public String getThemeAssetsPath() {
		return FilenameUtils.concat(getCatalogAssetPath(), getThemesSubfolder());
	}
	
	/**
	 * @return the name of the top-level directory within which themed resources are stored
	 */
	@Override
	public String getThemesSubfolder() {
		return getSettingsReader().getSettingValue("COMMERCE/SYSTEM/ASSETS/themesSubfolder").getValue();
	}
	
	/**
	 * @return the name of the top-level directory within which commerce manager assets are stored
	 */
	@Override
	public String getCmAssetsSubfolder() {
		return getSettingsReader().getSettingValue("COMMERCE/SYSTEM/ASSETS/cmAssetsSubfolder").getValue();
	}

	/**
	 * @return the system path to content wrappers repository.
	 */
	@Override
	public String getContentWrappersPath() {
		final String contentWrapperSubDirectory = getSettingsReader().getSettingValue("COMMERCE/SYSTEM/ASSETS/contentWrappersLocation").getValue();
		
		return FilenameUtils.concat(getCatalogAssetPath(), contentWrapperSubDirectory);
	}
	
	/**
	 * Test hook to allow testing of getCatalogAssetPath().
	 * http://commons.apache.org/io/api-release/org/apache/commons/io/FilenameUtils.html
	 * @param path the path to check.
	 * @return true if the path represents an absolute path location, false otherwise.
	 */
	boolean isAbsolute(final String path) {
		return new File(path).isAbsolute();
	}

	/**
	 * @return the settingsReader
	 */
	public SettingsReader getSettingsReader() {
		return settingsReader;
	}

	/**
	 * @param settingsReader the settingsReader to set
	 */
	public void setSettingsReader(final SettingsReader settingsReader) {
		this.settingsReader = settingsReader;
	}

	protected EnvironmentInfoService getEnvironmentInfoService() {
		return environmentInfoService;
	}

	public void setEnvironmentInfoService(final EnvironmentInfoService environmentInfoService) {
		this.environmentInfoService = environmentInfoService;
	}
}
