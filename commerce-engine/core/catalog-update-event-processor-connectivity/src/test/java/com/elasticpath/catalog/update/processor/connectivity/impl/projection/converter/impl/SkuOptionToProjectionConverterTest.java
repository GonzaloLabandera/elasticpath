/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.CatalogTranslationExtractorImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.SkuOptionLocalizedPropertyValueImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;

/**
 * Test for {@link SkuOptionToProjectionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkuOptionToProjectionConverterTest {

	@Mock
	private TimeService timeService;

	@Before
	public void setup() {
		when(timeService.getCurrentTime()).thenReturn(new Date());
	}

	/**
	 * Given SkuOption with filled localizedProperties, optionValues, optionKey fields.
	 * Given Store with filled storeCode, defaultLocale, supportedLocales fields.
	 * Then method "convert" converts SkuOption to Option. LocalizedProperties values, optionValues, optionKey fields are present in Option.
	 */
	@Test
	public void testThatConverterGetSkuOptionWithFilledFieldsAndConvertItToProjection() {
		final SkuOption skuOption = mock(SkuOption.class);
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(Locale.getDefault());
		when(skuOption.getCatalog()).thenReturn(catalog);
		final SkuOptionValue skuOptionValue = mock(SkuOptionValue.class);
		final SkuOptionValue skuOptionValueTwo = mock(SkuOptionValue.class);
		final LocalizedProperties localizedProperties = localizedPropertiesMock("Red", "Rouge");

		final LocalizedProperties localizedPropertiesTwo = localizedPropertiesMock("Blue", "Bleu");

		when(skuOptionValue.getLocalizedProperties()).thenReturn(localizedProperties);
		when(skuOptionValue.getOptionValueKey()).thenReturn("RED");

		when(skuOptionValueTwo.getLocalizedProperties()).thenReturn(localizedPropertiesTwo);
		when(skuOptionValueTwo.getOptionValueKey()).thenReturn("BLUE");

		final LocalizedProperties localizedPropertiesForKey = localizedPropertiesMock("Colour", "Couleur");

		when(skuOption.getLocalizedProperties()).thenReturn(localizedPropertiesForKey);
		when(skuOption.getOptionValues()).thenReturn(new HashSet<>(Arrays.asList(skuOptionValue, skuOptionValueTwo)));
		when(skuOption.getOptionKey()).thenReturn("COLOUR");

		final CatalogTranslationExtractor extractor = new CatalogTranslationExtractorImpl();
		final SkuOptionToProjectionConverter converter = new SkuOptionToProjectionConverter(extractor, timeService);
		final Store store = mock(Store.class);

		when(store.getCode()).thenReturn("storeCode");
		when(store.getDefaultLocale()).thenReturn(LocaleUtils.toLocale("en"));
		when(store.getSupportedLocales()).thenReturn(Arrays.asList(LocaleUtils.toLocale("en"), LocaleUtils.toLocale("fr"), LocaleUtils.toLocale(
				"fr_CA")));

		final Option option = converter.convert(skuOption, store, mock(Catalog.class));
		final String expectedLocalizedPropertyEnValue = localizedPropertiesForKey.getLocalizedPropertiesMap().get("localizedProperty_en").getValue();

		assertThat(option.getIdentity().getCode()).isEqualTo(skuOption.getOptionKey());
		assertThat(option.getIdentity().getStore()).isEqualTo(store.getCode());
		assertThat(option.getIdentity().getType()).isEqualTo(OPTION_IDENTITY_TYPE);

		assertThat(option.getTranslations())
				.extracting(Translation::getDisplayName)
				.containsAnyOf(expectedLocalizedPropertyEnValue);
	}

	@Test
	public void orderingOfOptionValuesShouldBeSameAsOrderingSkuOptionValues() {
		final Store store = mockStore();
		final SkuOptionValue skuOptionValue1 = mockSkuOptionValue("RED", 2);
		final SkuOptionValue skuOptionValue2 = mockSkuOptionValue("BLUE", 1);

		final SkuOption skuOption = mockSkuOption();
		when(skuOption.getOptionValues()).thenReturn(Arrays.asList(skuOptionValue1, skuOptionValue2));
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(Locale.getDefault());
		when(skuOption.getCatalog()).thenReturn(catalog);

		final CatalogTranslationExtractor extractor = new CatalogTranslationExtractorImpl();
		final SkuOptionToProjectionConverter converter = new SkuOptionToProjectionConverter(extractor, timeService);

		final Option option = converter.convert(skuOption, store, mock(Catalog.class));

		List<TranslatedName> optionValues = option.getTranslations().get(0).getOptionValues();

		assertThat(optionValues).extracting(TranslatedName::getValue).containsExactly("BLUE", "RED");
	}

	private Store mockStore() {
		final Store store = mock(Store.class);
		when(store.getCode()).thenReturn("storeCode");
		when(store.getDefaultLocale()).thenReturn(LocaleUtils.toLocale("en"));
		when(store.getSupportedLocales()).thenReturn(Collections.singletonList(LocaleUtils.toLocale("en")));

		return store;
	}

	private SkuOptionValue mockSkuOptionValue(final String optionValueKey, final int order) {
		final LocalizedProperties localizedProperties = mockLocalizedProperties();

		final SkuOptionValue skuOptionValue = mock(SkuOptionValue.class);
		when(skuOptionValue.getOptionValueKey()).thenReturn(optionValueKey);
		when(skuOptionValue.getLocalizedProperties()).thenReturn(localizedProperties);
		when(skuOptionValue.getOrdering()).thenReturn(order);

		return skuOptionValue;
	}

	private SkuOption mockSkuOption() {
		final LocalizedProperties localizedProperties = mockLocalizedProperties();

		final SkuOption skuOption = mock(SkuOption.class);
		when(skuOption.getOptionKey()).thenReturn("COLOUR");
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(Locale.getDefault());
		when(skuOption.getCatalog()).thenReturn(catalog);

		when(skuOption.getLocalizedProperties()).thenReturn(localizedProperties);

		return skuOption;
	}

	private LocalizedProperties mockLocalizedProperties() {
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = mock(SkuOptionLocalizedPropertyValueImpl.class);
		when(skuOptionLocalizedPropertyValueEn.getValue()).thenReturn("Red");

		final LocalizedProperties localizedProperties = mock(LocalizedProperties.class);
		when(localizedProperties.getLocalizedPropertiesMap()).thenReturn(Collections.singletonMap("localizedProperty_en",
				skuOptionLocalizedPropertyValueEn));

		return localizedProperties;
	}


	private LocalizedProperties localizedPropertiesMock(final String valueEng, final String valFr) {
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = mock(SkuOptionLocalizedPropertyValueImpl.class);
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = mock(SkuOptionLocalizedPropertyValueImpl.class);
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();

		when(skuOptionLocalizedPropertyValueEn.getValue()).thenReturn(valueEng);
		when(skuOptionLocalizedPropertyValueFr.getValue()).thenReturn(valFr);

		localizedPropertiesMap.put("localizedProperty_en", skuOptionLocalizedPropertyValueEn);
		localizedPropertiesMap.put("localizedProperty_fr", skuOptionLocalizedPropertyValueFr);
		final LocalizedProperties localizedProperties = mock(LocalizedProperties.class);

		when(localizedProperties.getLocalizedPropertiesMap()).thenReturn(localizedPropertiesMap);

		return localizedProperties;
	}
}
