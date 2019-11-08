/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.CatalogTranslationExtractorImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.SkuOptionLocalizedPropertyValueImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;

/**
 * Test for {@link BrandToProjectionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BrandToProjectionConverterTest {

	@Mock
	private TimeService timeService;

	@Before
	public void setup() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
	}

	/**
	 * Test, that converter get Brand with filled localized properties and convert it to Brand projection.
	 */
	@Test
	public void testThatConverterGetBrandWithFilledFieldsAndConvertItToProjection() {
		final com.elasticpath.domain.catalog.Brand brand = mock(com.elasticpath.domain.catalog.Brand.class);
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(Locale.getDefault());
		when(brand.getCatalog()).thenReturn(catalog);

		final Store store = mock(Store.class);
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = mock(SkuOptionLocalizedPropertyValueImpl.class);
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = mock(SkuOptionLocalizedPropertyValueImpl.class);
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();

		final String timHortonEn = "Tim Horton's";
		final String timHortonFr = "Chez Tim Horton";
		when(skuOptionLocalizedPropertyValueEn.getValue()).thenReturn(timHortonEn);
		when(skuOptionLocalizedPropertyValueFr.getValue()).thenReturn(timHortonFr);

		localizedPropertiesMap.put("localizedProperty_en", skuOptionLocalizedPropertyValueEn);
		localizedPropertiesMap.put("localizedProperty_fr", skuOptionLocalizedPropertyValueFr);

		final LocalizedProperties localizedProperties = mock(LocalizedProperties.class);

		when(localizedProperties.getLocalizedPropertiesMap()).thenReturn(localizedPropertiesMap);

		when(store.getCode()).thenReturn("storeCode");
		when(store.getDefaultLocale()).thenReturn(LocaleUtils.toLocale("en"));
		when(store.getSupportedLocales()).thenReturn(Arrays.asList(LocaleUtils.toLocale("en"), LocaleUtils.toLocale("fr")));

		when(brand.getCode()).thenReturn("TIM_HORTONS");
		when(brand.getLocalizedProperties()).thenReturn(localizedProperties);

		final CatalogTranslationExtractor extractor = new CatalogTranslationExtractorImpl();
		final BrandToProjectionConverter converter = new BrandToProjectionConverter(extractor, timeService);

		final Brand brandProjection = converter.convert(brand, store, mock(Catalog.class));

		assertThat(brandProjection.getIdentity().getCode()).isEqualTo(brand.getCode());
		assertThat(brandProjection.getIdentity().getStore()).isEqualTo(store.getCode());
		assertThat(brandProjection.getTranslations())
				.extracting(Translation::getDisplayName)
				.containsExactlyInAnyOrder(timHortonEn, timHortonFr);
	}
}
