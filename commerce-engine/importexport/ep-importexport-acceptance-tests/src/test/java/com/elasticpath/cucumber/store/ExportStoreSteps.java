/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import com.google.common.base.Splitter;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.store.StoreDTO;
import com.elasticpath.common.dto.store.StoreGlobalizationDTO;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.store.StoresDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.ql.parser.EPQueryType;
import com.elasticpath.search.searchengine.EpQLSearchEngine;
import com.elasticpath.search.searchengine.SolrIndexSearchResult;

/**
 * Export Store.
 */
public class ExportStoreSteps {

	private static final String EXISTING_STORE_EPQL_QUERY = "FIND Store WHERE StoreCode=";
	private static final String STORE_EXPORT_FILE = "stores.xml";

	private static final String MANIFEST_EXPORT_FILE = "manifest.xml";
	private static int runNumber = 1;

	@Autowired
	private ExportController exportController;

	@Autowired
	private EpQLSearchEngine epQLSearchEngine;

	private StoresDTO storesDTO;
	private File exportDirectory;

	/**
	 * Export store with importexport tool.
	 * @param storeCode the store code to find
	 * @throws Exception the exception
	 */
	@When("^exporting store with store code (.+) the importexport tool")
	public void runExport(final String storeCode) throws Exception {
		final String epQLSearchQuery = EXISTING_STORE_EPQL_QUERY + "'" + storeCode + "'";

		List<String> storeCodes = Collections.singletonList(storeCode);
		SolrIndexSearchResult<String> storeQuery = new SolrIndexSearchResult<>();
		storeQuery.setEpQueryType(EPQueryType.STORE);
		storeQuery.setSearchResults(storeCodes);

		when(epQLSearchEngine.<String>search(epQLSearchQuery)).thenReturn(storeQuery);

		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.STORE))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();
		searchConfiguration.setEpQLQuery(epQLSearchQuery);

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Parses the exported store data.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@When("^the exported store data is parsed$")
	public void parseExportedStoreData() throws IOException {
		final File exportedStoreFile = new File(exportDirectory, STORE_EXPORT_FILE);

		assertThat(exportedStoreFile)
				.as("Exported store file not found: %s", exportedStoreFile
						.getAbsolutePath())
				.exists();

		try (final FileInputStream exportedStoreFileStream =
					 new FileInputStream(exportedStoreFile)) {
			final XMLUnmarshaller storesUnmarshaller = new XMLUnmarshaller(StoresDTO.class);
			storesDTO = storesUnmarshaller.unmarshall(exportedStoreFileStream);
		}
	}

	/**
	 * Ensure the store is exported.
	 *
	 * @param storeMap store info.
	 */
	@Then("^the exported store records should equal$")
	public void ensureStoreExported(final Map<String, String> storeMap) {
		final List<StoreDTO> matchingStoreDTOs =
				storesDTO.getStores();

		final StoreDTO storeDTO = new StoreDTO();
		StoreGlobalizationDTO globalizationDTO = new StoreGlobalizationDTO();
		globalizationDTO.setCountry(storeMap.get("store country"));
		globalizationDTO.setDefaultCurrency(Currency.getInstance(storeMap.get("currency")));
		globalizationDTO.setSubCountry(storeMap.get("store sub country"));
		globalizationDTO.setTimeZone(TimeZone.getTimeZone(storeMap.get("timezone")));
		storeDTO.setGlobalization(globalizationDTO);
		storeDTO.setCode(storeMap.get("store code"));
		storeDTO.setName(storeMap.get("store name"));
		storeDTO.setAuthenticatedB2CRole(storeMap.get("authenticated role"));
		storeDTO.setSingleSessionB2CRole(storeMap.get("unauthenticated role"));

		@SuppressWarnings("UnstableApiUsage") final List<String> paymentProviderConfigGuids = Splitter.on(",")
				.omitEmptyStrings()
				.splitToList(storeMap.get("payment provider configs"));

		storeDTO.getPaymentProviderPluginConfigGuids().addAll(paymentProviderConfigGuids);

		StoreDTO exportedStoreDTO = findStoreDTOByStoreCode(
				matchingStoreDTOs, storeDTO.getCode());

		assertThat(exportedStoreDTO).isEqualToComparingOnlyGivenFields(storeDTO,
				"code", "name", "globalization.country", "globalization.defaultCurrency", "globalization.subCountry",
				"globalization.timeZone", "paymentProviderPluginConfigGuids", "authenticatedB2CRole", "singleSessionB2CRole");
	}

	private StoreDTO findStoreDTOByStoreCode(
			final List<StoreDTO> storeDTOs, final String storeCode) {

		final Optional<StoreDTO> matchingStoreDTOs = storeDTOs
				.stream()
				.filter(store -> storeCode.equals(store.getCode()))
				.findFirst();

		return matchingStoreDTOs.orElse(null);
	}

	/**
	 * Ensure manifest file includes an entry for stores.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Then("^the exported manifest file should have an entry for stores$")
	public void ensureManifestIncludesStore() throws IOException {
		final File exportedManifestFile = new File(exportDirectory, MANIFEST_EXPORT_FILE);

		assertThat(exportedManifestFile)
				.as("Exported manifest file not found: %s", exportedManifestFile.getAbsolutePath())
				.exists();

		try (final FileInputStream exportedManifestFileStream = new FileInputStream(exportedManifestFile)) {
			final XMLUnmarshaller manifestUnmarshaller = new XMLUnmarshaller(Manifest.class);
			final Manifest manifest = manifestUnmarshaller.unmarshall(exportedManifestFileStream);

			assertThat(manifest.getResources())
					.as("Manifest file missing stores entry")
					.contains(STORE_EXPORT_FILE);
		}
	}
}
