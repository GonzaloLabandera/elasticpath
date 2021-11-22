/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cucumber.steps;

import static com.elasticpath.commons.util.TestDomainMarshaller.marshalObject;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.MANIFEST_IMPORT_FILE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DESCRIPTION;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DICTIONARIES;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DISPLAY_NAME;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_DISPLAY_NAME_LANGUAGE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_GUID;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_NAME;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_DEFINITION_VALUE_TYPE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_DISPLAY_NAME;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_DISPLAY_NAME_LANGUAGE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_FILE;
import static com.elasticpath.importexport.common.dto.tag.TagGroupDTOConstants.TAG_GROUP_GUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.importexport.builder.ImportConfigurationBuilder;
import com.elasticpath.importexport.builder.ImportExportTestDirectoryBuilder;
import com.elasticpath.importexport.common.dto.tag.TagDefinitionDTO;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.dto.tag.TagGroupsDTO;
import com.elasticpath.importexport.common.exception.ConfigurationException;
import com.elasticpath.importexport.common.manifest.Manifest;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.importer.configuration.ImportConfiguration;
import com.elasticpath.importexport.importer.controller.ImportController;
import com.elasticpath.importexport.importer.types.ImportStrategyType;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.service.TagDictionaryService;
import com.elasticpath.tags.service.TagGroupService;

/**
 * Import TagGroup steps.
 */
public class ImportTagGroupSteps {

	private static int runNumber = 1;

	@Autowired
	private ImportController importController;

	@Autowired
	private TagGroupService tagGroupService;

	@Autowired
	private TagDictionaryService tagDictionaryService;

	private TagGroupsDTO tagGroupsDTO;

	/**
	 * Reset the tag group DTO object in memory.
	 */
	@Given("^the tag group import data has been emptied out$")
	public void clearTagGroupsImportData() {
		tagGroupsDTO = new TagGroupsDTO();
	}

	/**
	 * Import tag group from the passed data table into the tagGroup DTO object and trigger an import.
	 *
	 * @param dataTable the data table containing the tag group/definition/dictionary data
	 * @throws IOException            if there was a problem reading or writing the tag group data
	 * @throws ConfigurationException if there was a problem with the import/export configuration
	 */
	@When("^the following tags are imported using importexport$")
	public void importTagGroups(final DataTable dataTable) throws IOException, ConfigurationException {
		tagGroupsDTO.getTagGroups().addAll(buildTagGroupDTOsFromDataTable(dataTable));

		File importDirectory = ImportExportTestDirectoryBuilder.newInstance()
				.withTestName(this.getClass().getSimpleName())
				.withRunNumber(runNumber++)
				.build();

		if (!importDirectory.exists()) {
			importDirectory.mkdirs();
		}

		Manifest manifest = new Manifest();
		manifest.addResource(TAG_GROUP_FILE);

		marshalObject(Manifest.class, manifest, new File(importDirectory, MANIFEST_IMPORT_FILE));
		marshalObject(TagGroupsDTO.class, tagGroupsDTO, new File(importDirectory, TAG_GROUP_FILE));

		ImportConfiguration importConfiguration = ImportConfigurationBuilder.newInstance()
				.setRetrievalSource(importDirectory.getPath())
				.addImporterConfiguration(JobType.TAGGROUP, ImportStrategyType.INSERT_OR_UPDATE)
				.build();

		importController.loadConfiguration(importConfiguration);
		importController.executeImport();
	}

	/**
	 * Verify that all imported tags exist in the database.
	 */
	@Then("^all tags are persisted")
	public void verifyTagsArePersisted() {
		for (TagGroupDTO tagGroupDTO : tagGroupsDTO.getTagGroups()) {
			final TagGroup tagGroup = verifyTagGroup(tagGroupDTO);

			verifyTagDefinition(tagGroupDTO, tagGroup);
		}
	}

