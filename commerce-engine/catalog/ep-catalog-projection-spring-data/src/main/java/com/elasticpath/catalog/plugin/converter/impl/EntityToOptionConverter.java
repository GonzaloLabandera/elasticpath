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

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Converter to convert {@link ProjectionEntity} to {@link Option}.
 */
public class EntityToOptionConverter extends EntityToProjectionConverterBase implements EntityToProjectionConverter<Option> {

	/**
	 * Constructor.
	 *
	 * @param objectMapper  singleton object mapper.
	 */
	public EntityToOptionConverter(final ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public Option convert(final ProjectionEntity entity) {
		final List<OptionTranslation> translations = Optional.ofNullable(entity.getContent())
				.map(content -> readFromJson(entity, OptionTranslations.class))
				.map(OptionTranslations::getTranslations).orElse(Collections.emptyList());

		return new Option(entity.getCode(),
				entity.getStore(),
				translations,
				ZonedDateTime.ofInstant(entity.getProjectionDateTime().toInstant(), ZoneId.of("GMT")),
				entity.isDeleted());
	}
}
