/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.persistence.openjpa.impl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * A persistence unit post-processor which overrides persistence unit values for persistence units
 * with a matching name within the same classloader.
 */
public class OverridingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

	private String persistenceUnitName;
	private List<String> excludedMappingFiles;
	private List<String> excludedManagedClassNames;
	private List<URL> excludedJarFileUrls;
	private Map<String, String> propertyOverrides;
	private Boolean excludeUnlistedClasses;
	private DefaultPersistenceUnitManager persistenceUnitManager;
	private String jtaDataSource;
	private String nonJtaDataSource;

	@Override
	public void postProcessPersistenceUnitInfo(final MutablePersistenceUnitInfo persistenceUnitInfo) {
		if (persistenceUnitInfo.getPersistenceUnitName().equals(persistenceUnitName)) {
			overridePersistenceUnitInfo(persistenceUnitInfo);
		}
	}

	private void overridePersistenceUnitInfo(final MutablePersistenceUnitInfo persistenceUnitInfo) {
		if (excludedMappingFiles != null) {
			persistenceUnitInfo.getMappingFileNames().removeAll(excludedMappingFiles);
		}
		if (excludedManagedClassNames != null) {
			persistenceUnitInfo.getManagedClassNames().removeAll(excludedManagedClassNames);
		}
		if (excludedJarFileUrls != null) {
			persistenceUnitInfo.getJarFileUrls().removeAll(excludedJarFileUrls);
		}
		if (propertyOverrides != null) {
			persistenceUnitInfo.getProperties().putAll(propertyOverrides);
		}
		if (excludeUnlistedClasses != null) {
			persistenceUnitInfo.setExcludeUnlistedClasses(excludeUnlistedClasses);
		}
		if (jtaDataSource != null) {
			DataSource dataSource = persistenceUnitManager.getDataSourceLookup().getDataSource(jtaDataSource.trim());
			persistenceUnitInfo.setJtaDataSource(dataSource);
		}
		if (nonJtaDataSource != null) {
			DataSource dataSource = persistenceUnitManager.getDataSourceLookup().getDataSource(nonJtaDataSource.trim());
			persistenceUnitInfo.setNonJtaDataSource(dataSource);
		}
	}

	public void setPersistenceUnitName(final String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public void setExcludedMappingFiles(final List<String> excludedMappingFiles) {
		this.excludedMappingFiles = excludedMappingFiles;
	}

	public void setExcludedManagedClassNames(final List<String> excludedManagedClassNames) {
		this.excludedManagedClassNames = excludedManagedClassNames;
	}

	public void setExcludedJarFileUrls(final List<URL> excludedJarFileUrls) {
		this.excludedJarFileUrls = excludedJarFileUrls;
	}

	public void setPropertyOverrides(final Map<String, String> propertyOverrides) {
		this.propertyOverrides = propertyOverrides;
	}

	public void setExcludeUnlistedClasses(final Boolean excludeUnlistedClasses) {
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}
	
	public void setPersistenceUnitManager(final DefaultPersistenceUnitManager persistenceUnitManager) {
		this.persistenceUnitManager = persistenceUnitManager;
	}

	public void setJtaDataSource(final String jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public void setNonJtaDataSource(final String nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}
}
