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
import static com.elasticpath.common.dto.search.FacetDTOConstants.RANGE_FACET_VALUES;
import static com.elasticpath.common.dto.search.FacetDTOConstants.SEARCHABLE_OPTION;
import static com.elasticpath.common.dto.search.FacetDTOConstants.STORE_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.search.FacetDTO;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.impl.FacetImpl;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.facet.FacetsDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.service.search.FacetService;

/**
 * Export facet steps.
 */
public class ExportFacetSteps {

	private static int runNumber = 1;

	@Autowired
	private ExportController exportController;

	@Autowired
	private FacetService facetService;

	private File exportDirectory;

	private FacetsDTO facetsDTO;

	/**
	 * Persist the facets in the passed data table into the database.
	 *
	 * @param dataTable the facet data to persist
	 */
	@Given("^the following facets are saved in the database$")
	public void saveFacets(final DataTable dataTable) {
		List<Map<String, String>> facetMaps = dataTable.asMaps(String.class, String.class);
		for (Map<String, String> facetMap : facetMaps) {
			Facet facet = new FacetImpl();
			facet.setFacetGuid(facetMap.get(FACET_GUID));
			facet.setBusinessObjectId(facetMap.get(BUSINESS_OBJECT_ID));
			facet.setFacetName(facetMap.get(FACET_NAME));
			facet.setFieldKeyType(Integer.valueOf(facetMap.get(FIELD_KEY_TYPE)));
			facet.setStoreCode(facetMap.get(STORE_CODE));
			facet.setDisplayName(facetMap.get(DISPLAY_NAME));
			facet.setFacetType(Integer.valueOf(facetMap.get(FACET_TYPE)));
			facet.setSearchableOption(Boolean.valueOf(SEARCHABLE_OPTION));
			facet.setRangeFacetValues(facetMap.get(RANGE_FACET_VALUES));
			facet.setFacetGroup(Integer.valueOf(facetMap.get(FACET_GROUP)));
			facetService.saveOrUpdate(facet);
		}
	}

	/**
	 * Export the facet data from the database into an export folder.
	 *
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the facets in the database are exported using importexport$")
	public void exportFacets() throws ConfigurationException {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.FACET))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Retrieve the facet data from the export folder into the facetsDTO object in memory.
	 * @throws IOException if there was a problem reading or writing the facet data
	 */
	@When("^the exported facets are retrieved$")
	public void retrieveExportedFacets() throws IOException {
		final File exportedFacetFile = new File(exportDirectory, FACETS_FILE);

		assertThat(exportedFacetFile)
				.as(String.format("Exported facet file not found: %s", exportedFacetFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedFacetInputStream = new FileInputStream(exportedFacetFile)) {
			final XMLUnmarshaller facetUnmarshaller = new XMLUnmarshaller(FacetsDTO.class);
			facetsDTO = facetUnmarshaller.unmarshall(exportedFacetInputStream);
		}
	}

	/**
	 * Verify that the facetsDTO object in memory contains the passed facet guid.
	 * @param guid the facet guid to verify
	 */
	@Then("^the exported facet records contain a facet with guid ([\\w]+)$")
	public void verifyFacetsDTOContainsGuid(final String guid) {
		FacetDTO facet = facetsDTO.getFacets().stream()
				.filter(facetDTO -> facetDTO.getFacetGuid().equals(guid))
				.findFirst()
				.orElse(null);

		assertThat(facet)
				.as(String.format("Facet with guid %s not found", guid))
				.isNotNull();
	}
}
