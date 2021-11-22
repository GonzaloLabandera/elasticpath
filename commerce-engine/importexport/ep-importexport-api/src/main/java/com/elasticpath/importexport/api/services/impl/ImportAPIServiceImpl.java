/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.services.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.commons.ThreadLocalMap;
import com.elasticpath.importexport.api.services.ImportAPIService;
import com.elasticpath.importexport.common.configuration.ConfigurationLoader;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.summary.impl.SummaryImpl;
import com.elasticpath.importexport.common.util.LogAppenderUtil;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.common.util.MetaDataMapPopulator;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.context.ImportContext;
import com.elasticpath.importexport.importer.controller.ImportStage;

/**
 * Service for consuming Import/Export imports as an InputStream.
 */
public class ImportAPIServiceImpl implements ImportAPIService {
	private static final Logger LOG = LogManager.getLogger(ImportAPIServiceImpl.class);
	private static final String IMPORT_CONFIGURATION_FILE = "importconfiguration.xml";

	private ConfigurationLoader configurationLoader;
	private ImportConfiguration importConfiguration;
	private ImportStage importStage;
	private ThreadLocalMap<String, Object> metadataMap;
	private MetaDataMapPopulator metaDataMapPopulator;

	/**
	 * Initialize the service during Spring startup.
	 *
	 * @throws IOException if there is an issue reading the import configuration
	 * @throws ConfigurationException if there is an issue with the import configuration file
	 */
	public void initialize() throws IOException, ConfigurationException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream configStream = classLoader.getResourceAsStream(IMPORT_CONFIGURATION_FILE)) {
			importConfiguration = configurationLoader.load(configStream, ImportConfiguration.class);
		}
	}

	@Override
	public Summary doImport(final InputStream inputStream, final String changesetGuid) throws ConfigurationException {
		metaDataMapPopulator.configureMetadataMapForImport(changesetGuid, null);
		getMetadataMap().put("activeImportStage", importStage.getId());

		boolean importStageActiveState = importStage.isActive();
		LOG.debug(String.format("Import Stage: '%s' Active State: '%s'", importStage, importStageActiveState));
		if (!importStageActiveState) {
			return new SummaryImpl();
		}

		ImportContext context = new ImportContext(importConfiguration);

		LOG.debug(new Message("IE-30005", Boolean.toString(importConfiguration.isXmlValidation())));

		LogAppenderUtil.initializeSummary(context);

		importStage.execute(inputStream, context);

		return LogAppenderUtil.detachSummary(context);
	}

	protected ConfigurationLoader getConfigurationLoader() {
		return configurationLoader;
	}

	public void setConfigurationLoader(final ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}

	protected ImportStage getImportStage() {
		return importStage;
	}

	public void setImportStage(final ImportStage importStage) {
		this.importStage = importStage;
	}

	protected ThreadLocalMap<String, Object> getMetadataMap() {
		return metadataMap;
	}

	public void setMetadataMap(final ThreadLocalMap<String, Object> metadataMap) {
		this.metadataMap = metadataMap;
	}

	protected MetaDataMapPopulator getMetaDataMapPopulator() {
		return metaDataMapPopulator;
	}

	public void setMetaDataMapPopulator(final MetaDataMapPopulator metaDataMapPopulator) {
		this.metaDataMapPopulator = metaDataMapPopulator;
	}
}
