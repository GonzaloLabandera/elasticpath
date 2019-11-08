/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.adapters.tag;

import static com.elasticpath.importexport.common.comparators.ExportComparators.DISPLAY_VALUE_COMPARATOR;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.importexport.common.adapters.AbstractDomainAdapterImpl;
import com.elasticpath.importexport.common.dto.tag.TagDefinitionDTO;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagGroup;

/**
 * Adapter used to transform <code>TagGroup</code>
 * to  <code>TagGroupDTO</code> and vice versa.
 */
public class TagGroupAdapter extends AbstractDomainAdapterImpl<TagGroup, TagGroupDTO> {

	/**
	 * The error code thrown when the tag group code is not specified during import.
	 */
	public static final String IE_10901_EMPTY_GROUP_CODE_ERROR = "IE-10901";

	/**
	 * The error code thrown when the tag group display name is not specified during import.
	 */
	public static final String IE_10903_EMPTY_DISPLAY_VALUE_ERROR = "IE-10903";

	/**
	 * The error code thrown when the tag group display name locale is not valid during import.
	 */
	public static final String IE_10902_INVALID_LOCALE_ERROR = "IE-10902";

	private TagDefinitionAdapter tagDefinitionAdapter;

	@Override
	public TagGroup createDomainObject() {
		return getBeanFactory().getPrototypeBean(ContextIdNames.TAG_GROUP, TagGroup.class);
	}

	@Override
	public TagGroupDTO createDtoObject() {
		return new TagGroupDTO();
	}

	/**
	 * {@inheritDoc} Populates the DTO from the domain object for export.
	 */
	@Override
	public void populateDTO(final TagGroup source, final TagGroupDTO target) {
		target.setCode(source.getGuid());
		populateDtoNameValues(source, target);
		populateDtoTagDefinitionList(source, target);
	}

	/**
	 * {@inheritDoc} Populates the domain object from the DTO for import.
	 */
	@Override
	public void populateDomain(final TagGroupDTO source, final TagGroup target) {
		if (StringUtils.isEmpty(source.getCode())) {
			throw new PopulationRollbackException(IE_10901_EMPTY_GROUP_CODE_ERROR);
		}
		target.setGuid(source.getCode());
		populateDomainNameValues(source, target);
		populateDomainTagDefinitions(source, target);

	}

	private void populateDomainTagDefinitions(final TagGroupDTO sourceDTO, final TagGroup targetDomain) {
		for (TagDefinitionDTO tagDefinitionDTO : sourceDTO.getTags()) {
			TagDefinition tagDefinition = tagDefinitionAdapter.createDomainObject();
			tagDefinitionAdapter.populateDomain(tagDefinitionDTO, tagDefinition);
			targetDomain.addTagDefinition(tagDefinition);
		}
	}

	private void populateDomainNameValues(final TagGroupDTO sourceDTO, final TagGroup targetDomain) {
		LocalizedProperties localizedProperties = targetDomain.getLocalizedProperties();
		for (DisplayValue displayValue : sourceDTO.getNameValues()) {
			if (StringUtils.isEmpty(displayValue.getValue())) {
				throw new PopulationRollbackException(IE_10903_EMPTY_DISPLAY_VALUE_ERROR);
			}
			try {
				Locale locale = LocaleUtils.toLocale(displayValue.getLanguage());
				localizedProperties.setValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, locale, displayValue.getValue());
			} catch (IllegalArgumentException exception) {
				throw new PopulationRollbackException(IE_10902_INVALID_LOCALE_ERROR, exception, displayValue.getLanguage(), displayValue.getValue());
			}
		}

	}

	private void populateDtoTagDefinitionList(final TagGroup source, final TagGroupDTO tagGroupDTO) {
		Set<TagDefinition> tagDefinitionSet = source.getTagDefinitions();
		Set<TagDefinitionDTO> tagDefinitionDTOs = new HashSet<>(tagDefinitionSet.size());
		for (TagDefinition entry : tagDefinitionSet) {
			TagDefinitionDTO tagDefinitionDTO = new TagDefinitionDTO();
			tagDefinitionAdapter.populateDTO(entry, tagDefinitionDTO);
			tagDefinitionDTOs.add(tagDefinitionDTO);
		}
		tagGroupDTO.setTags(tagDefinitionDTOs);
	}

	private void populateDtoNameValues(final TagGroup source, final TagGroupDTO target) {
		final LocalizedProperties displayNames = source.getLocalizedProperties();
		Set<String> allLanguageKeys = getAllLanguageKeysFromLocalizedProperties(displayNames);
		final List<DisplayValue> nameValues = new ArrayList<>(allLanguageKeys.size());
		for (String langKey : allLanguageKeys) {
			DisplayValue displayValue = new DisplayValue();
			Locale locale = displayNames.getLocaleFromKey(langKey);
			displayValue.setLanguage(locale.getLanguage());
			displayValue.setValue(displayNames.getValueWithoutFallBack(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, locale));
			nameValues.add(displayValue);
		}
		nameValues.sort(DISPLAY_VALUE_COMPARATOR);
		target.setNameValues(nameValues);
	}

	private Set<String> getAllLanguageKeysFromLocalizedProperties(final LocalizedProperties localProps) {
		Set<String> localPropKeys = localProps.getLocalizedPropertiesMap().keySet();
		return localPropKeys.stream()
				.filter(propKey -> TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME.equals(localProps.getPropertyNameFromKey(propKey)))
				.collect(Collectors.toSet());
	}

	public void setTagDefinitionAdapter(final TagDefinitionAdapter tagDefinitionAdapter) {
		this.tagDefinitionAdapter = tagDefinitionAdapter;
	}

}

