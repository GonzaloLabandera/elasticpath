/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cucumber.steps;

import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.BUSINESS_OBJECT_ID;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DEFAULT_ATTRIBUTE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DESCENDING;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.DISPLAY_VALUE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTES_FILE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_GUID;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_GROUP;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.SORT_ATTRIBUTE_TYPE;
import static com.elasticpath.common.dto.sort.SortAttributeDTOConstants.STORE_CODE;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.sort.SortAttributeDTO;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortAttributeType;
import com.elasticpath.domain.search.SortLocalizedName;
import com.elasticpath.domain.search.impl.SortAttributeImpl;
import com.elasticpath.domain.search.impl.SortLocalizedNameImpl;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.sort.SortAttributesDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.service.search.SortAttributeService;

/**
 * Export sortAttribute steps.
 */
public class ExportSortAttributeSteps {

	private static final String LOCALE_CODE = "en";
	private static int runNumber = 1;

	@Autowired
	private ExportController exportController;

	@Autowired
	private SortAttributeService sortAttributeService;

	private File exportDirectory;

	private SortAttributesDTO sortAttributesDTO;

	/**
	 * Persist the sortAttributes in the passed data table into the database.
	 *
	 * @param dataTable the sortAttribute data to persist
	 */
	@Given("^the following sortAttributes are saved in the database$")
	public void saveSortAttributes(final DataTable dataTable) {
		List<Map<String, String>> sortAttributeMaps = dataTable.asMaps(String.class, String.class);
		for (final Map<String, String> sortAttributeMap : sortAttributeMaps) {
			SortAttribute sortAttribute = new SortAttributeImpl();
			sortAttribute.setGuid(sortAttributeMap.get(SORT_ATTRIBUTE_GUID));
			sortAttribute.setBusinessObjectId(sortAttributeMap.get(BUSINESS_OBJECT_ID));
			sortAttribute.setSortAttributeGroup(SortAttributeGroup.valueOf(sortAttributeMap.get(SORT_ATTRIBUTE_GROUP)));
			sortAttribute.setStoreCode(sortAttributeMap.get(STORE_CODE));
			sortAttribute.setDescending(Boolean.getBoolean(sortAttributeMap.get(DESCENDING)));
			SortLocalizedName localizedName = new SortLocalizedNameImpl();
			localizedName.setLocaleCode(LOCALE_CODE);
			localizedName.setName(sortAttributeMap.get(DISPLAY_VALUE));
			sortAttribute.setLocalizedNames(ImmutableMap.of(LOCALE_CODE, localizedName));
			sortAttribute.setSortAttributeType(SortAttributeType.valueOf(sortAttributeMap.get(SORT_ATTRIBUTE_TYPE)));
			sortAttribute.setDefaultAttribute(Boolean.getBoolean(sortAttributeMap.get(DEFAULT_ATTRIBUTE)));
			sortAttributeService.saveOrUpdate(sortAttribute);
		}
	}

	/**
	 * Export the sortAttribute data from the database into an export folder.
	 *
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the sortAttributes in the database are exported using importexport$")
	public void exportSortAttributes() throws ConfigurationException {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.SORT_ATTRIBUTE))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Retrieve the sortAttribute data from the export folder into the sortAttributesDTO object in memory.
	 *
	 * @throws IOException if there was a problem reading or writing the sortAttribute data
	 */
	@When("^the exported sortAttributes are retrieved$")
	public void retrieveExportedSortAttributes() throws IOException {
		final File exportedSortAttributeFile = new File(exportDirectory, SORT_ATTRIBUTES_FILE);

		assertThat(exportedSortAttributeFile)
				.as(String.format("Exported sortAttribute file not found: %s", exportedSortAttributeFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedSortAttributeInputStream = new FileInputStream(exportedSortAttributeFile)) {
			final XMLUnmarshaller sortAttributeUnmarshaller = new XMLUnmarshaller(SortAttributesDTO.class);
			sortAttributesDTO = sortAttributeUnmarshaller.unmarshall(exportedSortAttributeInputStream);
		}
	}

	/**
	 * Verify that the sortAttributesDTO object in memory contains the passed sortAttribute guid.
	 *
	 * @param guid the sortAttribute guid to verify
	 */
	@Then("^the exported sortAttribute records contain a sortAttribute with guid ([\\w]+)$")
	public void verifySortAttributesDTOContainsGuid(final String guid) {
		SortAttributeDTO sortAttribute = sortAttributesDTO.getSortAttributes().stream()
				.filter(sortAttributeDTO -> sortAttributeDTO.getSortAttributeGuid().equals(guid))
				.findFirst()
				.orElse(null);

		assertThat(sortAttribute)
				.as(String.format("SortAttribute with guid %s not found", guid))
				.isNotNull();
	}
}
