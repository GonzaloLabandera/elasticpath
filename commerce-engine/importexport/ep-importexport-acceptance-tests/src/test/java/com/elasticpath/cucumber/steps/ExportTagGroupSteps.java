/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cucumber.steps;

import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DESCRIPTION;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DICTIONARIES;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DISPLAY_NAME;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DISPLAY_NAME_LANGUAGE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_GROUP_CODE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_GUID;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_NAME;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_VALUE_TYPE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_DISPLAY_NAME;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_DISPLAY_NAME_LANGUAGE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_FILE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_GUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.apache.commons.lang.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.importexport.builder.ExportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.tag.TagDefinitionDTO;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.dto.tag.TagGroupsDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.marshalling.XMLUnmarshaller;
import com.elasticpath.importexport.common.types.RequiredJobType;
import com.elasticpath.importexport.exporter.configuration.ExportConfiguration;
import com.elasticpath.importexport.exporter.configuration.search.SearchConfiguration;
import com.elasticpath.importexport.exporter.controller.ExportController;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.domain.impl.TagDefinitionImpl;
import com.elasticpath.tags.domain.impl.TagGroupImpl;
import com.elasticpath.tags.service.TagDefinitionService;
import com.elasticpath.tags.service.TagDictionaryService;
import com.elasticpath.tags.service.TagGroupService;
import com.elasticpath.tags.service.TagValueTypeService;

/**
 * Export tag group steps.
 */
public class ExportTagGroupSteps {

	private static int runNumber = 1;

	@Autowired
	private ExportController exportController;

	@Autowired
	private TagGroupService tagGroupService;

	@Autowired
	private TagDefinitionService tagDefinitionService;

	@Autowired
	private TagValueTypeService tagValueTypeService;

	@Autowired
	private TagDictionaryService tagDictionaryService;

	private File exportDirectory;

	private TagGroupsDTO tagGroupsDTO;

	private List<String> importedTagGroupGuids;
	private Map<String, String> importedTagDefinitionMap;

