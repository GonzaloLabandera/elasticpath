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

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.entity.translation.FieldMetadataTranslation;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Converter to convert {@link ProjectionEntity} to {@link FieldMetadata}.
 */
public class EntityToFieldMetadataConverter extends EntityToProjectionConverterBase implements EntityToProjectionConverter<FieldMetadata> {

	/**
	 * Constructor.
	 *
	 * @param objectMapper  singleton object mapper.
	 */
	public EntityToFieldMetadataConverter(final ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public FieldMetadata convert(final ProjectionEntity entity) {
		final List<FieldMetadataTranslation> translations = Optional.ofNullable(entity.getContent())
				.map(content -> readFromJson(entity, FieldMetadataTranslations.class))
				.map(FieldMetadataTranslations::getTranslations)
				.orElse(Collections.emptyList());

		return new FieldMetadata(entity.getCode(),
				entity.getStore(),
				translations,
				ZonedDateTime.ofInstant(entity.getProjectionDateTime().toInstant(), ZoneId.of("GMT")),
				entity.isDeleted());
	}
}