	private List<TagGroupDTO> buildTagGroupDTOsFromDataTable(final DataTable dataTable) throws IOException {
		final List<Map<String, String>> tagGroupMaps = dataTable.asMaps(String.class, String.class);
		final List<TagGroupDTO> tagGroupDTOS = new ArrayList<>();
		for (Map<String, String> tagGroupAttrs : tagGroupMaps) {
			final TagGroupDTO tagGroupDTO = new TagGroupDTO();
			tagGroupDTO.setCode(tagGroupAttrs.get(TAG_GROUP_GUID));
			final String language = tagGroupAttrs.get(TAG_GROUP_DISPLAY_NAME_LANGUAGE);
			final String displayName = tagGroupAttrs.get(TAG_GROUP_DISPLAY_NAME);
			tagGroupDTO.setNameValues(Collections.singletonList(new DisplayValue(language, displayName)));

			final TagDefinitionDTO tagDefinitionDTO = new TagDefinitionDTO();
			tagDefinitionDTO.setCode(tagGroupAttrs.get(TAG_DEFINITION_GUID));
			tagDefinitionDTO.setName(tagGroupAttrs.get(TAG_DEFINITION_NAME));
			final DisplayValue displayValue = new DisplayValue(tagGroupAttrs.get(TAG_DEFINITION_DISPLAY_NAME_LANGUAGE),
					tagGroupAttrs.get(TAG_DEFINITION_DISPLAY_NAME));
			tagDefinitionDTO.setNameValues(Collections.singletonList(displayValue));
			tagDefinitionDTO.setDescription(tagGroupAttrs.get(TAG_DEFINITION_DESCRIPTION));
			final String[] dictionaries = tagGroupAttrs.get(TAG_DEFINITION_DICTIONARIES).split(",");
			final List<String> dictList = Stream.of(dictionaries).collect(Collectors.toList());
			tagDefinitionDTO.setDictionaries(dictList);
			tagDefinitionDTO.setType(tagGroupAttrs.get(TAG_DEFINITION_VALUE_TYPE));
			tagGroupDTO.setTags(Collections.singleton(tagDefinitionDTO));

			tagGroupDTOS.add(tagGroupDTO);
		}
		return tagGroupDTOS;
	}

	private void verifyTagDefinition(final TagGroupDTO tagGroupDTO, final TagGroup tagGroup) {
		for (TagDefinitionDTO tagDefinitionDTO : tagGroupDTO.getTags()) {
			final TagDefinition tagDefDO = tagGroup.getTagDefinitions().stream()
					.filter(tagDefinition -> tagDefinition.getGuid().equals(tagDefinitionDTO.getCode()))
					.findFirst()
					.orElse(null);
			assertThat(tagDefDO)
					.as(String.format("Tag Definition with guid %s not found in group %s", tagDefinitionDTO.getCode(), tagGroupDTO.getCode()))
					.isNotNull();
			assertThat(tagDefDO.getName())
					.as(String.format("Tag Definition name does not match for guid %s", tagDefinitionDTO.getCode()))
					.isEqualTo(tagDefinitionDTO.getName());
			assertThat(tagDefDO.getDescription())
					.as(String.format("Tag Definition description does not match for guid %s", tagDefinitionDTO.getCode()))
					.isEqualTo(tagDefinitionDTO.getDescription());
			if (!tagDefinitionDTO.getNameValues().isEmpty()) {
				final DisplayValue displayValue = tagDefinitionDTO.getNameValues().get(0);
				final Locale locale = LocaleUtils.toLocale(displayValue.getLanguage());
				final String displayName = displayValue.getValue();
				assertThat(tagDefDO.getLocalizedName(locale))
						.as(String.format("Tag Definition display name does not match import for guid %s", tagDefinitionDTO.getCode()))
						.isEqualTo(displayName);
			}
			assertThat(tagDefDO.getValueType().getGuid())
					.as(String.format("Tag Definition value type does not match import for guid %s", tagDefinitionDTO.getCode()))
					.isEqualTo(tagDefinitionDTO.getType());
			verifyDictionaries(tagDefinitionDTO);
		}
	}

	private void verifyDictionaries(final TagDefinitionDTO tagDefinitionDTO) {
		if (tagDefinitionDTO.getDictionaries() != null && !tagDefinitionDTO.getDictionaries().isEmpty()) {
			for (String dictionaryGuid : tagDefinitionDTO.getDictionaries()) {
				final TagDictionary dictionary = tagDictionaryService.findByGuid(dictionaryGuid);
				boolean foundIt = false;
				for (TagDefinition tagDefFromDictionary : dictionary.getTagDefinitions()) {
					if (tagDefFromDictionary.getGuid().equals(tagDefinitionDTO.getCode())) {
						foundIt = true;
						break;
					}
				}
				assertThat(foundIt)
					.as(String.format("Tag Definition %s is not in dictionary %s", tagDefinitionDTO.getCode(),
							dictionaryGuid))
					.isTrue();
			}
		}
	}

	private TagGroup verifyTagGroup(final TagGroupDTO tagGroupDTO) {
		final TagGroup tagGroup = tagGroupService.findByGuid(tagGroupDTO.getCode());
		assertThat(tagGroup)
				.as(String.format("TagGroup with guid %s not found", tagGroupDTO.getCode()))
				.isNotNull();

		for (DisplayValue displayValueDTO : tagGroupDTO.getNameValues()) {
			final Locale locale = LocaleUtils.toLocale(displayValueDTO.getLanguage());
			assertThat(tagGroup.getLocalizedProperties().getValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, locale))
					.as(String.format("Tag Group display name does not match import for guid %s", tagGroupDTO.getCode()))
					.isEqualTo(displayValueDTO.getValue());
		}
		return tagGroup;
	}
}