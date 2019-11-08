/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.LocaleUtils;

import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.domain.modifier.ModifierFieldOptionLdf;

/**
 * Adapter from {@link ModifierFieldOptionLdf} to {@link ProjectionLocaleAdapter}.
 */
public class ModifierFieldOptionLdfAdapter implements ProjectionLocaleAdapter {

	private final Set<ModifierFieldOptionLdf> cartItemModifierFieldOptionLdfs;
	private final Locale defaultLocale;

	/**
	 * Constructor for ModifierFieldOptionLdfAdapter.
	 *
	 * @param defaultLocale                   is default locale.
	 * @param cartItemModifierFieldOptionLdfs set of {@link ModifierFieldOptionLdf}
	 */
	public ModifierFieldOptionLdfAdapter(final Locale defaultLocale,
												 final Set<ModifierFieldOptionLdf> cartItemModifierFieldOptionLdfs) {
		this.cartItemModifierFieldOptionLdfs = Optional.ofNullable(cartItemModifierFieldOptionLdfs)
				.filter(CollectionUtils::isNotEmpty)
				.map(Collections::unmodifiableSet)
				.orElseGet(Collections::emptySet);
		this.defaultLocale = defaultLocale;
	}

	@Override
	public Map<Locale, String> getCatalogLocaleByValue() {
		return cartItemModifierFieldOptionLdfs
				.stream()
				.collect(Collectors.toMap(item -> LocaleUtils.toLocale(item.getLocale()),
						ModifierFieldOptionLdf::getDisplayName));
	}

	@Override
	public Locale getDefaultCatalogLocale() {
		return defaultLocale;
	}
}
