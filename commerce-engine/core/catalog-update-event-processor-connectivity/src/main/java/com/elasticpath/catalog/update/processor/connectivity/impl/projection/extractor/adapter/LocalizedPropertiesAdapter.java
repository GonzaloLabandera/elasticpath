/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.LocaleUtils;

import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;

/**
 * Adapter from {@link LocalizedProperties} to {@link ProjectionLocaleAdapter}.
 */
public class LocalizedPropertiesAdapter implements ProjectionLocaleAdapter {

	private static final String SEPARATOR = "_";
	private final LocalizedProperties localizedProperties;
	private final Locale defaultLocale;

	/**
	 * Constructor for LocalizedPropertiesAdapter.
	 *
	 * @param defaultLocale is default locale.
	 * @param localizedProperties {@link LocalizedProperties}
	 */
	public LocalizedPropertiesAdapter(final Locale defaultLocale, final LocalizedProperties localizedProperties) {
		this.localizedProperties = localizedProperties;
		this.defaultLocale = defaultLocale;
	}

	@Override
	public Map<Locale, String> getCatalogLocaleByValue() {
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = Optional.ofNullable(localizedProperties.getLocalizedPropertiesMap())
				.filter(MapUtils::isNotEmpty)
				.orElse(Collections.emptyMap());
		return localizedPropertiesMap
				.entrySet()
				.stream()
				.collect(Collectors.toMap(entry -> getLocaleFromKey(entry.getKey()),
						entry -> entry.getValue().getValue()));
	}

	/**
	 * Get the locale from the key in the map.
	 *
	 * @param keyFromMap the key in the map
	 * @return the locale
	 */
	private Locale getLocaleFromKey(final String keyFromMap) {
		final String[] keys = keyFromMap.split(SEPARATOR);
		String locale = keys[1];
		if (keys.length > 2) {
			locale += SEPARATOR + keys[2];
		}

		return LocaleUtils.toLocale(locale);
	}

	@Override
	public Locale getDefaultCatalogLocale() {
		return defaultLocale;
	}
}
