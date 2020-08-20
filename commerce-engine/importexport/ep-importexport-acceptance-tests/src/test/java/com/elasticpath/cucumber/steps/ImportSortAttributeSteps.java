/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.BUSINESS_OBJECT_ID;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DEFAULT_ATTRIBUTE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DESCENDING;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DISPLAY_VALUE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.MANIFEST_IMPORT_FILE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTES_FILE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_GUID;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_GROUP;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_TYPE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.STORE_CODE;
import static com.elasticpath.commons.util.TestDomainMarshaller.marshalObject;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.sort.SortAttributeDTO;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.sort.SortAttributesDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Import SortAttribute steps.
 */
public class ImportSortAttributeSteps {

	private static int runNumber = 1;

	@Autowired
	private ImportController importController;

	@Autowired
	private SortAttributeService sortAttributeService;

	private SortAttributesDTO sortAttributesDTO;

	/**
	 * Reset the sortAttribute DTO object in memory.
	 */
	@Given("^the sort attribute import data has been emptied out$")
	public void clearSortAttributesImportData() {
		sortAttributesDTO = new SortAttributesDTO();
	}

	/**
	 * Import sort attributes from the passed data table into the sortAttribute DTO object and trigger an import.
	 *
	 * @param dataTable the data table containing the sortAttribute data
	 * @throws IOException            if there was a problem reading or writing the sortAttribute data
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the following sortAttributes are imported using importexport$")
	public void importSortAttributes(final DataTable dataTable) throws IOException, ConfigurationException {
		sortAttributesDTO.getSortAttributes().addAll(buildSortAttributeDTOsFromDataTable(dataTable));

		File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		Manifest manifest = new Manifest();
		manifest.addResource(SORT_ATTRIBUTES_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(SortAttributesDTO.class, sortAttributesDTO, new File(importDirectory, SORT_ATTRIBUTES_FILE));

		ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.SORTATTRIBUTE, ImportStrategyType.INSERT_OR_UPDATE)
				.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}

	private List<SortAttributeDTO> buildSortAttributeDTOsFromDataTable(final DataTable dataTable) throws IOException {
		List<Map<String, String>> sortAttributeMaps = dataTable.asMaps(String.class, String.class);
		List<SortAttributeDTO> sortAttributeDTOs = new ArrayList<>();
		for (Map<String, String> sortAttributeMap : sortAttributeMaps) {
			SortAttributeDTO sortAttributeDTO = new SortAttributeDTO();
			sortAttributeDTO.setSortAttributeGuid(sortAttributeMap.get(SORT_ATTRIBUTE_GUID));
			sortAttributeDTO.setBusinessObjectId(sortAttributeMap.get(BUSINESS_OBJECT_ID));
			sortAttributeDTO.setSortAttributeGroup(sortAttributeMap.get(SORT_ATTRIBUTE_GROUP));
			sortAttributeDTO.setStoreCode(sortAttributeMap.get(STORE_CODE));
			sortAttributeDTO.setDescending(Boolean.getBoolean(sortAttributeMap.get(DESCENDING)));
			sortAttributeDTO.setDisplayValues(Collections.singletonList(new DisplayValue("en", sortAttributeMap.get(DISPLAY_VALUE))));
			sortAttributeDTO.setSortAttributeType(sortAttributeMap.get(SORT_ATTRIBUTE_TYPE));
			sortAttributeDTO.setDefaultAttribute(Boolean.getBoolean(sortAttributeMap.get(DEFAULT_ATTRIBUTE)));
			sortAttributeDTOs.add(sortAttributeDTO);
		}
		return sortAttributeDTOs;
	}

	/**
	 * Verify that the database contains a sortAttribute with the passed guid.
	 *
	 * @param guid the sortAttribute guid to verify
	 */
	@Then("^the sortAttribute with guid ([\\w]+) is persisted")
	public void verifySortAttributeIsPersisted(final String guid) {
		SortAttribute sortAttribute = sortAttributeService.findByGuid(guid);
		assertThat(sortAttribute)
				.as(String.format("SortAttribute with guid %s not found", guid))
				.isNotNull();
	}
}
