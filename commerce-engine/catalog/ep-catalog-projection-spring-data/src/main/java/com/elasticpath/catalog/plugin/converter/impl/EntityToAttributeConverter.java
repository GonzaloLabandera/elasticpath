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

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Converter to convert {@link ProjectionEntity} to {@link Attribute}.
 */
public class EntityToAttributeConverter extends EntityToProjectionConverterBase implements EntityToProjectionConverter<Attribute> {

	/**
	 * Constructor.
	 *
	 * @param objectMapper  singleton object mapper.
	 */
	public EntityToAttributeConverter(final ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public Attribute convert(final ProjectionEntity entity) {
		final List<AttributeTranslation> translations = Optional.ofNullable(entity.getContent())
				.map(content -> readFromJson(entity, AttributeTranslations.class))
				.map(AttributeTranslations::getTranslations).orElse(Collections.emptyList());

		return new Attribute(entity.getCode(),
				entity.getStore(),
				translations,
				ZonedDateTime.ofInstant(entity.getProjectionDateTime().toInstant(), ZoneId.of("GMT")),
				entity.isDeleted());
	}
}
