/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.LocaleByValueNotFoundException;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.ModifierGroupLdfAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocaleDependantFieldsAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocalizedPropertiesAdapter;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierGroupLdf;
import com.elasticpath.domain.modifier.impl.ModifierGroupLdfImpl;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.misc.impl.SkuOptionLocalizedPropertyValueImpl;

/**
 * Test for {@link CatalogTranslationExtractor}.
 */
public class CatalogTranslationExtractorUnitTest {

	private static final String EN_VALUE = "en_value";
	private static final String FR_VALUE = "fr_value";
	private static final String LOCALIZED_PROPERTY_FR_VALUE = "localizedProperty_fr";
	private static final String FR_CA_LOCALE = "fr_CA";
	private static final String FR_FR_LANGUAGE = "fr-FR";
	private static final String NAME = "name";
	private static final String LOCALIZED_PROPERTY_EN_KEY = "localizedProperty_en";
	private final CatalogTranslationExtractor projectionTranslationExtractor = new CatalogTranslationExtractorImpl();

	/**
	 * Given that Store A supports the following locales:  en, fr, fr_CA,
	 * Given that Catalog B supports the following locales:  en, fr,
	 * Then all projections for Store A will have translations: en, fr, fr_CA.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodReturnAppropriateCountOfTranslation() {
		final Locale defaultStoreLocale = new Locale("en");
		final List<Locale> supportedByStore = Collections.singletonList(new Locale("en"));
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = createSkuOptionValue(EN_VALUE);
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = createSkuOptionValue(FR_VALUE);

		localizedPropertiesMap.put(LOCALIZED_PROPERTY_EN_KEY, skuOptionLocalizedPropertyValueEn);
		localizedPropertiesMap.put(LOCALIZED_PROPERTY_FR_VALUE, skuOptionLocalizedPropertyValueFr);
		LocalizedProperties localizedProperties = new LocalizedPropertiesImpl();

		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		localizedProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final List<Translation> translations = projectionTranslationExtractor.getProjectionTranslations(defaultStoreLocale, supportedByStore,
				new LocalizedPropertiesAdapter(Locale.getDefault(), localizedProperties));
		assertThat(translations.get(0).getDisplayName()).isEqualTo(EN_VALUE);
	}

	private SkuOptionLocalizedPropertyValueImpl createSkuOptionValue(final String value) {
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValue = new SkuOptionLocalizedPropertyValueImpl();
		skuOptionLocalizedPropertyValue.setValue(value);
		return skuOptionLocalizedPropertyValue;
	}

	private List<Locale> createSupportedLocales() {
		return Arrays.asList(LocaleUtils.toLocale("en"), LocaleUtils.toLocale("fr"), LocaleUtils.toLocale(FR_CA_LOCALE));
	}

	/**
	 * Given that Store A supports the following locales:  en, fr, fr_CA,
	 * Given that Catalog B supports the following locales:  en, fr,
	 * Then all projections for Store A will have translations with values: en - en value, fr - fr value, fr_CA - fr value.
	 */
	@Test
	public void testThatGetProjectionTranslationsMethodFallbackLanguage() {
		final Locale defaultLocale = new Locale("en");
		final List<Locale> supportedLocales = createSupportedLocales();
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = createSkuOptionValue(EN_VALUE);
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = createSkuOptionValue(FR_VALUE);

		localizedPropertiesMap.put(LOCALIZED_PROPERTY_EN_KEY, skuOptionLocalizedPropertyValueEn);
		localizedPropertiesMap.put(LOCALIZED_PROPERTY_FR_VALUE, skuOptionLocalizedPropertyValueFr);
		LocalizedProperties localizedProperties = new LocalizedPropertiesImpl();

		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		localizedProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultLocale, supportedLocales,
						new LocalizedPropertiesAdapter(Locale.getDefault(), localizedProperties));

