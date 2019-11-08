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
import com.elasticpath.domain.modifier.ModifierGroupLdf;

/**
 * Adapter from {@link ModifierGroupLdf} to {@link ProjectionLocaleAdapter}.
 */
public class ModifierGroupLdfAdapter implements ProjectionLocaleAdapter {

	private final Set<ModifierGroupLdf> cartItemModifierGroupLdf;
	private final Locale defaultLocale;

	/**
	 * Constructor for ModifierGroupLdfAdapter.
	 *
	 * @param defaultLocale is default locale.
	 * @param cartItemModifierGroupLdf list of {@link ModifierGroupLdf}
	 */
	public ModifierGroupLdfAdapter(final Locale defaultLocale, final Set<ModifierGroupLdf> cartItemModifierGroupLdf) {
		this.cartItemModifierGroupLdf = Optional.ofNullable(cartItemModifierGroupLdf)
				.filter(CollectionUtils::isNotEmpty)
				.map(Collections::unmodifiableSet)
				.orElseGet(Collections::emptySet);
		this.defaultLocale = defaultLocale;
	}

	@Override
	public Map<Locale, String> getCatalogLocaleByValue() {
		return cartItemModifierGroupLdf
				.stream()
				.collect(Collectors.toMap(item -> LocaleUtils.toLocale(item.getLocale()),
						ModifierGroupLdf::getDisplayName));
	}

	@Override
	public Locale getDefaultCatalogLocale() {
		return defaultLocale;
	}
}
