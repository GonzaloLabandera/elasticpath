/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.catalog.ObjectWithLocaleDependantFields;

/**
 * Adapter from {@link com.elasticpath.domain.catalog.LocaleDependantFields} to {@link ProjectionLocaleAdapter}.
 */
public class LocaleDependantFieldsAdapter implements ProjectionLocaleAdapter {
	private final Map<Locale, String> catalogLocaleByValue;
	private final Locale defaultLocale;

	/**
	 * Constructor.
	 *
	 * @param defaultLocale         is default catalog locale.
	 * @param source                is an object with locale dependant fields.
	 * @param storeSupportedLocales list of {@link Locale}.
	 */
	public LocaleDependantFieldsAdapter(final Locale defaultLocale, final ObjectWithLocaleDependantFields source,
										final Collection<Locale> storeSupportedLocales) {
		this.defaultLocale = defaultLocale;
		this.catalogLocaleByValue = Stream.of(storeSupportedLocales, Collections.singleton(defaultLocale))
				.flatMap(Collection::stream)
				.collect(HashMap::new, (map, locale) -> map.put(locale, getLocalizedValue(source, locale)), HashMap::putAll);
	}

	@Override
	public Map<Locale, String> getCatalogLocaleByValue() {
		return catalogLocaleByValue;
	}

	@Override
	public Locale getDefaultCatalogLocale() {
		return defaultLocale;
	}

	private String getLocalizedValue(final ObjectWithLocaleDependantFields source, final Locale locale) {
		return Optional.ofNullable(source)
				.map(fields -> fields.getLocaleDependantFields(locale))
				.map(LocaleDependantFields::getDisplayName)
				.orElse(null);
	}
}
