/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.domain.attribute.AttributeGroup;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.attribute.AttributeValueGroup;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.store.Store;

/**
 * Tests {@link CategoryTranslationExtractor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CategoryTranslationExtractorTest {

	public static final String TEST_VALUE = "testValue";

	@Test
	public void extractTranslationsTest() {
		final Translation translation = new Translation("en", "displayName");
		final Attribute attribute = new Attribute("code", null, Collections.singletonList(new AttributeTranslation(new Translation("en",
				"attributeName"), "", false)), null, false);
		final Category category = getCategory();
		final Store store = getStore();
		final Catalog catalog = getCatalog();


		final CategoryTranslationExtractor extractor = new CategoryTranslationExtractor(Collections.singletonList(translation),
				Collections.singletonList(attribute), category, store, catalog);

		assertThat(extractor.getCategoryTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(extractor.getCategoryTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(extractor.getCategoryTranslations().get(0).getDetails().get(0).getName()).isEqualTo("code");
		assertThat(extractor.getCategoryTranslations().get(0).getDetails().get(0).getDisplayName()).isEqualTo("attributeName");
		assertThat(extractor.getCategoryTranslations().get(0).getDetails().get(0).getValues().get(0)).isEqualTo(TEST_VALUE);
		assertThat(extractor.getCategoryTranslations().get(0).getDetails().get(0).getDisplayValues().get(0)).isEqualTo(TEST_VALUE);
	}

	private Catalog getCatalog() {

		final Catalog catalog = mock(Catalog.class);
		when(catalog.getDefaultLocale()).thenReturn(Locale.ENGLISH);
		return catalog;
	}

	private Store getStore() {
		final Store store = mock(Store.class);
		when(store.getSupportedLocales()).thenReturn(Collections.singletonList(Locale.ENGLISH));
		return store;
	}

	private Category getCategory() {
		final Category category = mock(Category.class);
		final CategoryType categoryType = mock(CategoryType.class);
		final AttributeGroup attributeGroup = mock(AttributeGroup.class);
		final AttributeValueGroup attributeValueGroup = mock(AttributeValueGroup.class);
		final AttributeValue attributeValue = mock(AttributeValue.class);
		final com.elasticpath.domain.attribute.Attribute attribute = mock(com.elasticpath.domain.attribute.Attribute.class);

		when(category.getCategoryType()).thenReturn(categoryType);
		when(categoryType.getAttributeGroup()).thenReturn(attributeGroup);
		when(category.getAttributeValueGroup()).thenReturn(attributeValueGroup);
		when(attributeValue.getAttribute()).thenReturn(attribute);
		when(attributeValue.getValue()).thenReturn("testValue");
		when(attributeValue.getStringValue()).thenReturn("testValue");
		when(attributeValue.getAttributeType()).thenReturn(AttributeType.SHORT_TEXT);
		when(attribute.getKey()).thenReturn("code");
		when(attributeValueGroup.getFullAttributeValues(attributeGroup, Locale.ENGLISH))
				.thenReturn(Collections.singletonList(attributeValue));

		return category;
	}
}