	/**
	 * Persist the tag groups in the passed data table into the database.
	 *
	 * @param dataTable the tag group data to persist
	 */
	@Given("^the following tag groups are saved in the database$")
	public void saveTagGroups(final DataTable dataTable) {
		importedTagGroupGuids = new ArrayList<>();
		List<Map<String, String>> tagGroupMaps = dataTable.asMaps(String.class, String.class);
		for (final Map<String, String> tagGroupAttrs : tagGroupMaps) {
			TagGroup tagGroup = new TagGroupImpl();
			tagGroup.setGuid(tagGroupAttrs.get(TAG_GROUP_GUID));
			String language = tagGroupAttrs.get(TAG_GROUP_DISPLAY_NAME_LANGUAGE);
			String displayName = tagGroupAttrs.get(TAG_GROUP_DISPLAY_NAME);
			Locale displayNameLocale = LocaleUtils.toLocale(language);
			tagGroup.getLocalizedProperties().setValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, displayNameLocale, displayName);
			tagGroupService.saveOrUpdate(tagGroup);
			importedTagGroupGuids.add(tagGroup.getGuid());
		}
	}

	/**
	 * Persist the tag definitions in the passed data table into the database.
	 *
	 * @param dataTable the tag definition data to persist
	 */
	@Given("^the following tag definitions are saved in the database$")
	public void saveTagDefinitions(final DataTable dataTable) {
		importedTagDefinitionMap = new HashMap<>();
		List<Map<String, String>> tagDefinitionMaps = dataTable.asMaps(String.class, String.class);
		for (final Map<String, String> tagDefAttrs : tagDefinitionMaps) {
			TagDefinition tagDefinition = new TagDefinitionImpl();
			tagDefinition.setGuid(tagDefAttrs.get(TAG_DEFINITION_GUID));
			tagDefinition.setName(tagDefAttrs.get(TAG_DEFINITION_NAME));
			tagDefinition.setDescription(tagDefAttrs.get(TAG_DEFINITION_DESCRIPTION));
			String language = tagDefAttrs.get(TAG_DEFINITION_DISPLAY_NAME_LANGUAGE);
			String displayName = tagDefAttrs.get(TAG_DEFINITION_DISPLAY_NAME);
			Locale displayNameLocale = LocaleUtils.toLocale(language);
			tagDefinition.getLocalizedProperties().setValue(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, displayNameLocale, displayName);
			TagValueType valueType = tagValueTypeService.findByGuid(tagDefAttrs.get(TAG_DEFINITION_VALUE_TYPE));
			tagDefinition.setValueType(valueType);
			TagGroup tagGroup = tagGroupService.findByGuid(tagDefAttrs.get(TAG_DEFINITION_GROUP_CODE));
			tagDefinition.setGroup(tagGroup);
			tagDefinitionService.saveOrUpdate(tagDefinition);
			tagGroup.addTagDefinition(tagDefinition);
			tagGroupService.saveOrUpdate(tagGroup);
			String[] dictionaryGuids = tagDefAttrs.get(TAG_DEFINITION_DICTIONARIES).split(",");
			for (String dictionaryGuid : dictionaryGuids) {
				TagDictionary dictionary = tagDictionaryService.findByGuid(dictionaryGuid);
				dictionary.addTagDefinition(tagDefinition);
				tagDictionaryService.saveOrUpdate(dictionary);
			}
			importedTagDefinitionMap.put(tagDefinition.getGuid(), tagDefinition.getGroup().getGuid());
		}
	}

	/**
	 * Export the tag group data from the database into an export folder.
	 *
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the tags in the database are exported using importexport$")
	public void exportTagGroups() throws ConfigurationException {
		exportDirectory =
				ImportExportTestDirectoryBuilder.newInstance()
						.withTestName(this.getClass().getSimpleName())
						.withRunNumber(runNumber++)
						.build();

		final ExportConfiguration exportConfiguration =
				ExportConfigurationBuilder.newInstance()
						.setDeliveryTarget(exportDirectory.getPath())
						.setExporterTypes(Collections.singletonList(RequiredJobType.TAGGROUP))
						.build();

		final SearchConfiguration searchConfiguration = new SearchConfiguration();

		exportController.loadConfiguration(exportConfiguration, searchConfiguration);
		exportController.executeExport();
	}

	/**
	 * Retrieve the tag group data from the export folder into the tagGroupDTO object in memory.
	 *
	 * @throws IOException if there was a problem reading or writing the tag group data
	 */
	@When("^the exported tags are retrieved$")
	public void retrieveExportedTagGroups() throws IOException {
		final File exportedTagGroupFile = new File(exportDirectory, TAG_GROUP_FILE);

		assertThat(exportedTagGroupFile)
				.as(String.format("Exported tag group file not found: %s", exportedTagGroupFile.getAbsolutePath()))
				.exists();

		try (final FileInputStream exportedTagGroupInputStream = new FileInputStream(exportedTagGroupFile)) {
			final XMLUnmarshaller tagGroupUnmarshaller = new XMLUnmarshaller(TagGroupsDTO.class);
			tagGroupsDTO = tagGroupUnmarshaller.unmarshall(exportedTagGroupInputStream);
		}
	}

	/**
	 * Verify that the export tagGroupsDTO include all of the imported tag group and tag def codes.
	 */
	@Then("^the exported tag records contain all tag groups and definitions$")
	public void verifyAllTagGroupsAndDefinitions() {
		verifyTagGroups();

		verifyTagDefinitions();
	}

	private void verifyTagDefinitions() {
		for (String tagDefinitionGuid : importedTagDefinitionMap.keySet()) {
			TagGroupDTO tagGroupDTO = tagGroupsDTO.getByTagGroupGuid(importedTagDefinitionMap.get(tagDefinitionGuid));

			assertThat(tagGroupDTO)
					.as(String.format("Could not find the tag group associated with tag definition with guid %s", tagDefinitionGuid))
					.isNotNull();

			TagDefinitionDTO tagDefDTO = tagGroupDTO.getTags().stream()
							.filter(tagDefinitionDTO -> tagDefinitionDTO.getCode().equals(tagDefinitionGuid))
							.findFirst()
							.orElse(null);

			assertThat(tagDefDTO)
					.as(String.format("TagDefinition with guid %s not found", tagDefinitionGuid))
					.isNotNull();
		}
	}

	private void verifyTagGroups() {
		for (String guid : importedTagGroupGuids) {
			TagGroupDTO tagGroupDTO = tagGroupsDTO.getByTagGroupGuid(guid);

			assertThat(tagGroupDTO)
					.as(String.format("TagGroup with guid %s not found", guid))
					.isNotNull();
		}
	}
}
