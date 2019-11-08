/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.elasticpath.catalog.entity.ProjectionProperties;
import com.elasticpath.catalog.entity.offer.Offer;
import com.elasticpath.catalog.entity.offer.OfferProperties;
import com.elasticpath.catalog.entity.offer.OfferRules;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;

/**
 * Converter to convert {@link ProjectionEntity} to {@link Offer}.
 */
public class EntityToOfferConverter extends EntityToProjectionConverterBase implements EntityToProjectionConverter<Offer> {

	/**
	 * Constructor.
	 *
	 * @param objectMapper singleton object mapper.
	 */
	public EntityToOfferConverter(final ObjectMapper objectMapper) {
		super(objectMapper);
	}
	@Override
	public Offer convert(final ProjectionEntity entity) {
		final OfferContent offerContent = Optional.ofNullable(entity.getContent())
				.map(content -> readFromJson(entity, OfferContent.class))
				.orElse(new OfferContent());

		final ProjectionProperties projectionProperties = new ProjectionProperties(entity.getCode(), entity.getStore(),
				ZonedDateTime.ofInstant(entity.getProjectionDateTime().toInstant(), ZoneId.of("GMT")), entity.isDeleted());

		return new Offer(new OfferProperties(projectionProperties, getProcessedField(entity, offerContent.getProperties())),
				getProcessedField(entity, offerContent.getItems()),
				offerContent.getExtensions(),
				getProcessedField(entity, offerContent.getAssociations()),
				offerContent.getComponents(),
				new OfferRules(offerContent.getAvailabilityRules(), offerContent.getSelectionRules()),
				getProcessedField(entity, offerContent.getFormFields()),
				getProcessedField(entity, offerContent.getTranslations()),
				getProcessedField(entity, offerContent.getCategories()));
	}

	private <T> T getProcessedField(final ProjectionEntity entity, final T field) {
		return entity.isDeleted()
				? null
				: field;
	}
}
