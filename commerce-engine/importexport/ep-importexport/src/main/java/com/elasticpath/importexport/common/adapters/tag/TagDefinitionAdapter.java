/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.adapters.tag;

import static com.elasticpath.importexport.common.comparators.ExportComparators.DISPLAY_VALUE_COMPARATOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.tag.TagDefinitionDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.service.TagDictionaryService;
import com.elasticpath.tags.service.TagValueTypeService;

/**
 * Adapter used to transform <code>TagDefinition</code>
 * to  <code>TagDefinitionDTO</code> and vice versa.
 */
public class TagDefinitionAdapter extends AbstractDomainAdapterImpl<TagDefinition, TagDefinitionDTO> {

	/**
	 * The error code thrown when a required field is missing during import.
	 */
	public static final String IE_10904_MISSING_REQUIRED_FIELD = "IE-10904";

	/**
	 * The error code thrown when a specified tag value type does not exist during import.
	 */
	public static final String IE_10905_INVALID_VALUE_TYPE = "IE-10905";

	/**
	 * The error code thrown when a specified locale does not exist during import.
	 */
	public static final String IE_10902_INVALID_LOCALE = "IE-10902";

	/**
	 * The error code thrown when a specified tag dictionary does not exist during import.
	 */
	public static final String IE_10906_INVALID_TAG_DICTIONARY = "IE-10906";

	private TagDictionaryService tagDictionaryService;

	private TagValueTypeService tagValueTypeService;
	private List<TagDictionary> allTagDictionaries;
	private List<TagValueType> allTagValueTypes;

	@Override
	public TagDefinition createDomainObject() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class);
	}

	@Override
	public TagDefinitionDTO createDtoObject() {
		return new TagDefinitionDTO();
	}

	/**
	 * {@inheritDoc} Populates the DTO from the domain object for export.
	 */
	@Override
	public void populateDTO(final TagDefinition source, final TagDefinitionDTO target) {
		target.setCode(source.getGuid());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		target.setType(source.getValueType().getGuid());
		populateDTODictionaries(source, target);
		populateDTONameValues(source, target);
	}

	private void populateDTODictionaries(final TagDefinition source, final TagDefinitionDTO target) {
		List<TagDictionary> tagDictionaries = getAllTagDictionaries();
		List<String> dictionaryDTOS = new ArrayList<>(tagDictionaries.size());
		for (TagDictionary tagDictionary : tagDictionaries) {
			for (TagDefinition tagDefinition : tagDictionary.getTagDefinitions()) {
				if (tagDefinition.getGuid().equals(source.getGuid())) {
					dictionaryDTOS.add(tagDictionary.getGuid());
				}
			}
		}
		target.setDictionaries(dictionaryDTOS);
	}

	/**
	 * {@inheritDoc} Populates the domain object from the DTO for import.
	 */
	@Override
	public void populateDomain(final TagDefinitionDTO source, final TagDefinition target) {
		if (StringUtils.isEmpty(source.getCode())) {
			throw new PopulationRollbackException(IE_10904_MISSING_REQUIRED_FIELD, "code");
		}
		if (StringUtils.isEmpty(source.getName())) {
			throw new PopulationRollbackException(IE_10904_MISSING_REQUIRED_FIELD, "name");
		}
		if (StringUtils.isEmpty(source.getType())) {
			throw new PopulationRollbackException(IE_10904_MISSING_REQUIRED_FIELD, "type");
		}

		target.setGuid(source.getCode());
		target.setName(source.getName());
		target.setDescription(source.getDescription());
		populateDomainDisplayNames(source, target);

		Optional<TagValueType> tagValueType = findTagValueType(source);
		if (!tagValueType.isPresent()) {
			throw new PopulationRollbackException(IE_10905_INVALID_VALUE_TYPE, source.getCode(), source.getType());
		}
		tagValueType.ifPresent(target::setValueType);

		populateDomainTagDictionaries(source, target);
	}

	private Optional<TagValueType> findTagValueType(final TagDefinitionDTO source) {
		return getAllTagValueTypes().stream().filter(tagValueType -> tagValueType.getGuid().equals(source.getType())).findFirst();
	}

	private List<TagValueType> getAllTagValueTypes() {
		if (allTagValueTypes == null) {
			allTagValueTypes = tagValueTypeService.getTagValueTypes();
		}

		return allTagValueTypes;
	}

	private void populateDomainDisplayNames(final TagDefinitionDTO source, final TagDefinition target) {
		LocalizedProperties localizedProperties = target.getLocalizedProperties();
		for (DisplayValue displayValue : source.getNameValues()) {
			if (StringUtils.isEmpty(displayValue.getValue())) {
				throw new PopulationRollbackException(IE_10904_MISSING_REQUIRED_FIELD, "displayName");
			}
			try {
				Locale locale = LocaleUtils.toLocale(displayValue.getLanguage());
				localizedProperties.setValue(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, locale, displayValue.getValue());
			} catch (IllegalArgumentException exception) {
				throw new PopulationRollbackException(IE_10902_INVALID_LOCALE, exception, displayValue.getLanguage(), displayValue.getValue());
			}
		}
	}

	private void populateDomainTagDictionaries(final TagDefinitionDTO source, final TagDefinition target) {
		for (String dictionaryGuid : source.getDictionaries()) {
			Optional<TagDictionary> tagDictionaryTarget = findTagDictionary(dictionaryGuid);
			if (!tagDictionaryTarget.isPresent()) {
				throw new PopulationRollbackException(IE_10906_INVALID_TAG_DICTIONARY, source.getCode(), dictionaryGuid);
			}
			tagDictionaryTarget.ifPresent(tagDictionary -> tagDictionary.addTagDefinition(target));
		}
	}

	private Optional<TagDictionary> findTagDictionary(final String dictionaryGuid) {
		return getAllTagDictionaries().stream().filter(tagDictionary -> tagDictionary.getGuid().equals(dictionaryGuid)).findFirst();
	}

	private List<TagDictionary> getAllTagDictionaries() {
		if (allTagDictionaries == null) {
			allTagDictionaries = tagDictionaryService.getTagDictionaries();
		}
		return allTagDictionaries;
	}

	private void populateDTONameValues(final TagDefinition source, final TagDefinitionDTO target) {
		final LocalizedProperties displayNames = source.getLocalizedProperties();
		Set<String> allLanguageKeys = getAllLanguageKeysFromLocalizedProperties(displayNames);
		final List<DisplayValue> nameValues = new ArrayList<>(allLanguageKeys.size());
		for (String langKey : allLanguageKeys) {
			DisplayValue displayValue = new DisplayValue();
			Locale locale = displayNames.getLocaleFromKey(langKey);
			displayValue.setLanguage(locale.getLanguage());
			displayValue.setValue(displayNames.getValueWithoutFallBack(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, locale));
			nameValues.add(displayValue);
		}
		nameValues.sort(DISPLAY_VALUE_COMPARATOR);

		target.setNameValues(nameValues);
	}

	private Set<String> getAllLanguageKeysFromLocalizedProperties(final LocalizedProperties localProps) {
		Set<String> localPropKeys = localProps.getLocalizedPropertiesMap().keySet();
		return localPropKeys.stream()
				.filter(propKey -> TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME.equals(localProps.getPropertyNameFromKey(propKey)))
				.collect(Collectors.toSet());
	}

	public void setTagDictionaryService(final TagDictionaryService tagDictionaryService) {
		this.tagDictionaryService = tagDictionaryService;
	}

	public void setTagValueTypeService(final TagValueTypeService tagValueTypeService) {
		this.tagValueTypeService = tagValueTypeService;
	}

}