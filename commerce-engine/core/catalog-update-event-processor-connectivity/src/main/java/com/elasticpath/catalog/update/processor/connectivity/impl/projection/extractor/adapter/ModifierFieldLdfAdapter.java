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
import com.elasticpath.domain.modifier.ModifierFieldLdf;

/**
 * Adapter from {@link ModifierFieldLdf} to {@link ProjectionLocaleAdapter}.
 */
public class ModifierFieldLdfAdapter implements ProjectionLocaleAdapter {

	private final Set<ModifierFieldLdf> cartItemModifierFieldLdf;
	private final Locale defaultLocale;

	/**
	 * Constructor for ModifierGroupLdfAdapter.
	 *
	 * @param defaultLocale is default locale.
	 * @param cartItemModifierFieldLdf list of {@link ModifierFieldLdf}
	 */
	public ModifierFieldLdfAdapter(final Locale defaultLocale, final Set<ModifierFieldLdf> cartItemModifierFieldLdf) {
		this.cartItemModifierFieldLdf = Optional.ofNullable(cartItemModifierFieldLdf)
				.filter(CollectionUtils::isNotEmpty)
				.map(Collections::unmodifiableSet)
				.orElseGet(Collections::emptySet);
		this.defaultLocale = defaultLocale;
	}

	public Map<Locale, String> getCatalogLocaleByValue() {
		return cartItemModifierFieldLdf
				.stream()
				.collect(Collectors.toMap(item -> LocaleUtils.toLocale(item.getLocale()),
						ModifierFieldLdf::getDisplayName));
	}

	@Override
	public Locale getDefaultCatalogLocale() {
		return defaultLocale;
	}
}