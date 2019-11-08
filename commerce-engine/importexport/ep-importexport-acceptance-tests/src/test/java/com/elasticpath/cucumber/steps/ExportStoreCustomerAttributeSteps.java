/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.customer.StoreCustomerAttributesDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;

/**
 * Export store customer attribute steps.
 */
public class ExportStoreCustomerAttributeSteps {

	private static final String STORE_CUSTOMER_ATTRIBUTE_EXPORT_FILE = "store_customer_attributes.xml";

	private static final String MANIFEST_EXPORT_FILE = "manifest.xml";

	@Autowired
	private ExportController exportController;

	private StoreCustomerAttributesDTO attributesDTO;

	private File exportDirectory;

	private static int runNumber = 1;

	/**
	 * Export store customer attributes with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^exporting store customer attributes with the importexport tool")
	public void runExport() throws Exception {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Arrays.asList(RequiredJobType.STORE_CUSTOMER_ATTRIBUTE))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Parses the exported store customer attributes data.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@When("^the exported store customer attribute data is parsed$")
	public void parseExportedStoreCustomerAttributeData() throws IOException {
		final File exportedAttributesFile = new File(exportDirectory, STORE_CUSTOMER_ATTRIBUTE_EXPORT_FILE);

		assertThat(exportedAttributesFile)
				.as(String.format("Exported store customer attributes file not found: %s", exportedAttributesFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedAttributesFileStream = new FileInputStream(exportedAttributesFile)) {
			final XMLUnmarshaller attributeGroupUnmarshaller = new XMLUnmarshaller(StoreCustomerAttributesDTO.class);
			attributesDTO = attributeGroupUnmarshaller.unmarshall(exportedAttributesFileStream);
		}
	}

	/**
	 * Ensure the attribute policy is exported with correct data.
	 *
	 * @param attributeDataTable attribute data table
	 */
	@Then("^the exported store customer attribute records should include$")
	public void ensureStoreCustomerAttributesExport(final DataTable attributeDataTable) {
		final List<Map<String, String>> attributeMaps = attributeDataTable.asMaps(String.class, String.class);
		attributeMaps.forEach(this::ensureStoreCustomerAttributeExport);
	}

	private void ensureStoreCustomerAttributeExport(final Map<String, String> attributeMap) {
		final String guid = attributeMap.get("guid");
		final String storeCode = attributeMap.get("storeCode");
		final String attributeKey = attributeMap.get("attributeKey");
		final PolicyKey policyKey = PolicyKey.valueOf(attributeMap.get("policyKey"));

		final Optional<StoreCustomerAttributeDTO> attributeDTO =
				findStoreCustomerAttributeDTOByGuid(attributesDTO.getStoreCustomerAttributes(), guid);

		assertThat(attributeDTO.isPresent())
				.as(String.format("Store customer attribute [%s] not found in exported records", guid))
				.isTrue();

		assertThat(storeCode)
				.as(String.format("Store customer attribute [%s] is exported with incorrect store code", guid))
				.isEqualTo(attributeDTO.get().getStoreCode());

		assertThat(attributeKey)
				.as(String.format("Store customer attribute [%s] is exported with incorrect store code", guid))
				.isEqualTo(attributeDTO.get().getAttributeKey());

		assertThat(policyKey.getName())
				.as(String.format("Store customer attribute [%s] is exported with incorrect policy key", guid))
				.isEqualTo(attributeDTO.get().getPolicyKey());
	}

	private Optional<StoreCustomerAttributeDTO> findStoreCustomerAttributeDTOByGuid(final List<StoreCustomerAttributeDTO> attributeDTOs,
																					final String guid) {
		return attributeDTOs
				.stream()
				.filter(attributeDTO -> guid.equals(attributeDTO.getGuid()))
				.findFirst();
	}

	/**
	 * Ensure manifest file includes an entry for attribute policies.
	 *
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	@Then("^the exported manifest file should have an entry for store customer attributes$")
	public void ensureManifestIncludesStoreCustomerAttributes() throws IOException {
		final File exportedManifestFile = new File(exportDirectory, MANIFEST_EXPORT_FILE);

		assertThat(exportedManifestFile)
				.as(String.format("Exported manifest file not found: %s", exportedManifestFile.getAbsolutePath()))
				.exists();


		try (final FileInputStream exportedManifestFileStream = new FileInputStream(exportedManifestFile)) {
			final XMLUnmarshaller manifestUnmarshaller = new XMLUnmarshaller(Manifest.class);
			final Manifest manifest = manifestUnmarshaller.unmarshall(exportedManifestFileStream);

			assertThat(manifest.getResources())
					.as("Manifest file missing store customer attributes entry")
					.contains(STORE_CUSTOMER_ATTRIBUTE_EXPORT_FILE);
		}
	}
}
