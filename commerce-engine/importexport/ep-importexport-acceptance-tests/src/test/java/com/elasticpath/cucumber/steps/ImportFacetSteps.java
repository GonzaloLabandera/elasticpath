/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

import static com.elasticpath.common.dto.search.FacetDTOConstants.BUSINESS_OBJECT_ID;
import static com.elasticpath.common.dto.search.FacetDTOConstants.DISPLAY_NAME;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACETS_FILE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_GROUP;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_GUID;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_NAME;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FACET_TYPE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.FIELD_KEY_TYPE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.MANIFEST_IMPORT_FILE;
import static com.elasticpath.common.dto.search.FacetDTOConstants.RANGE_FACET_VALUES;
import static com.elasticpath.common.dto.search.FacetDTOConstants.SEARCHABLE_OPTION;
import static com.elasticpath.common.dto.search.FacetDTOConstants.STORE_CODE;
import static com.elasticpath.commons.util.TestDomainMarshaller.marshalObject;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.common.dto.search.FacetDTO;
import com.elasticpath.common.dto.search.RangeFacetDTO;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.facet.FacetsDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.service.search.FacetService;

/**
 * Import Facet steps.
 */
public class ImportFacetSteps {

	private static int runNumber = 1;

	@Autowired
	private ImportController importController;

	@Autowired
	private FacetService facetService;

	private FacetsDTO facetsDTO;

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Reset the facet DTO object in memory.
	 */
	@Given("^the facets import data has been emptied out$")
	public void clearFacetImportData() {
		facetsDTO = new FacetsDTO();
	}

	/**
	 * Import facts from the passed data table into the facet DTO object and trigger an import.
	 *
	 * @param dataTable the data table containing the facet data
	 * @throws IOException if there was a problem reading or writing the facet data
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the following facets are imported using importexport$")
	public void importFacets(final DataTable dataTable) throws IOException, ConfigurationException {
		facetsDTO.getFacets().addAll(buildFacetDTOsFromDataTable(dataTable));

		File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		Manifest manifest = new Manifest();
		manifest.addResource(FACETS_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(FacetsDTO.class, facetsDTO, new File(importDirectory, FACETS_FILE));

		ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.FACET, ImportStrategyType.INSERT)
				.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}

	private List<FacetDTO> buildFacetDTOsFromDataTable(final DataTable dataTable) throws IOException {
		List<Map<String, String>> facetMaps = dataTable.asMaps(String.class, String.class);
		List<FacetDTO> facetDTOs = new ArrayList<>();
		for (Map<String, String> facetMap : facetMaps) {
			FacetDTO facetDTO = new FacetDTO();
			facetDTO.setFacetGuid(facetMap.get(FACET_GUID));
			facetDTO.setBusinessObjectId(facetMap.get(BUSINESS_OBJECT_ID));
			facetDTO.setFacetName(facetMap.get(FACET_NAME));
			facetDTO.setFieldKeyType(Integer.valueOf(facetMap.get(FIELD_KEY_TYPE)));
			facetDTO.setStoreCode(facetMap.get(STORE_CODE));

			List<DisplayValue> displayValues = new ArrayList<>();
			displayValues.addAll(Arrays.asList(objectMapper.readValue(facetMap.get(DISPLAY_NAME), DisplayValue[].class)));

			facetDTO.setDisplayValues(displayValues);
			facetDTO.setFacetType(Integer.valueOf(facetMap.get(FACET_TYPE)));
			facetDTO.setSearchableOption(Boolean.valueOf(SEARCHABLE_OPTION));

			List<RangeFacetDTO> rangeFacetDTOs = new ArrayList<>();
			rangeFacetDTOs.addAll(Arrays.asList(objectMapper.readValue(facetMap.get(RANGE_FACET_VALUES), RangeFacetDTO[].class)));
			facetDTO.setRangeFacetValues(rangeFacetDTOs);

			facetDTO.setFacetGroup(Integer.valueOf(facetMap.get(FACET_GROUP)));
			facetDTOs.add(facetDTO);
		}
		return facetDTOs;
	}

	/**
	 * Verify that the database contains a facet with the passed guid.
	 *
	 * @param guid the facet guid to verify
	 */
	@Then("^the facet with guid ([\\w]+) is persisted")
	public void verifyFacetIsPersisted(final String guid) {
		Facet facet = facetService.findByGuid(guid);

		assertThat(facet)
				.as(String.format("Facet with guid %s not found", guid))
				.isNotNull();
	}
}
