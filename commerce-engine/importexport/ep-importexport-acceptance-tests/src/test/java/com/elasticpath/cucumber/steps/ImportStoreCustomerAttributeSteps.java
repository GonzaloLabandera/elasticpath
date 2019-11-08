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

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.customer.StoreCustomerAttributesDTO;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;

/**
 * Import Store customer attribute steps.
 */
public class ImportStoreCustomerAttributeSteps {

	private static final String STORE_CUSTOMER_ATTRIBUTES_IMPORT_FILE = "store_customer_attributes.xml";

	private static final String MANIFEST_EXPORT_FILE = "manifest.xml";

	@Autowired
	private ImportController importController;

	private StoreCustomerAttributesDTO dtoForImport;

	private File importDirectory;

	private static int runNumber = 1;

	/**
	 * Clear import data.
	 */
	@Given("^the store customer attribute import data has been emptied out$")
	public void clearAttributePolicyImportData() {
		dtoForImport = new StoreCustomerAttributesDTO();
	}

	/**
	 * Setup the tests with attribute policies.
	 *
	 * @param dataTable attribute policy data table
	 */
	@Given("^the store customer attributes to import of$")
	public void setUpAttributePolicies(final DataTable dataTable) {
		final List<StoreCustomerAttributeDTO> attributes = createAttributeDTOs(dataTable.asMaps(String.class, String.class));
		dtoForImport.getStoreCustomerAttributes().addAll(attributes);
	}

	private List<StoreCustomerAttributeDTO> createAttributeDTOs(final List<Map<String, String>> attributes) {
		return attributes.stream()
				.map(this::createAttributeDTO)
				.collect(Collectors.toList());
	}

	private StoreCustomerAttributeDTO createAttributeDTO(final Map<String, String> attributeMap) {
		final StoreCustomerAttributeDTO attributeDTO = new StoreCustomerAttributeDTO();
		attributeDTO.setGuid(attributeMap.get("guid"));
		attributeDTO.setStoreCode(attributeMap.get("storeCode"));
		attributeDTO.setAttributeKey(attributeMap.get("attributeKey"));
		attributeDTO.setPolicyKey(attributeMap.get("policyKey"));
		return attributeDTO;
	}

	/**
	 * Import store customer attribute with importexport tool.
	 *
	 * @throws Exception the exception
	 */
	@When("^importing store customer attributes with the importexport tool$")
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
		manifest.addResource(STORE_CUSTOMER_ATTRIBUTES_IMPORT_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_EXPORT_FILE));
		marshalObject(StoreCustomerAttributesDTO.class, dtoForImport,
				new File(importDirectory, STORE_CUSTOMER_ATTRIBUTES_IMPORT_FILE));

		final ImportConfiguration importConfiguration =
				ImportConfigurationBuilder.newInstance()
						.setRetrievalSource(importDirectory.getPath())
						.addImporterConfiguration(JobType.STORE_CUSTOMER_ATTRIBUTE, ImportStrategyType.INSERT)
						.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}
}
