/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocalizedPropertiesAdapter;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;

/**
 * A projection converter which converts Brand {@link com.elasticpath.domain.catalog.Brand} to Brand {@link Brand}.
 */
public class BrandToProjectionConverter implements Converter<com.elasticpath.domain.catalog.Brand, Brand> {

	private final CatalogTranslationExtractor translationExtractor;
	private final TimeService timeService;

	/**
	 * Constructor for BrandToProjectionConverter.
	 *
	 * @param translationExtractor {@link CatalogTranslationExtractor}.
	 * @param timeService          the time service.
	 */
	public BrandToProjectionConverter(final CatalogTranslationExtractor translationExtractor, final TimeService timeService) {
		this.translationExtractor = translationExtractor;
		this.timeService = timeService;
	}

	/**
	 * Convert Brand {@link com.elasticpath.domain.catalog.Brand} to Brand {@link Brand}
	 * for particularly store {@link Store}.
	 *
	 * @param brand {@link com.elasticpath.domain.catalog.Brand}.
	 * @param store {@link Store}.
	 * @return projection {@link Brand}.
	 */
	@Override
	public Brand convert(final com.elasticpath.domain.catalog.Brand brand, final Store store, final Catalog catalog) {
		final ZonedDateTime currentTime = ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT"));
		return new Brand(brand.getCode(), store.getCode(), extractTranslations(brand, store), currentTime, false);
	}

	private List<Translation> extractTranslations(final com.elasticpath.domain.catalog.Brand brand, final Store store) {
		return translationExtractor.getProjectionTranslations(store.getDefaultLocale(),
				store.getSupportedLocales(),
				new LocalizedPropertiesAdapter(brand.getCatalog().getDefaultLocale(), brand.getLocalizedProperties()));
	}

}
