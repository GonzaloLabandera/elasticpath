/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.cucumber.steps;

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
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.modifier.ModifierGroupFilter;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.modifier.ModifierFieldDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFilterDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupFiltersDTO;
import com.elasticpath.importexport.common.dto.modifier.ModifierGroupsDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Import ModifierGroups steps.
 */
public class ImportModifierGroupSteps {

	private static final String MODIFIER_GRPOUPS_FILE = "modifiergroups.xml";
	private static final String MODIFIER_GROUPFILTERS_FILE = "modifiergroupfilters.xml";
	private static final String MANIFEST_IMPORT_FILE = "manifest.xml";
	private static final String CODE = "code";
	private static final String MODIFIER_CODE = "modifierCode";
	private static final String TYPE = "type";
	private static final String REFERENCE_GUID = "referenceGuid";
	private static final String FIELD_CODE = "fieldCode";
	private static final String FIELD_TYPE = "fieldType";
	private static final String MAX_SIZE = "maxSize";
	private static final String REQUIRED = "required";
	private static final String FIELD_DISPLAY_NAME = "fieldDisplayName";
	private static final String DISPLAY_NAME = "displayName";
	private static int runNumber = 1;

	@Autowired
	private ImportController importController;

	@Autowired
	private ModifierService modifierService;

	private ModifierGroupsDTO modifierGroupsDTO = new ModifierGroupsDTO();
	private ModifierGroupFiltersDTO modifierGroupFiltersDTO = new ModifierGroupFiltersDTO();

	/**
	 * Reset the modifier groups DTO object in memory.
	 */
	@Given("^the modifier groups import data has been emptied out$")
	public void clearModifierGroupsImportData() {
		modifierGroupsDTO = new ModifierGroupsDTO();
	}


	/**
	 * Reset the modifier groupfilters DTO object in memory.
	 */
	@Given("^the modifier group filters import data has been emptied out$")
	public void clearModifierGroupFiltersImportData() {
		modifierGroupFiltersDTO = new ModifierGroupFiltersDTO();
	}

