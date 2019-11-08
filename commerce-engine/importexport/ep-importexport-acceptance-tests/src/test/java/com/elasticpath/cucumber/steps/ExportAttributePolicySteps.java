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

import com.elasticpath.common.dto.customer.AttributePolicyDTO;
import com.elasticpath.domain.customer.PolicyKey;
import com.elasticpath.domain.customer.PolicyPermission;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.customer.AttributePoliciesDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;

/**
 * Export attribute policy steps.
 */
public class ExportAttributePolicySteps {

	private static final String ATTRIBUTE_POLICY_EXPORT_FILE = "attribute_policies.xml";

	private static final String MANIFEST_EXPORT_FILE = "manifest.xml";

	@Autowired
	private ExportController exportController;

	private AttributePoliciesDTO policiesDTO;

	private File exportDirectory;

	private static int runNumber = 1;

	/**
	 * Export attribute policies with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^exporting attribute policies with the importexport tool")
	public void runExport() throws Exception {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Arrays.asList(RequiredJobType.ATTRIBUTE_POLICY))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Parses the exported attribute policies data.
	 *
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	@When("^the exported attribute policy data is parsed$")
	public void parseExportedAttributePolicyData() throws IOException {
		final File exportedPoliciesFile = new File(exportDirectory, ATTRIBUTE_POLICY_EXPORT_FILE);

		assertThat(exportedPoliciesFile)
				.as(String.format("Exported attribute policies file not found: %s", exportedPoliciesFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedPolicyFileStream = new FileInputStream(exportedPoliciesFile)) {
			final XMLUnmarshaller policyGroupUnmarshaller = new XMLUnmarshaller(AttributePoliciesDTO.class);
			policiesDTO = policyGroupUnmarshaller.unmarshall(exportedPolicyFileStream);
		}
	}

	/**
	 * Ensure the attribute policies were exported with correct data.
	 *
	 * @param policyDataTable policy data table
	 */
	@Then("^the exported attribute policy records should include$")
	public void ensureAttributePoliciesExport(final DataTable policyDataTable) {
		final List<Map<String, String>> policyMaps = policyDataTable.asMaps(String.class, String.class);
		policyMaps.forEach(this::ensureAttributePolicyExport);
	}

	private void ensureAttributePolicyExport(final Map<String, String> policyMap) {
		final String guid = policyMap.get("guid");
		final PolicyKey policyKey = PolicyKey.valueOf(policyMap.get("policyKey"));
		final PolicyPermission policyPermission = PolicyPermission.valueOf(policyMap.get("policyPermission"));

		final Optional<AttributePolicyDTO> policyDTO =
				findAttributePolicyDTOByGuid(policiesDTO.getAttributePolicies(), guid);

		assertThat(policyDTO.isPresent())
				.as(String.format("Attribute policy [%s] not found in exported records", guid))
				.isTrue();

		assertThat(policyKey.getName())
				.as(String.format("Attribute policy [%s] is exported with incorrect policy key", guid))
				.isEqualTo(policyDTO.get().getPolicyKey());

		assertThat(policyPermission.getName())
				.as(String.format("Attribute policy [%s] is exported with incorrect policy permission", guid))
				.isEqualTo(policyDTO.get().getPolicyPermission());
	}

	private Optional<AttributePolicyDTO> findAttributePolicyDTOByGuid(
			final List<AttributePolicyDTO> policyDTOs, final String guid) {
		return policyDTOs
				.stream()
				.filter(policyDTO -> guid.equals(policyDTO.getGuid()))
				.findFirst();
	}

	/**
	 * Ensure manifest file includes an entry for attribute policies.
	 *
	 * @throws IOException Signals that an I/O exception has occurred
	 */
	@Then("^the exported manifest file should have an entry for attribute policies$")
	public void ensureManifestIncludesAttributePolicies() throws IOException {
		final File exportedManifestFile = new File(exportDirectory, MANIFEST_EXPORT_FILE);

		assertThat(exportedManifestFile)
				.as(String.format("Exported manifest file not found: %s", exportedManifestFile.getAbsolutePath()))
				.exists();


		try (final FileInputStream exportedManifestFileStream = new FileInputStream(exportedManifestFile)) {
			final XMLUnmarshaller manifestUnmarshaller = new XMLUnmarshaller(Manifest.class);
			final Manifest manifest = manifestUnmarshaller.unmarshall(exportedManifestFileStream);

			assertThat(manifest.getResources())
					.as("Manifest file missing attribute policies entry")
					.contains(ATTRIBUTE_POLICY_EXPORT_FILE);
		}
	}
}