		assertThat(translations)
				.filteredOn(translation -> "fr-CA".equals(translation.getLanguage()))
				.extracting(Translation::getDisplayName)
				.containsExactly(localizedPropertiesMap.get(LOCALIZED_PROPERTY_FR_VALUE).getValue());
	}

	/**
	 * Given that Store A supports the following locales:  en, fr, fr_CA,
	 * Given that Catalog B supports the following locales:  en, fr_ca,
	 * Then all projections for Store A will have translations with values: en - en value, fr - fr_ca value, fr_ca - fr_ca value.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodReplaceLocaleForTheSimilar() {
		final Locale defaultLocale = new Locale("en");
		final List<Locale> supportedLocales = createSupportedLocales();

		ModifierGroupLdf cartItemModifierGroupLdf = new ModifierGroupLdfImpl();
		cartItemModifierGroupLdf.setLocale("en");
		cartItemModifierGroupLdf.setDisplayName(EN_VALUE);

		ModifierGroupLdf cartItemModifierGroupLdfFrCa = new ModifierGroupLdfImpl();
		cartItemModifierGroupLdfFrCa.setLocale(FR_CA_LOCALE);
		cartItemModifierGroupLdfFrCa.setDisplayName("fr_CA_value");
		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultLocale, supportedLocales,
						new ModifierGroupLdfAdapter(Locale.getDefault(),
								new HashSet<>(Arrays.asList(cartItemModifierGroupLdf, cartItemModifierGroupLdfFrCa))));

		assertThat(translations)
				.filteredOn(translation -> "fr".equals(translation.getLanguage()))
				.extracting(Translation::getDisplayName)
				.containsExactly(cartItemModifierGroupLdfFrCa.getDisplayName());
	}

	/**
	 * Given that Store A supports the following locales:  en, fr, fr_CA,
	 * Given that Catalog B supports the following locales:  en, fr, fr_ca,
	 * Then all projections for Store A will have translations with values: en - en value, fr - fr value, fr_ca - fr_ca value.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodCreateAllNeededTranslation() {
		final Locale defaultLocale = LocaleUtils.toLocale("en");
		final List<Locale> supportedLocales = createSupportedLocales();
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = createSkuOptionValue(EN_VALUE);
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFrCa = createSkuOptionValue("fr_CA_value");
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = createSkuOptionValue(FR_VALUE);

		localizedPropertiesMap.put(LOCALIZED_PROPERTY_EN_KEY, skuOptionLocalizedPropertyValueEn);
		localizedPropertiesMap.put("localizedProperty_fr_CA", skuOptionLocalizedPropertyValueFrCa);
		localizedPropertiesMap.put(LOCALIZED_PROPERTY_FR_VALUE, skuOptionLocalizedPropertyValueFr);

		final LocalizedProperties localizedProperties = new LocalizedPropertiesImpl();
		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		localizedProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultLocale, supportedLocales,
						new LocalizedPropertiesAdapter(Locale.getDefault(), localizedProperties));

		assertThat(translations)
				.filteredOn(translation -> "fr".equals(translation.getLanguage()))
				.extracting(Translation::getDisplayName)
				.containsExactly(localizedPropertiesMap.get(LOCALIZED_PROPERTY_FR_VALUE).getValue());
	}

	/**
	 * Given that Store A supports the following locales:  en, fr, fr_CA,
	 * Given that Catalog B supports the following locales:  fr_ca,
	 * Then all projections for Store A will have translations with values: en - fr_ca value, fr - fr_ca value, fr_ca - fr_ca value.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodCreateTranslationFromOneCatalogLanguage() {
		final Locale defaultLocale = new Locale("en");
		final List<Locale> supportedLocales = createSupportedLocales();
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFrCa = createSkuOptionValue("fr_CA_value");

		localizedPropertiesMap.put("localizedProperty_fr_CA", skuOptionLocalizedPropertyValueFrCa);

		final LocalizedProperties localizedProperties = new LocalizedPropertiesImpl();
		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		localizedProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultLocale, supportedLocales,
						new LocalizedPropertiesAdapter(LocaleUtils.toLocale(FR_CA_LOCALE), localizedProperties));

		assertThat(translations)
				.filteredOn(translation -> "en".equals(translation.getLanguage()))
				.extracting(Translation::getDisplayName)
				.containsExactly(localizedPropertiesMap.get("localizedProperty_fr_CA").getValue());
	}

	/**
	 * GetProjectionTranslations method throws exception, than Localized property is empty.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodThrowsException() {
		final Locale defaultLocale = new Locale("en");
		final List<Locale> supportedLocales = createSupportedLocales();

		final LocalizedProperties localizedProperties = new LocalizedPropertiesImpl();

		assertThatExceptionOfType(LocaleByValueNotFoundException.class).isThrownBy(() -> projectionTranslationExtractor
				.getProjectionTranslations(defaultLocale, supportedLocales, new LocalizedPropertiesAdapter(Locale.getDefault(), localizedProperties)))
				.withMessageContaining("Values not found for map:")
				.withNoCause();
	}

	/**
	 * Given that Catalog supports en (default) and fr languages and domain object has names for both locales,
	 * Given that Store supports zh_SG language,
	 * Then projection has one locale zh_SG with value from catalog's en locale in translations.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodUsesDefaultCatalogTranslationValueWhenStoryLocaleIsNotSupportedByCatalog() {
		final Locale defaultStoreLocale = new Locale("zh_SG");
		final List<Locale> supportedByStoreLocales = createSupportedLocales();
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = createSkuOptionValue("fr_value");
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = createSkuOptionValue("en_value");


		localizedPropertiesMap.put("localizedProperty_fr", skuOptionLocalizedPropertyValueFr);
		localizedPropertiesMap.put(LOCALIZED_PROPERTY_EN_KEY, skuOptionLocalizedPropertyValueEn);


		final LocalizedProperties catalogProperties = new LocalizedPropertiesImpl();
		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		catalogProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultStoreLocale, supportedByStoreLocales,
						new LocalizedPropertiesAdapter(LocaleUtils.toLocale("en"), catalogProperties));

		assertThat(translations)
				.filteredOn(translation -> "en".equals(translation.getLanguage()))
				.extracting(Translation::getDisplayName)
				.containsExactly(localizedPropertiesMap.get(LOCALIZED_PROPERTY_EN_KEY).getValue());
	}

	/**
	 * Given that Catalog supports en (default) and fr languages and domain object has names for both locales,
	 * Given that Store supports fr language,
	 * Then projection has one locale fr with value from catalog's fr locale in translations.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodUsesCreatesTranslationsOnlyForLanguagesSupportedByStore() {
		final Locale defaultStoreLocale = new Locale("fr");
		final List<Locale> supportedByStoreLocales = Collections.singletonList(LocaleUtils.toLocale("fr"));
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = createSkuOptionValue("fr_value");
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = createSkuOptionValue("en_value");


		localizedPropertiesMap.put("localizedProperty_fr", skuOptionLocalizedPropertyValueFr);
		localizedPropertiesMap.put(LOCALIZED_PROPERTY_EN_KEY, skuOptionLocalizedPropertyValueEn);


		final LocalizedProperties catalogProperties = new LocalizedPropertiesImpl();
		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		catalogProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultStoreLocale, supportedByStoreLocales,
						new LocalizedPropertiesAdapter(LocaleUtils.toLocale("en"), catalogProperties));

		assertThat(translations).hasSize(1);
		assertThat(translations.get(0).getLanguage()).isEqualTo("fr");
	}

	/**
	 * Given that Catalog supports de (default) and domain object has name for locale zh ,
	 * Given that Store supports fr_FR language,
	 * Then projection has one locale fr_FR with value from catalog's zh locale in translations.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodGetsTranslationsWhenDefaultStoreAndDefaultCatalogTranslationsNotInDomeinObject() {
		final Locale defaultStoreLocale = Locale.FRANCE;
		final List<Locale> supportedByStoreLocales = Collections.singletonList(Locale.FRANCE);
		final String chineseDisplayName = NAME;
		final StoreProduct storeProduct = mock(StoreProduct.class);
		final LocaleDependantFields localeDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.CHINESE)).thenReturn(localeDependantFields);
		when(localeDependantFields.getDisplayName()).thenReturn(chineseDisplayName);

		final ProjectionLocaleAdapter adapter = new LocaleDependantFieldsAdapter(Locale.GERMAN, storeProduct,
				Collections.singletonList(Locale.CHINESE));

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultStoreLocale, supportedByStoreLocales,
						adapter);

		assertThat(translations).hasSize(1);
		assertThat(translations.get(0).getLanguage()).isEqualTo(FR_FR_LANGUAGE);
		assertThat(translations.get(0).getDisplayName()).isEqualTo(chineseDisplayName);
	}

	/**
	 * Given that Catalog supports de (default) and domain object has name for locale zh and domain object has name as empty string for locale
	 * fr_FR ,
	 * Given that Store supports fr_FR language,
	 * Then projection has one locale fr_FR with value from catalog's zh locale in translations.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodGetsTranslationsWhenDefaultStoreTranslationIsEmptyStringInDomainObject() {
		final Locale defaultStoreLocale = Locale.FRANCE;
		final List<Locale> supportedByStoreLocales = Collections.singletonList(Locale.FRANCE);
		final String chineseDisplayName = NAME;
		final String frenchDisplayName = StringUtils.EMPTY;

		final StoreProduct storeProduct = mock(StoreProduct.class);


		final LocaleDependantFields chineseLocaleDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.CHINESE)).thenReturn(chineseLocaleDependantFields);
		when(chineseLocaleDependantFields.getDisplayName()).thenReturn(chineseDisplayName);

		final LocaleDependantFields frenchLocaleDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.FRANCE)).thenReturn(frenchLocaleDependantFields);
		when(frenchLocaleDependantFields.getDisplayName()).thenReturn(frenchDisplayName);

		final ProjectionLocaleAdapter adapter = new LocaleDependantFieldsAdapter(Locale.GERMAN, storeProduct,
				Collections.singletonList(Locale.CHINESE));

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultStoreLocale, supportedByStoreLocales,
						adapter);

		assertThat(translations).hasSize(1);
		assertThat(translations.get(0).getLanguage()).isEqualTo(FR_FR_LANGUAGE);
		assertThat(translations.get(0).getDisplayName()).isEqualTo(chineseDisplayName);
	}

	/**
	 * Given that Catalog supports de (default) and domain object has name for locale zh and domain object has name as empty string for locale
	 * fr_FR ,
	 * Given that Store supports fr_FR language,
	 * Then projection has one locale fr_FR with value from catalog's zh locale in translations.
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodGetsTranslationsWhenDefaultCatalogTranslationIsEmptyStringInDomainObject() {
		final Locale defaultStoreLocale = Locale.FRANCE;
		final List<Locale> supportedByStoreLocales = Collections.singletonList(Locale.FRANCE);
		final String chineseDisplayName = NAME;
		final String germanDisplayName = StringUtils.EMPTY;

		final StoreProduct storeProduct = mock(StoreProduct.class);


		final LocaleDependantFields chineseLocaleDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.CHINESE)).thenReturn(chineseLocaleDependantFields);
		when(chineseLocaleDependantFields.getDisplayName()).thenReturn(chineseDisplayName);

		final LocaleDependantFields germanLocaleDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.GERMAN)).thenReturn(germanLocaleDependantFields);
		when(germanLocaleDependantFields.getDisplayName()).thenReturn(germanDisplayName);

		final ProjectionLocaleAdapter adapter = new LocaleDependantFieldsAdapter(Locale.GERMAN, storeProduct,
				Collections.singletonList(Locale.CHINESE));

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(defaultStoreLocale, supportedByStoreLocales,
						adapter);

		assertThat(translations).hasSize(1);
		assertThat(translations.get(0).getLanguage()).isEqualTo(FR_FR_LANGUAGE);
		assertThat(translations.get(0).getDisplayName()).isEqualTo(chineseDisplayName);
	}

	/**
	 * Given that Catalog supports de (default) and store support locale with empty name for fr_FR.
	 *
	 */
	@Test
	public void ensureThatGetProjectionTranslationsMethodGetsTranslationsWhenStoreTranslationIsEmpty() {
		final Locale storeLocale = Locale.FRANCE;
		final List<Locale> supportedByStoreLocales = Collections.singletonList(Locale.FRANCE);
		final String franceDisplayName = "";
		final String germanDisplayName = NAME;

		final StoreProduct storeProduct = mock(StoreProduct.class);

		final LocaleDependantFields franceLocaleDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.FRANCE)).thenReturn(franceLocaleDependantFields);
		when(franceLocaleDependantFields.getDisplayName()).thenReturn(franceDisplayName);

		final LocaleDependantFields germanLocaleDependantFields = mock(LocaleDependantFields.class);
		when(storeProduct.getLocaleDependantFields(Locale.GERMAN)).thenReturn(germanLocaleDependantFields);
		when(germanLocaleDependantFields.getDisplayName()).thenReturn(germanDisplayName);

		final ProjectionLocaleAdapter adapter = new LocaleDependantFieldsAdapter(Locale.GERMAN, storeProduct,
				Collections.singletonList(storeLocale));

		final List<Translation> translations = projectionTranslationExtractor
				.getProjectionTranslations(storeLocale, supportedByStoreLocales,
						adapter);

		assertThat(translations).hasSize(1);
		assertThat(translations.get(0).getLanguage()).isEqualTo(FR_FR_LANGUAGE);
		assertThat(translations.get(0).getDisplayName()).isEqualTo(germanDisplayName);
	}
}
