/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.TranslatedName;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocalizedPropertiesAdapter;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;

/**
 * Represents a base entity for SkuOptionToProjectionConverter.
 */
public class SkuOptionToProjectionConverter implements Converter<SkuOption, Option> {

	private final CatalogTranslationExtractor translationExtractor;
	private final TimeService timeService;

	/**
	 * Constructor for SkuOptionToProjectionConverter.
	 *
	 * @param translationExtractor {@link CatalogTranslationExtractor}.
	 * @param timeService          the time service.
	 */
	public SkuOptionToProjectionConverter(final CatalogTranslationExtractor translationExtractor, final TimeService timeService) {
		this.translationExtractor = translationExtractor;
		this.timeService = timeService;
	}

	/**
	 * Convert skuOption {@link SkuOption} to Option {@link Option}
	 * for particularly store {@link Store}.
	 *
	 * @param skuOption {@link SkuOption}.
	 * @param store     {@link Store}.
	 * @return projection {@link Option}.
	 */
	@Override
	public Option convert(final SkuOption skuOption, final Store store, final Catalog catalog) {
		final List<OptionTranslation> optionTranslations = extractOptionTranslations(skuOption, store);

		final String skuOptionName = skuOption.getOptionKey();
		final String storeCode = store.getCode();

		ZonedDateTime currentTime = ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT"));

		return new Option(skuOptionName, storeCode, optionTranslations, currentTime, false);
	}

	private List<OptionTranslation> extractOptionTranslations(final SkuOption skuOption, final Store store) {
		final List<Translation> translationsExternal = extractTranslations(skuOption, store);

		return translationsExternal
				.stream()
				.map(optTranslation -> createOptionTranslation(skuOption, store, optTranslation))
				.collect(Collectors.toList());
	}

	private OptionTranslation createOptionTranslation(final SkuOption skuOption, final Store store, final Translation optTranslation) {
		final List<TranslatedName> translatedOptionValues = extractTranslatedOptionValues(skuOption, store, optTranslation.getLanguage());

		return new OptionTranslation(optTranslation.getLanguage(), optTranslation.getDisplayName(), translatedOptionValues);
	}

	private TranslatedName createTranslatedNameForEachValueKey(final Locale defaultCatalogLocale,
															   final Store store,
															   final String language,
															   final SkuOptionValue skuOptionValue) {
		final Optional<Translation> translationOptional = getOptionValueTranslationBySpecificLanguage(defaultCatalogLocale,
				store,
				language,
				skuOptionValue);

		return translationOptional
				.map(translation -> new TranslatedName(skuOptionValue.getOptionValueKey(), translation.getDisplayName()))
				.orElse(null);
	}

	private List<TranslatedName> extractTranslatedOptionValues(final SkuOption skuOption, final Store store,
															   final String language) {
		return skuOption
				.getOptionValues()
				.stream()
				.sorted(Comparator.comparingInt(SkuOptionValue::getOrdering))
				.map(skuOptionValue -> createTranslatedNameForEachValueKey(skuOption.getCatalog().getDefaultLocale(), store, language,
						skuOptionValue))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private Optional<Translation> getOptionValueTranslationBySpecificLanguage(final Locale defaultCatalogLocale, final Store store,
																			  final String language, final SkuOptionValue optionValue) {
		return extractTranslations(defaultCatalogLocale, optionValue.getLocalizedProperties(), store)
				.stream()
				.filter(optValTranslation -> optValTranslation.getLanguage().equals(language))
				.findAny();
	}

	private List<Translation> extractTranslations(final SkuOption skuOption, final Store store) {
		return extractTranslations(skuOption.getCatalog().getDefaultLocale(), skuOption.getLocalizedProperties(), store);
	}

	private List<Translation> extractTranslations(final Locale defaultCatalogLocale, final LocalizedProperties localizedProperties,
												  final Store store) {
		final Locale defaultStoreLocale = store.getDefaultLocale();
		final Collection<Locale> supportedLocales = store.getSupportedLocales();
		final LocalizedPropertiesAdapter localizedPropertiesAdapter = new LocalizedPropertiesAdapter(defaultCatalogLocale, localizedProperties);

		return translationExtractor.getProjectionTranslations(defaultStoreLocale, supportedLocales, localizedPropertiesAdapter);
	}
}
