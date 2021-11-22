/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.adapters.tags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.TagDefinitionLocalizedPropertyValueImpl;
import com.elasticpath.importexport.common.adapters.tag.TagDefinitionAdapter;
import com.elasticpath.importexport.common.dto.tag.TagDefinitionDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.tags.domain.TagDefinition;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.domain.TagValueType;
import com.elasticpath.tags.domain.impl.TagDefinitionImpl;
import com.elasticpath.tags.service.TagDictionaryService;
import com.elasticpath.tags.service.TagValueTypeService;

/**
 * Tests population of DTO and domain objects by <code>TagDefinitionAdapter</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagDefinitionAdapterTest {

	private static final String LANGUAGE = "en";
	private static final String INVALID_LANGUAGE = "invalid_language";
	private static final Locale LANGUAGE_LOCALE = LocaleUtils.toLocale(LANGUAGE);
	private static final String TAG_DEFINITION_CODE = "TAG_DEFINITION_CODE";
	private static final String TAG_DEFINITION_DISPLAY_NAME = "TAG_DEFINITION_DISPLAY_NAME";
	private static final String TAG_DEFINITION_NAME = "TAG_DEFINITION_NAME";
	private static final String TAG_DEFINITION_TYPE = "text";
	private static final String TAG_DEFINITION_DESCRIPTION = "TAG_DEFINITION_DESCRIPTION";

	private static final DisplayValue INVALID_DISPLAY_LOCALE = new DisplayValue(INVALID_LANGUAGE, TAG_DEFINITION_DISPLAY_NAME);
	private static final DisplayValue INVALID_DISPLAY_NAME = new DisplayValue(LANGUAGE, StringUtils.EMPTY);
	private static final String TAG_DEFINITION_DICTIONARY_ONE = "TAG_DICTIONARY_1";
	private static final String TAG_DEFINITION_INVALID_TYPE = "foo";
	private static final String TAG_DEFINITION_INVALID_DICTIONARY = "foo";
	private static final String IE_MESSAGE = "ieMessage";
	private static final String CODE = "code";

	private TagDefinitionAdapter tagDefinitionAdapter;

	@Mock
	private BeanFactory mockBeanFactory;

	@Mock
	private TagDefinition mockTagDefinition;

	@Mock
	private TagValueType mockTagValueType;

	@Mock
	private TagDictionary mockTagDictionary;

	@Mock
	private TagDictionaryService mockTagDictionaryService;

	@Mock
	private TagValueTypeService mockTagValueTypeService;

	@Mock
	private LocalizedProperties mockLocalizedProperties;

	/**
	 * Sets Up Test Case.
	 */
	@Before
	public void setUp() {
		tagDefinitionAdapter = new TagDefinitionAdapter();
		tagDefinitionAdapter.setBeanFactory(mockBeanFactory);
		tagDefinitionAdapter.setTagDictionaryService(mockTagDictionaryService);
		tagDefinitionAdapter.setTagValueTypeService(mockTagValueTypeService);

		when(mockBeanFactory.getPrototypeBean(ContextIdNames.TAG_DEFINITION, TagDefinition.class)).thenReturn(new TagDefinitionImpl());
	}

	/**
	 * Tests that the DTO is properly populated from the TagDefinition domain object
	 * during export.
	 */
	@Test
	public void testPopulateDTO() {
		TagDefinitionDTO dto = tagDefinitionAdapter.createDtoObject();

		final Map<String, LocalizedPropertyValue> localizedPropertyValueMap = new HashMap<>();
		localizedPropertyValueMap.put(LANGUAGE, new TagDefinitionLocalizedPropertyValueImpl());

		when(mockLocalizedProperties.getLocalizedPropertiesMap()).thenReturn(localizedPropertyValueMap);
		when(mockLocalizedProperties.getLocaleFromKey(LANGUAGE)).thenReturn(LANGUAGE_LOCALE);
		when(mockLocalizedProperties.getValueWithoutFallBack(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, LANGUAGE_LOCALE))
				.thenReturn(TAG_DEFINITION_DISPLAY_NAME);
		when(mockLocalizedProperties.getPropertyNameFromKey(LANGUAGE)).thenReturn(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME);
		when(mockTagDefinition.getLocalizedProperties()).thenReturn(mockLocalizedProperties);
		when(mockTagDefinition.getGuid()).thenReturn(TAG_DEFINITION_CODE);
		when(mockTagDefinition.getName()).thenReturn(TAG_DEFINITION_NAME);
		when(mockTagDefinition.getDescription()).thenReturn(TAG_DEFINITION_DESCRIPTION);
		when(mockTagDefinition.getValueType()).thenReturn(mockTagValueType);
		when(mockTagValueType.getGuid()).thenReturn(TAG_DEFINITION_TYPE);
		when(mockTagDictionaryService.getTagDictionaries()).thenReturn(Collections.singletonList(mockTagDictionary));
		when(mockTagDictionary.getGuid()).thenReturn(TAG_DEFINITION_DICTIONARY_ONE);
		when(mockTagDictionary.getTagDefinitions()).thenReturn(Collections.singleton(mockTagDefinition));

		tagDefinitionAdapter.populateDTO(mockTagDefinition, dto);

		assertThat(dto.getCode()).isEqualTo(TAG_DEFINITION_CODE);
		assertThat(dto.getDescription()).isEqualTo(TAG_DEFINITION_DESCRIPTION);
		assertThat(dto.getName()).isEqualTo(TAG_DEFINITION_NAME);
		assertThat(dto.getType()).isEqualTo(TAG_DEFINITION_TYPE);

		final List<DisplayValue> nameValues = dto.getNameValues();
		assertThat(nameValues.size()).isEqualTo(1);
		assertThat(nameValues.get(0).getLanguage()).isEqualTo(LANGUAGE);
		assertThat(nameValues.get(0).getValue()).isEqualTo(TAG_DEFINITION_DISPLAY_NAME);

		final List<String> dictionaries = dto.getDictionaries();
		assertThat(dictionaries.size()).isEqualTo(1);
		assertThat(dictionaries.get(0)).isEqualTo(TAG_DEFINITION_DICTIONARY_ONE);
	}

	/**
	 * Tests that all of the TagDefinition fields provided during
	 * import are successfully transferred from the DTO into the domain object.
	 */
	@Test
	public void testPopulateDomain() {
		TagDefinitionDTO dto = buildValidDTO();

		when(mockTagValueTypeService.getTagValueTypes()).thenReturn(Collections.singletonList(mockTagValueType));
		when(mockTagValueType.getGuid()).thenReturn(TAG_DEFINITION_TYPE);
		when(mockTagDefinition.getLocalizedProperties()).thenReturn(mockLocalizedProperties);
		when(mockTagDictionaryService.getTagDictionaries()).thenReturn(Collections.singletonList(mockTagDictionary));
		when(mockTagDictionary.getGuid()).thenReturn(TAG_DEFINITION_DICTIONARY_ONE);

		tagDefinitionAdapter.populateDomain(dto, mockTagDefinition);

		verify(mockLocalizedProperties).setValue(TagDefinition.LOCALIZED_PROPERTY_DISPLAY_NAME, LANGUAGE_LOCALE,
				TAG_DEFINITION_DISPLAY_NAME);
		verify(mockTagDefinition).setGuid(TAG_DEFINITION_CODE);
		verify(mockTagDefinition).setName(TAG_DEFINITION_NAME);
		verify(mockTagDefinition).setDescription(TAG_DEFINITION_DESCRIPTION);
		verify(mockTagDefinition).setValueType(mockTagValueType);
		verify(mockTagDictionary).addTagDefinition(mockTagDefinition);
	}

	/**
	 * Tests that the proper exception is thrown if an empty tag definition code/guid is supplied
	 * during import.
	 */
	@Test
	public void testPopulateDomainWithEmptyCodeExpectedRollBack() {
		TagDefinitionDTO dto = buildValidDTO();
		dto.setCode("");

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, null))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10904_MISSING_REQUIRED_FIELD)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests the error condition when an invalid locale is specified for
	 * a tag definition display name during import.
	 */
	@Test
	public void testPopulateDomainWithInvalidLocaleExpectedRollback() {
		TagDefinitionDTO dto = buildValidDTO();
		dto.setNameValues(Collections.singletonList(INVALID_DISPLAY_LOCALE));

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, mockTagDefinition))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10902_INVALID_LOCALE)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests the error condition when the value for display name is not specified for
	 * a tag definition during import.
	 */
	@Test
	public void testPopulateDomainWithEmptyDisplayNameExpectedRollback() {
		TagDefinitionDTO dto = buildValidDTO();
		dto.setNameValues(Collections.singletonList(INVALID_DISPLAY_NAME));

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, mockTagDefinition))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10904_MISSING_REQUIRED_FIELD)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests the error condition when the value for name is not specified for
	 * a tag definition during import.
	 */
	@Test
	public void testPopulateDomainWithEmptyNameExpectedRollback() {
		TagDefinitionDTO dto = buildValidDTO();
		dto.setName(null);

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, mockTagDefinition))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10904_MISSING_REQUIRED_FIELD)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests the error condition when the value for type is not specified for
	 * a tag definition during import.
	 */
	@Test
	public void testPopulateDomainWithEmptyTypeExpectedRollback() {
		TagDefinitionDTO dto = buildValidDTO();
		dto.setType(null);

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, mockTagDefinition))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10904_MISSING_REQUIRED_FIELD)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests the error condition when the value for type is an invalid value for
	 * a tag definition during import.
	 */
	@Test
	public void testPopulateDomainWithInvalidTypeExpectedRollback() {
		final TagDefinitionDTO dto = buildValidDTO();
		dto.setType(TAG_DEFINITION_INVALID_TYPE);

		when(mockTagDefinition.getLocalizedProperties()).thenReturn(mockLocalizedProperties);

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, mockTagDefinition))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10905_INVALID_VALUE_TYPE)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests the error condition when the value for type is an invalid value for
	 * a tag definition during import.
	 */
	@Test
	public void testPopulateDomainWithInvalidDictionaryExpectedRollback() {
		final TagDefinitionDTO dto = buildValidDTO();
		dto.setDictionaries(Collections.singletonList(TAG_DEFINITION_INVALID_DICTIONARY));

		when(mockTagDefinition.getLocalizedProperties()).thenReturn(mockLocalizedProperties);
		when(mockTagValueTypeService.getTagValueTypes()).thenReturn(Collections.singletonList(mockTagValueType));
		when(mockTagValueType.getGuid()).thenReturn(TAG_DEFINITION_TYPE);
		when(mockTagDictionaryService.getTagDictionaries()).thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> tagDefinitionAdapter.populateDomain(dto, mockTagDefinition))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting(IE_MESSAGE)
				.filteredOn(CODE, TagDefinitionAdapter.IE_10906_INVALID_TAG_DICTIONARY)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests creation of Domain Object.
	 */
	@Test
	public void testCreateDomainObject() {
		TagDefinition domainObject = tagDefinitionAdapter.createDomainObject();
		assertThat(domainObject).isNotNull();
		assertThat(domainObject).isInstanceOf(TagDefinition.class);
	}

	/**
	 * Tests creation of DTO Object.
	 */
	@Test
	public void testCreateDtoObject() {
		TagDefinitionDTO dtoObject = tagDefinitionAdapter.createDtoObject();
		assertThat(dtoObject).isNotNull();
		assertThat(dtoObject).isInstanceOf(TagDefinitionDTO.class);
	}

	private TagDefinitionDTO buildValidDTO() {
		TagDefinitionDTO dto = tagDefinitionAdapter.createDtoObject();
		dto.setCode(TAG_DEFINITION_CODE);
		dto.setDescription(TAG_DEFINITION_DESCRIPTION);
		dto.setType(TAG_DEFINITION_TYPE);
		dto.setNameValues(Collections.singletonList(new DisplayValue(LANGUAGE, TAG_DEFINITION_DISPLAY_NAME)));
		dto.setName(TAG_DEFINITION_NAME);
		dto.setDictionaries(Collections.singletonList(TAG_DEFINITION_DICTIONARY_ONE));
		return dto;
	}
}
