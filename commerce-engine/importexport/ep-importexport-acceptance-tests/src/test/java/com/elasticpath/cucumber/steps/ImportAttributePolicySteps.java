/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.steps;


import static com.elasticpath.commons.util.TestDomainMarshaller.marshalObject;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.customer.AttributePolicyDTO;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.customer.AttributePoliciesDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;

/**
 * Import attribute policy steps.
 */
public class ImportAttributePolicySteps {

	private static final String ATTRIBUTE_POLICIES_IMPORT_FILE = "attribute_policies.xml";

	private static final String MANIFEST_EXPORT_FILE = "manifest.xml";

	@Autowired
	private ImportController importController;

	private AttributePoliciesDTO dtoForImport;

	private File importDirectory;

	private static int runNumber = 1;

	/**
	 * Clear import data.
	 */
	@Given("^the attribute policy import data has been emptied out$")
	public void clearAttributePolicyImportData() {
		dtoForImport = new AttributePoliciesDTO();
	}

	/**
	 * Setup the tests with attribute policies.
	 *
	 * @param dataTable attribute policy data table
	 */
	@Given("^the attribute policies to import of$")
	public void setUpAttributePolicies(final DataTable dataTable) {
		final List<AttributePolicyDTO> policies = createAttributePolicyDTOs(dataTable.asMaps(String.class, String.class));
		dtoForImport.getAttributePolicies().addAll(policies);
	}

	private List<AttributePolicyDTO> createAttributePolicyDTOs(final List<Map<String, String>> attributePolicies) {
		return attributePolicies.stream()
				.map(this::createAttributePolicyDTO)
				.collect(Collectors.toList());
	}

	private AttributePolicyDTO createAttributePolicyDTO(final Map<String, String> attributePolicyMap) {
		final AttributePolicyDTO policyDTO = new AttributePolicyDTO();
		policyDTO.setGuid(attributePolicyMap.get("guid"));
		policyDTO.setPolicyKey(attributePolicyMap.get("policyKey"));
		policyDTO.setPolicyPermission(attributePolicyMap.get("policyPermission"));
		return policyDTO;
	}

	/**
	 * Import attribute policy with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^importing attribute policies with the importexport tool$")
	public void executeImport() throws Exception {
		importDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		final Manifest manifest = new Manifest();
		manifest.addResource(ATTRIBUTE_POLICIES_IMPORT_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_EXPORT_FILE));
		marshalObject(AttributePoliciesDTO.class, dtoForImport,
				new File(importDirectory, ATTRIBUTE_POLICIES_IMPORT_FILE));

		final ImportConfiguration importConfiguration =
				ImportConfigurationBuilder.newInstance()
						.setRetrievalSource(importDirectory.getPath())
						.addImporterConfiguration(JobType.ATTRIBUTEPOLICY, ImportStrategyType.INSERT)
						.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}
}
