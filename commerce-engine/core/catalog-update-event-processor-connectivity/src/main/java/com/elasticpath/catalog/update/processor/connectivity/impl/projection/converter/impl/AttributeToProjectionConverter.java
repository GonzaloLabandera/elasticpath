/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.extractor.CatalogTranslationExtractor;
import com.elasticpath.catalog.extractor.ProjectionLocaleAdapter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.converter.Converter;
import com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter.LocalizedPropertiesAdapter;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;

/**
 * Convert {@link com.elasticpath.domain.attribute.Attribute} to {@link Attribute}. List of
 * {@link com.elasticpath.catalog.entity.translation.Translation} is empty list.(Will be fixed)
 */
public class AttributeToProjectionConverter implements Converter<com.elasticpath.domain.attribute.Attribute, Attribute> {

	private final TimeService timeService;
	private final CatalogTranslationExtractor translationExtractor;


	/**
	 * Constructor.
	 *
	 * @param timeService the time service.
	 * @param translationExtractor {@link CatalogTranslationExtractor}.
	 */
	public AttributeToProjectionConverter(final TimeService timeService, final CatalogTranslationExtractor translationExtractor) {
		this.timeService = timeService;
		this.translationExtractor = translationExtractor;
	}

	/**
	 * Convert to {@link Attribute} by {@link Attribute}.
	 *
	 * @param attribute {@link com.elasticpath.domain.attribute.Attribute}
	 * @param store     {@link Store}
	 * @return {@link Attribute}
	 */
	@Override
	public Attribute convert(final com.elasticpath.domain.attribute.Attribute attribute, final Store store, final Catalog catalog) {
		final ProjectionLocaleAdapter localeAdapter = new LocalizedPropertiesAdapter(catalog.getDefaultLocale(), attribute.getLocalizedProperties());

		final List<AttributeTranslation> translations = translationExtractor
				.getProjectionTranslations(store.getDefaultLocale(), store.getSupportedLocales(), localeAdapter)
				.stream()
				.map(translation -> new AttributeTranslation(translation, attribute.getAttributeType().getCamelCaseName(),
						attribute.isMultiValueEnabled()))
				.collect(Collectors.toList());

		final ZonedDateTime currentTime = ZonedDateTime.ofInstant(timeService.getCurrentTime().toInstant(), ZoneId.of("GMT"));

		return new Attribute(attribute.getKey(), store.getCode(), translations, currentTime, false);
	}

}
