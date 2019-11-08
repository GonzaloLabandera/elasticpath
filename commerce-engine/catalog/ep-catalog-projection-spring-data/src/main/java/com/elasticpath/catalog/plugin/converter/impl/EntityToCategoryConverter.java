/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.entity.category.CategoryProperties;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Converter to convert {@link ProjectionEntity} to {@link Category}.
 */
public class EntityToCategoryConverter extends EntityToProjectionConverterBase implements EntityToProjectionConverter<Category> {

	/**
	 * Constructor.
	 *
	 * @param objectMapper singleton object mapper.
	 */
	public EntityToCategoryConverter(final ObjectMapper objectMapper) {
		super(objectMapper);
	}

	@Override
	public Category convert(final ProjectionEntity category) {
		final CategoryContent categoryContent = Optional.ofNullable(category.getContent())
				.map(content -> readFromJson(category, CategoryContent.class))
				.orElse(new CategoryContent());

		final ProjectionProperties projectionProperties = new ProjectionProperties(category.getCode(), category.getStore(),
				ZonedDateTime.ofInstant(category.getProjectionDateTime().toInstant(), ZoneId.of("GMT")), category.isDeleted());

		return new Category(new CategoryProperties(projectionProperties, getProcessedField(category, categoryContent.getProperties())),
				categoryContent.getExtensions(),
				getProcessedField(category, categoryContent.getTranslations()),
				getProcessedField(category, categoryContent.getChildren()),
				getProcessedField(category, categoryContent.getAvailabilityRules()),
				getProcessedField(category, categoryContent.getPath()),
				getProcessedField(category, categoryContent.getParent()));

	}

	private <T> T getProcessedField(final ProjectionEntity entity, final T field) {
		return entity.isDeleted()
				? null
				: field;
	}
}
