/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

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

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.impl.ModifierGroupImpl;
import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFilterDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFiltersDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupsDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Export modifier group steps.
 */
public class ExportModifierGroupSteps {

	private static final String CODE = "code";
	private static final String MODIFIERS_FILE = "modifiergroups.xml";
	private static final String MODIFIER_FILTERS_FILE = "modifiergroupfilters.xml";
	private static final String MODIFIER_CODE = "modifiercode";
	private static final String TYPE = "type";
	private static final String REFERENCE_GUID = "reference";
	private static int runNumber = 1;

	@Autowired
	private ExportController exportController;

	@Autowired
	private ModifierService modifierService;

	private File exportDirectory;

	private ModifierGroupsDTO modifierGroupsDTO;
	private ModifierGroupFiltersDTO modifierGroupFiltersDTO;

	/**
	 * Persist the modifier groups in the passed data table into the database.
	 *
	 * @param dataTable the modifier group data to persist
	 */
	@Given("^the following modifier groups are saved in the database$")
	public void saveModifiers(final DataTable dataTable) {
		List<Map<String, String>> modiferMaps = dataTable.asMaps(String.class, String.class);
		for (Map<String, String> modifierMap : modiferMaps) {
			ModifierGroup modifierGroup = new ModifierGroupImpl();
			modifierGroup.setCode(modifierMap.get(CODE));
			modifierService.saveOrUpdate(modifierGroup);
		}
	}


	/**
	 * Persist the modifier groups in the passed data table into the database.
	 *
	 * @param dataTable the modifier group data to persist
	 */
	@Given("^the following modifier group filters are saved in the database$")
	public void saveModifierFilters(final DataTable dataTable) {
		List<Map<String, String>> modiferFilterMaps = dataTable.asMaps(String.class, String.class);
		for (Map<String, String> modifierFilterMap : modiferFilterMaps) {
			modifierService.addGroupFilter(modifierFilterMap.get(TYPE),
					modifierFilterMap.get(REFERENCE_GUID),
					modifierFilterMap.get(MODIFIER_CODE));
		}
	}


	/**
	 * Export the modifier data from the database into an export folder.
	 *
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the modifier groups in the database are exported using importexport$")
	public void exportModifierGroups() throws ConfigurationException {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.MODIFIERGROUP))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Export the modifier data from the database into an export folder.
	 *
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the modifier group Filters in the database are exported using importexport$")
	public void exportModifierGroupFilters() throws ConfigurationException {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.MODIFIERGROUPFILTER))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Retrieve the modifier data from the export folder into the modifierGroupsDTO object in memory.
	 * @throws IOException if there was a problem reading or writing the modifiergroup data
	 */
	@When("^the exported modifier groups are retrieved$")
	public void retrieveExportedModifierGroups() throws IOException {
		final File exportedFile = new File(exportDirectory, MODIFIERS_FILE);

		assertThat(exportedFile)
				.as(String.format("Exported modifier file not found: %s", exportedFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedInputStream = new FileInputStream(exportedFile)) {
			final XMLUnmarshaller unmarshaller = new XMLUnmarshaller(ModifierGroupsDTO.class);
			modifierGroupsDTO = unmarshaller.unmarshall(exportedInputStream);
		}
	}

	/**
	 * Retrieve the modifier data from the export folder into the modifierGroupsDTO object in memory.
	 * @throws IOException if there was a problem reading or writing the modifiergroup data
	 */
	@When("^the exported modifier group filters are retrieved$")
	public void retrieveExportedModifierGroupFilters() throws IOException {
		final File exportedFile = new File(exportDirectory, MODIFIER_FILTERS_FILE);

		assertThat(exportedFile)
				.as(String.format("Exported modifier filter file not found: %s", exportedFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedInputStream = new FileInputStream(exportedFile)) {
			final XMLUnmarshaller unmarshaller = new XMLUnmarshaller(ModifierGroupFiltersDTO.class);
			modifierGroupFiltersDTO = unmarshaller.unmarshall(exportedInputStream);
		}
	}


	/**
	 * Verify that the modifierGroupsDTO object in memory contains the passed code.
	 * @param code the code to verify
	 */
	@Then("^the exported modifier group records contain a modifier group with code ([\\w]+)$")
	public void verifyModifierGroupsDTOContainsGuid(final String code) {
		ModifierGroupDTO modifierGroupDTO = this.modifierGroupsDTO.getModifierGroups().stream()
				.filter(modifierGroup -> modifierGroup.getCode().equals(code))
				.findFirst()
				.orElse(null);

		assertThat(modifierGroupDTO)
				.as(String.format("Modifier Group with code%s not found", code))
				.isNotNull();
	}

	/**
	 * Verify that the modifierGroupsDTO object in memory contains the passed code.
	 * @param code the code to verify
	 */
	@Then("^the exported modifier group filter records contain a modifier group filter with modifier code ([\\w]+)$")
	public void verifyModifierGroupFiltersDTOContainsGuid(final String code) {
		ModifierGroupFilterDTO modifierGroupFilterDTO = this.modifierGroupFiltersDTO.getModifierGroupFilters().stream()
				.filter(modifierGroup -> modifierGroup.getModifierCode().equals(code))
				.findFirst()
				.orElse(null);

		assertThat(modifierGroupFilterDTO)
				.as(String.format("Modifier Group Filter with code%s not found", code))
				.isNotNull();
	}
}
