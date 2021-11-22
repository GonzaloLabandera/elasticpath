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
import com.elasticpath.domain.misc.impl.TagGroupLocalizedPropertyValueImpl;
import com.elasticpath.importexport.common.adapters.tag.TagGroupAdapter;
import com.elasticpath.importexport.common.dto.tag.TagGroupDTO;
import com.elasticpath.importexport.common.exception.runtime.PopulationRollbackException;
import com.elasticpath.tags.domain.TagGroup;
import com.elasticpath.tags.domain.impl.TagGroupImpl;

/**
 * Tests population of DTO and domain objects by <code>TagGroupAdapter</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class TagGroupAdapterTest {

	private static final String LANGUAGE = "en";
	private static final String INVALID_LANGUAGE = "invalid_language";
	private static final Locale LANGUAGE_LOCALE = LocaleUtils.toLocale(LANGUAGE);
	private static final String TAG_GROUP_CODE = "TAG_GROUP_CODE";
	private static final String TAG_GROUP_DISPLAY_NAME = "TAG_GROUP_DISPLAY_NAME";
	private static final DisplayValue INVALID_DISPLAY_LOCALE = new DisplayValue(INVALID_LANGUAGE, TAG_GROUP_DISPLAY_NAME);
	private static final DisplayValue INVALID_DISPLAY_NAME = new DisplayValue(LANGUAGE, StringUtils.EMPTY);
	private static final DisplayValue VALID_DISPLAY_NAME = new DisplayValue(LANGUAGE, TAG_GROUP_DISPLAY_NAME);

	private final TagGroupAdapter tagGroupAdapter = new TagGroupAdapter();

	@Mock
	private BeanFactory mockBeanFactory;

	@Mock
	private TagGroup tagGroup;

	@Mock
	private LocalizedProperties mockLocalizedProperties;

	/**
	 * Sets Up Test Case.
	 */
	@Before
	public void setUp() {
		tagGroupAdapter.setBeanFactory(mockBeanFactory);

		when(mockBeanFactory.getPrototypeBean(ContextIdNames.TAG_GROUP, TagGroup.class)).thenReturn(new TagGroupImpl());
	}

	/**
	 * Tests that the DTO is properly populated from the TagGroup domain object
	 * during export.
	 */
	@Test
	public void testPopulateDTO() {
		TagGroupDTO dto = tagGroupAdapter.createDtoObject();

		final Map<String, LocalizedPropertyValue> localizedPropertyValueMap = new HashMap<>();
		localizedPropertyValueMap.put(LANGUAGE, new TagGroupLocalizedPropertyValueImpl());

		when(mockLocalizedProperties.getLocalizedPropertiesMap()).thenReturn(localizedPropertyValueMap);

		when(mockLocalizedProperties.getLocaleFromKey(LANGUAGE)).thenReturn(LANGUAGE_LOCALE);
		when(mockLocalizedProperties.getValueWithoutFallBack(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, LANGUAGE_LOCALE))
				.thenReturn(TAG_GROUP_DISPLAY_NAME);
		when(mockLocalizedProperties.getPropertyNameFromKey(LANGUAGE)).thenReturn(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME);
		when(tagGroup.getGuid()).thenReturn(TAG_GROUP_CODE);
		when(tagGroup.getLocalizedProperties()).thenReturn(mockLocalizedProperties);

		tagGroupAdapter.populateDTO(tagGroup, dto);

		assertThat(dto.getCode()).isEqualTo(TAG_GROUP_CODE);

		final List<DisplayValue> nameValues = dto.getNameValues();
		assertThat(nameValues.size()).isEqualTo(1);
		assertThat(nameValues.get(0).getLanguage()).isEqualTo(LANGUAGE);
		assertThat(nameValues.get(0).getValue()).isEqualTo(TAG_GROUP_DISPLAY_NAME);
	}

	/**
	 * Tests that the TagGroup guid/code and display name / locale provided during
	 * import are successfully transferred from the DTO into the domain object.
	 */
	@Test
	public void testPopulateDomain() {
		TagGroupDTO dto = createDTO(TAG_GROUP_CODE, VALID_DISPLAY_NAME);

		when(tagGroup.getLocalizedProperties()).thenReturn(mockLocalizedProperties);

		tagGroupAdapter.populateDomain(dto, tagGroup);

		verify(tagGroup).setGuid(TAG_GROUP_CODE);
		verify(mockLocalizedProperties).setValue(TagGroup.LOCALIZED_PROPERTY_DISPLAY_NAME, LANGUAGE_LOCALE, VALID_DISPLAY_NAME.getValue());
	}

	/**
	 * Tests the error condition when an invalid locale is specified for
	 * a tag group display name during import.
	 */
	@Test
	public void testPopulateDomainWithInvalidLocaleExpectedRollback() {
		TagGroupDTO dto = createDTO(TAG_GROUP_CODE, INVALID_DISPLAY_LOCALE);

		assertThatThrownBy(() -> tagGroupAdapter.populateDomain(dto, tagGroup))
				.isInstanceOf(PopulationRollbackException.class)
				.extracting("ieMessage")
				.filteredOn("code", TagGroupAdapter.IE_10902_INVALID_LOCALE_ERROR)
				.size()
				.isEqualTo(1);
	}

	/**
	 * Tests that the proper exception is thrown if an empty tag group code/guid is supplied
	 * during import.
	 */
	@Test
	public void testPopulateDomainWithEmptyCodeExpectedRollBack() {
		TagGroupDTO dto = createDTO("", null);

		assertThatThrownBy(() -> tagGroupAdapter.populateDomain(dto, null))
				.isInstanceOf(PopulationRollbackException.class)
				.hasMessageContaining(TagGroupAdapter.IE_10901_EMPTY_GROUP_CODE_ERROR);
	}

	/**
	 * Tests the error condition when the value for display name is not specified for
	 * a tag group during import.
	 */
	@Test
	public void testPopulateDomainWithInvalidDisplayNameExpectedRollback() {
		TagGroupDTO dto = createDTO(TAG_GROUP_CODE, INVALID_DISPLAY_NAME);

		assertThatThrownBy(() -> tagGroupAdapter.populateDomain(dto, tagGroup))
				.isInstanceOf(PopulationRollbackException.class)
				.hasMessageContaining(TagGroupAdapter.IE_10903_EMPTY_DISPLAY_VALUE_ERROR);
	}

	/**
	 * Tests creation of Domain Object.
	 */
	@Test
	public void testCreateDomainObject() {
		assertThat(tagGroupAdapter.createDomainObject()).isNotNull();
		assertThat(tagGroupAdapter.createDomainObject()).isInstanceOfAny(TagGroup.class);
	}

	/**
	 * Tests creation of DTO Object.
	 */
	@Test
	public void testCreateDtoObject() {
		assertThat(tagGroupAdapter.createDtoObject()).isNotNull();
		assertThat(tagGroupAdapter.createDtoObject()).isInstanceOfAny(TagGroupDTO.class);
	}

	private TagGroupDTO createDTO(final String code, final DisplayValue displayName) {
		TagGroupDTO dto = tagGroupAdapter.createDtoObject();
		dto.setCode(code);
		if (displayName != null) {
			dto.setNameValues(Collections.singletonList(displayName));
		}
		return dto;
	}
}