	/**
	 * Import modifier groups from the passed data table into the modifierGroup DTO object and trigger an import.
	 *
	 * @param dataTable the data table containing the modifierGroup data
	 * @throws IOException            if there was a problem reading or writing the modifierGroup data
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the following modifier groups are imported using importexport$")
	public void importModifierGroups(final DataTable dataTable) throws IOException, ConfigurationException {
		modifierGroupsDTO.getModifierGroups().addAll(buildModifierGroupDTOsFromDataTable(dataTable));

		File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		Manifest manifest = new Manifest();
		manifest.addResource(MODIFIER_GRPOUPS_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(ModifierGroupsDTO.class, modifierGroupsDTO, new File(importDirectory, MODIFIER_GRPOUPS_FILE));

		ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.MODIFIERGROUP, ImportStrategyType.INSERT_OR_UPDATE)
				.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}

	private List<ModifierGroupDTO> buildModifierGroupDTOsFromDataTable(final DataTable dataTable) throws IOException {
		List<Map<String, String>> modifierGroupMaps = dataTable.asMaps(String.class, String.class);
		List<ModifierGroupDTO> modifierGroupDTOs = new ArrayList<>();
		for (Map<String, String> modifierGroupMap : modifierGroupMaps) {
			ModifierGroupDTO modifierGroupDTO = new ModifierGroupDTO();
			modifierGroupDTO.setCode(modifierGroupMap.get(CODE));
			ModifierFieldDTO modifierField = new ModifierFieldDTO();
			modifierField.setCode(modifierGroupMap.get(FIELD_CODE));
			modifierField.setType(modifierGroupMap.get(FIELD_TYPE));
			modifierField.setMaxSize(Integer.parseInt(modifierGroupMap.get(MAX_SIZE)));
			modifierField.setRequired(Boolean.parseBoolean(modifierGroupMap.get(REQUIRED)));
			modifierField.setValues(getDisplayValues(modifierGroupMap.get(FIELD_DISPLAY_NAME)));
			List<ModifierFieldDTO> modifierFields = Collections.singletonList(modifierField);
			modifierGroupDTO.setModifierFields(modifierFields);

			modifierGroupDTO.setValues(getDisplayValues(modifierGroupMap.get(DISPLAY_NAME)));
			modifierGroupDTOs.add(modifierGroupDTO);
		}
		return modifierGroupDTOs;
	}

	private List<DisplayValue> getDisplayValues(final String displayName) {
		DisplayValue displayValue = new DisplayValue();
		displayValue.setLanguage("en");
		displayValue.setValue(displayName);
		return Collections.singletonList(displayValue);
	}


	/**
	 * Import modifier group filters from the passed data table into the modifierfilterGroup DTO object and trigger an import.
	 *
	 * @param dataTable the data table containing the modifier filter Group data
	 * @throws IOException            if there was a problem reading or writing the modifierGroup data
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the following modifier group filters are imported using importexport$")
	public void importModifierFilterGroups(final DataTable dataTable) throws IOException, ConfigurationException {
		modifierGroupFiltersDTO.getModifierGroupFilters().addAll(buildModifierGroupFilterDTOsFromDataTable(dataTable));

		File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		Manifest manifest = new Manifest();
		manifest.addResource(MODIFIER_GROUPFILTERS_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(ModifierGroupFiltersDTO.class, modifierGroupFiltersDTO,
				new File(importDirectory, MODIFIER_GROUPFILTERS_FILE));

		ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.MODIFIERGROUPFILTER, ImportStrategyType.INSERT_OR_UPDATE)
				.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}


	private List<ModifierGroupFilterDTO> buildModifierGroupFilterDTOsFromDataTable(final DataTable dataTable) {
		List<Map<String, String>> modifierGroupFiltersMaps = dataTable.asMaps(String.class, String.class);
		List<ModifierGroupFilterDTO> modifierGroupFilterDTOs = new ArrayList<>();

		for (Map<String, String> modifierGroupMap : modifierGroupFiltersMaps) {
			ModifierGroupFilterDTO modifierGroupFilterDTO = new ModifierGroupFilterDTO();
			modifierGroupFilterDTO.setModifierCode(modifierGroupMap.get(MODIFIER_CODE));
			modifierGroupFilterDTO.setReferenceGuid(modifierGroupMap.get(REFERENCE_GUID));
			modifierGroupFilterDTO.setType(modifierGroupMap.get(TYPE));

			modifierGroupFilterDTOs.add(modifierGroupFilterDTO);
		}
		return modifierGroupFilterDTOs;
	}

	/**
	 * Verify that the database contains a modifierGroup with the passed code.
	 *
	 * @param code the modifierGroup code to verify
	 */
	@Then("^the modifier group with code ([\\w]+) is persisted")
	public void verifyModifierGroupIsPersisted(final String code) {
		ModifierGroup modifierGroup = modifierService.findModifierGroupByCode(code);
		assertThat(modifierGroup)
				.as(String.format("ModifierGroup with code %s not found", code))
				.isNotNull();
	}

	/**
	 * Verify that the database contains a modifierGroupFilter with the passed code, guid and type.
	 * @param modifierCode the modifier code.
	 * @param referenceGuid the reference guid.
	 * @param type the type.
	 */
	@Then("^the modifier group filter with modifier code ([\\w]+) and reference guid ([\\w]+) and type ([\\w]+) is persisted")
	public void verifyModifierGroupFilterIsPersisted(final String modifierCode, final String referenceGuid, final String type) {
		ModifierGroupFilter modifierGroupFilter = modifierService.findModifierGroupFilter(referenceGuid, modifierCode, type);
		assertThat(modifierGroupFilter)
				.as(String.format("ModifierGroupFilter with code %s not found", modifierCode))
				.isNotNull();
	}
}
