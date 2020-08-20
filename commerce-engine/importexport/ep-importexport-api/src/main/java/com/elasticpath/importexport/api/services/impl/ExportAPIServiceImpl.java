/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.api.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.elasticpath.importexport.api.exceptions.EpInvalidRequest;
import com.elasticpath.importexport.api.services.ExportAPIService;
import com.elasticpath.importexport.common.configuration.ConfigurationLoader;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.summary.impl.NullSummaryImpl;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchQuery;
import com.elasticpath.importexport.exporter.context.DependencyRegistry;
import com.elasticpath.importexport.exporter.context.ExportContext;
import com.elasticpath.importexport.exporter.exportentry.ExportEntry;
import com.elasticpath.importexport.exporter.exporters.Exporter;
import com.elasticpath.importexport.exporter.exporters.ExporterFactory;

/**
 * Service for generating Import/Export exports as an InputStream.
 */
public class ExportAPIServiceImpl implements ExportAPIService {
	private static final int BUFFER_SIZE = 1024;
	private static final String EXPORT_CONFIGURATION_FILE = "exportconfiguration.xml";

	private ConfigurationLoader configurationLoader;
	private ExportConfiguration exportConfiguration;
	private ExporterFactory exporterFactory;

	/**
	 * Initialize the service during Spring startup.
	 *
	 * @throws IOException if there is an issue reading the export configuration
	 * @throws ConfigurationException if there is an issue with the export configuration file
	 */
	public void initialize() throws IOException, ConfigurationException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		try (InputStream configStream = classLoader.getResourceAsStream(EXPORT_CONFIGURATION_FILE)) {
			exportConfiguration = configurationLoader.load(configStream, ExportConfiguration.class);
		}
	}

	@Override
	public InputStream doExport(final String type, final String parentType, final String query) throws ConfigurationException, IOException {
		JobType parentJobType = null;
		if (parentType != null) {
			parentJobType = getJobType(parentType);
		}
		JobType jobType = getJobType(type);

		SearchConfiguration searchConfiguration;
		if (parentJobType == null) {
			searchConfiguration = getSearchConfiguration(jobType, query);
		} else {
			searchConfiguration = getSearchConfiguration(parentJobType, query);
		}
		ExportContext context = new ExportContext(exportConfiguration, searchConfiguration);
		context.setSummary(new NullSummaryImpl());

		List<Exporter> configuredExporters = new ArrayList<>(exporterFactory.getAllConfiguredExporters(context));
		final DependencyRegistry dependencyRegistry = new DependencyRegistry(getDependentTypes(configuredExporters));
		context.setDependencyRegistry(dependencyRegistry);

		Exporter exporter = getExporterForType(jobType, configuredExporters);
		if (parentJobType == null) {
			exporter.initialize(context);
		} else {
			Exporter parentExporter = getExporterForType(parentJobType, configuredExporters);
			parentExporter.initialize(context);
			exporter.initialize(context);
			ExportEntry parentExportEntry = parentExporter.executeExport();
			// Consume parent input stream so that dependencies are added to dependency registry
			consumeInputStream(parentExportEntry.getInputStream());
		}
		ExportEntry exportEntry = exporter.executeExport();

		return exportEntry.getInputStream();
	}

	/**
	 * Get a configured SearchConfiguration object for the passed jobType and query.
	 *
	 * @param jobType the jobType that we're searching on
	 * @param query the query to use when searching
	 * @return the configured SerachConfiguration object
	 */
	protected SearchConfiguration getSearchConfiguration(final JobType jobType, final String query) {
		if (jobType.getEpQueryType() == null && query != null) {
			throw new EpInvalidRequest(jobType.name() + " does not support queries.");
		}

		SearchConfiguration searchConfiguration = new SearchConfiguration();
		if (jobType.getEpQueryType() != null && query == null) {
			String effectiveQuery = "FIND " + jobType.getEpQueryType().getTypeName();
			searchConfiguration.getQueries().add(new SearchQuery(jobType.getEpQueryType().getTypeName(), effectiveQuery));
		} else if (jobType.getEpQueryType() != null && query != null) {
			searchConfiguration.getQueries().add(new SearchQuery(jobType.getEpQueryType().getTypeName(), query));
		}
		return searchConfiguration;
	}

	@SuppressWarnings({"PMD.EmptyBlock"})
	private void consumeInputStream(final InputStream inputStream) throws IOException {
		try {
			byte[] bytes = new byte[BUFFER_SIZE];
			//CHECKSTYLE:OFF
			while ((inputStream.read(bytes, 0, BUFFER_SIZE)) != -1) {
				// Do nothing
			}
			//CHECKSTYLE:ON
		} finally {
			inputStream.close();
		}
	}

	/**
	 * Get JobType enum with the passed name.
	 * @param type the JobType name
	 * @return the corresponding JobType enum
	 */
	protected JobType getJobType(final String type) {
		return Arrays.stream(JobType.values())
						.filter(thisJobType -> thisJobType.name().equalsIgnoreCase(type))
						.findAny()
						.orElseThrow(() -> new EpInvalidRequest("Unable to find Import/Export job type " + type + "."));
	}

	/**
	 * Get Exporter for the passed jobType from the passed list of exporters.
	 * @param jobType the JobType enum
	 * @param exporters the list of exporters to search
	 * @return the corresponding exporter
	 */
	protected Exporter getExporterForType(final JobType jobType, final List<Exporter> exporters) {
		return exporters.stream()
				.filter(exporter -> exporter.getJobType() == jobType)
				.findAny()
				.orElseThrow(() -> new EpInvalidRequest("Unable to find exporter of type " + jobType.name()));
	}

	private List<Class<?>> getDependentTypes(final List<Exporter> exporterSequence) {
		List<Class<?>> dependentTypes = new ArrayList<>();
		for (Exporter exporter : exporterSequence) {
			for (Class<?> clazz : exporter.getDependentClasses()) {
				dependentTypes.add(clazz);
			}
		}
		return dependentTypes;
	}

	protected ConfigurationLoader getConfigurationLoader() {
		return configurationLoader;
	}

	public void setConfigurationLoader(final ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}

	protected ExporterFactory getExporterFactory() {
		return exporterFactory;
	}

	public void setExporterFactory(final ExporterFactory exporterFactory) {
		this.exporterFactory = exporterFactory;
	}
}
