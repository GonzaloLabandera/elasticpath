/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Converter to convert {@link ProjectionEntity} to {@link Brand}.
 */
public class EntityToBrandConverter extends EntityToProjectionConverterBase implements EntityToProjectionConverter<Brand> {

	/**
	 * Constructor.
	 *
	 * @param objectMapper  singleton object mapper.
	 */
	public EntityToBrandConverter(final ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public Brand convert(final ProjectionEntity entity) {
		final List<Translation> translations = Optional.ofNullable(entity.getContent())
				.map(content -> readFromJson(entity, BrandTranslations.class))
				.map(BrandTranslations::getTranslations).orElse(Collections.emptyList());

		return new Brand(entity.getCode(),
				entity.getStore(),
				translations,
				ZonedDateTime.ofInstant(entity.getProjectionDateTime().toInstant(), ZoneId.of("GMT")),
				entity.isDeleted());
	}
}
