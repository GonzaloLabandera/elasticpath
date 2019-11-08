/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor;

import static com.elasticpath.catalog.update.processor.connectivity.impl.exception.LocaleByValueNotFoundException.VALUES_NOT_FOUND_MESSAGE_FOR_MAP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.exception.LocaleByValueNotFoundException;

/**
 * Implementation of {@link CatalogTranslationExtractor}.
 */
public class CatalogTranslationExtractorImpl implements CatalogTranslationExtractor {
	private static final String DASH = "-";
	private static final String UNDERSCORES = "_";

	@Override
	public List<Translation> getProjectionTranslations(final Locale defaultStoreLocale, final Collection<Locale> supportedByStoreLocales,
													   final ProjectionLocaleAdapter projectionLocaleExtractor) {

		final Map<Locale, String> catalogLocaleByValue = Optional.ofNullable(projectionLocaleExtractor.getCatalogLocaleByValue())
				.filter(MapUtils::isNotEmpty)
				.map(Collections::unmodifiableMap)
				.orElseThrow(() ->
						new LocaleByValueNotFoundException(VALUES_NOT_FOUND_MESSAGE_FOR_MAP + projectionLocaleExtractor.getCatalogLocaleByValue()));
		return getTranslations(projectionLocaleExtractor.getDefaultCatalogLocale(), defaultStoreLocale, new ArrayList<>(supportedByStoreLocales),
				catalogLocaleByValue);
	}

	private List<Translation> getTranslations(final Locale defaultCatalogLocale,
											  final Locale defaultStoreLocale,
											  final List<Locale> supportedByStoreLocales,
											  final Map<Locale, String> catalogLocaleByValue) {
		final Map<Locale, String> translations = new HashMap<>(supportedByStoreLocales.size());

		for (final Locale storeLocale : supportedByStoreLocales) {
			final Optional<String> defaultStoreValue = Optional.ofNullable(catalogLocaleByValue.get(defaultStoreLocale))
					.filter(StringUtils::isNotEmpty);

			final Optional<String> defaultCatalogValue = Optional.ofNullable(catalogLocaleByValue.get(defaultCatalogLocale))
					.filter(StringUtils::isNotEmpty);

			final Optional<String> defaultAnyValue = catalogLocaleByValue.values().stream().filter(StringUtils::isNotEmpty).findAny();

			final String defaultValue = defaultStoreValue
					.filter(StringUtils::isNotEmpty)
					.map(Optional::of)
					.orElse(defaultCatalogValue)
					.filter(StringUtils::isNotEmpty)
					.map(Optional::of)
					.orElse(defaultAnyValue)
					.orElseThrow(() -> new LocaleByValueNotFoundException(VALUES_NOT_FOUND_MESSAGE_FOR_MAP + catalogLocaleByValue));

			final String similarValue = getSimilarLocaleFromCatalogForStore(storeLocale, catalogLocaleByValue);
			final String value = Optional.ofNullable(similarValue)
					.filter(StringUtils::isNotEmpty)
					.orElse(defaultValue);

			translations.put(storeLocale, value);
		}

		return translations.entrySet()
				.stream()
				.filter(projectionLocale -> supportedByStoreLocales.contains(projectionLocale.getKey()))
				.map(projectionLocale -> new Translation(projectionLocale.getKey().toString()
						.replace(UNDERSCORES, DASH), projectionLocale.getValue()))
				.collect(Collectors.toList());
	}

	/**
	 * Example:
	 * Given supported {@link com.elasticpath.domain.store.Store} Locale - fr_CA, supported {@link com.elasticpath.domain.catalog.Catalog} Locales -
	 * en, fr, fr_DE, fr_BE.
	 * Then method returns default value fr for fr_CA Locale.
	 *
	 * @param supportedLocale supported store Locale
	 * @param localeByValue   map, where key - Locale, value - translation of Locale
	 * @return translation of Locale if exist
	 */
	private String getDefaultValueWithoutCountryInfluence(final Locale supportedLocale, final Map<Locale, String> localeByValue) {
		for (Map.Entry<Locale, String> entry : localeByValue.entrySet()) {
			if (supportedLocale.getLanguage().equals(entry.getKey().getLanguage())
					&& StringUtils.isEmpty(entry.getKey().getCountry())) {
				return entry.getValue();

			}
		}

		return null;
	}

	/**
	 * Example:
	 * Given supported {@link com.elasticpath.domain.store.Store} Locale - fr_CA, supported {@link com.elasticpath.domain.catalog.Catalog} Locales -
	 * en, fr_DE, fr_BE.
	 * Then method returns default value fr_DE for fr_CA Locale.
	 *
	 * @param supportedLocale supported store Locale
	 * @param localeByValue   map, where key - Locale, value - translation of Locale
	 * @return translation of Locale if exist
	 */
	private String getSimilarLocaleFromCatalogForStore(final Locale supportedLocale, final Map<Locale, String> localeByValue) {
		String valueWithoutLangInfluence = getDefaultValueWithoutCountryInfluence(supportedLocale, localeByValue);
		if (!StringUtils.isEmpty(valueWithoutLangInfluence)) {
			return valueWithoutLangInfluence;
		}

		for (Map.Entry<Locale, String> entry : localeByValue.entrySet()) {
			if (supportedLocale.getLanguage().equals(entry.getKey().getLanguage())) {
				return entry.getValue();
			}
		}
		return null;
	}

}
