/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.misc.TimeService;

/**
 * Test for {@link AttributeToProjectionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttributeToProjectionConverterUnitTest {

	@Mock
	private TimeService timeService;
	@Mock
	private CatalogTranslationExtractor translationExtractor;

	@Before
	public void setup() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
	}

	/**
	 * Given Attribute with filled attributeType, name, multiValueType fields.
	 * Given Store with filled storeCode field.
	 * Then method "convert" converts Attribute to Attribute. AttributeType, name, multiValueType fields are present in Attribute.
	 */
	@Test
	public void testThatConverterGetAttributeWithFilledNameFieldTypeMultiValueAndConvertToProjection() {
		final AttributeToProjectionConverter attributeConverter = new AttributeToProjectionConverter(timeService, translationExtractor);
		final com.elasticpath.domain.attribute.Attribute attribute = new AttributeImpl();
		final Store store = new StoreImpl();

		store.setCode("store_code");

		attribute.setAttributeType(AttributeType.LONG_TEXT);
		attribute.setKey("FABRIC");
		attribute.setMultiValueType(AttributeMultiValueType.RFC_4180);
		attribute.setLocalizedProperties(new LocalizedPropertiesImpl());

		final Attribute attributeProjection = attributeConverter.convert(attribute, store, mock(Catalog.class));

		assertThat(attributeProjection.getIdentity().getStore()).isEqualTo(store.getCode());
		assertThat(attributeProjection.getIdentity().getCode()).isEqualTo(attribute.getKey());
	}

	@Test
	public void testThatConverterExtractTranslations() throws DefaultValueRemovalForbiddenException {
		final AttributeToProjectionConverter attributeConverter = new AttributeToProjectionConverter(timeService, translationExtractor);
		final com.elasticpath.domain.attribute.Attribute attribute = new AttributeImpl();
		final Store store = new StoreImpl();

		store.setCode("store_code");
		store.setDefaultLocale(Locale.ENGLISH);
		store.setSupportedLocales(Collections.singleton(Locale.ENGLISH));

		attribute.setAttributeType(AttributeType.LONG_TEXT);
		attribute.setKey("FABRIC");
		attribute.setMultiValueType(AttributeMultiValueType.RFC_4180);
		attribute.setLocalizedProperties(new LocalizedPropertiesImpl());

		when(translationExtractor.getProjectionTranslations(eq(Locale.ENGLISH),
				eq(Collections.singleton(Locale.ENGLISH)),
				any(ProjectionLocaleAdapter.class))).thenReturn(Collections.singletonList(new Translation("en", "displayName")));

		final Attribute attributeProjection = attributeConverter.convert(attribute, store, mock(Catalog.class));

		assertThat(attributeProjection.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(attributeProjection.getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(attributeProjection.getTranslations().get(0).getMultiValue()).isTrue();
		assertThat(attributeProjection.getTranslations().get(0).getDataType()).isEqualTo(AttributeType.LONG_TEXT.getCamelCaseName());

	}

}
