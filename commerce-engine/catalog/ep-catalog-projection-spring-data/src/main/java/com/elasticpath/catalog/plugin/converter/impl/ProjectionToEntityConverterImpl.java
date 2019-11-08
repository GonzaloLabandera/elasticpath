/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter.impl;

import static com.elasticpath.catalog.plugin.exception.ConverterException.CONVERTER_EXCEPTION_MESSAGE;
import static com.elasticpath.catalog.plugin.exception.ConverterException.NO_CONVERTER;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.view.ProjectionView;
import com.elasticpath.catalog.plugin.converter.EntityToProjectionConverter;
import com.elasticpath.catalog.plugin.converter.ProjectionToEntityConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionHistoryId;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.plugin.exception.ConverterException;
import com.elasticpath.catalog.validator.Validator;


/**
 * Abstract converter, that's sets common projection field.
 */
public class ProjectionToEntityConverterImpl implements ProjectionToEntityConverter {

	private final Map<String, Validator<String>> validators;
	private final Map<String, EntityToProjectionConverter<? extends Projection>> converters;
	private final ObjectMapper objectMapper;
	private final String schemaVersion;

	/**
	 * Constructor.
	 *
	 * @param validators    validators for {@link Projection}
	 * @param converters    converters for {@link Projection}
	 * @param objectMapper  singleton object mapper.
	 * @param schemaVersion json schema version.
	 */
	public ProjectionToEntityConverterImpl(final Map<String, Validator<String>> validators,
					       final Map<String, EntityToProjectionConverter<? extends Projection>> converters,
					       final ObjectMapper objectMapper,
					       final String schemaVersion) {
		this.validators = validators;
		this.converters = converters;
		this.objectMapper = objectMapper;
		this.schemaVersion = schemaVersion;
	}

	@Override
	public ProjectionEntity convertFromProjection(final Projection source) {
		return source.isDeleted()
				? createAsDeleted(source)
				: createNotDeleted(source);
	}

	@Override
	public ProjectionHistoryEntity convertToProjectionHistory(final ProjectionEntity source) {
		final ProjectionHistoryId historyId = new ProjectionHistoryId();
		historyId.setVersion(source.getVersion());
		historyId.setType(source.getType());
		historyId.setStore(source.getStore());
		historyId.setCode(source.getCode());

		final ProjectionHistoryEntity historyEntity = new ProjectionHistoryEntity();
		historyEntity.setHistoryId(historyId);
		historyEntity.setProjectionDateTime(source.getProjectionDateTime());
		historyEntity.setSchemaVersion(source.getSchemaVersion());
		historyEntity.setContent(source.getContent());
		historyEntity.setContentHash(source.getContentHash());
		historyEntity.setDeleted(source.isDeleted());
		return historyEntity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Projection> T convertToProjection(final ProjectionEntity entity) {
		return (T) Optional.ofNullable(converters.get(entity.getType()))
				.map(converter -> converter.convert(entity))
				.orElseThrow(() -> new ConverterException(NO_CONVERTER + entity.getType()));
	}

	@Override
	public ProjectionEntity convertToDeletedEntity(final ProjectionEntity projectionEntity, final Date time) {
		projectionEntity.setProjectionDateTime(time);
		setFieldsAsDeleted(projectionEntity);
		return projectionEntity;
	}

	private ProjectionEntity createNotDeleted(final Projection source) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setCode(source.getIdentity().getCode());
		projectionId.setType(source.getIdentity().getType());
		projectionId.setStore(source.getIdentity().getStore());

		final ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setProjectionDateTime(convertTime(source.getModifiedDateTime()));
		entity.setDisableDateTime(convertTime(source.getDisableDateTime()));

		validate(source);

		final String jsonContent;
		try {
			jsonContent = objectMapper
					.writerWithView(ProjectionView.ContentOnly.class)
					.writeValueAsString(source);
		} catch (JsonProcessingException e) {
			throw new ConverterException(CONVERTER_EXCEPTION_MESSAGE + source, e);
		}
		entity.setContentHash(DigestUtils.sha256Hex(jsonContent));
		entity.setContent(jsonContent);
		entity.setSchemaVersion(schemaVersion);

		return entity;
	}

	private void setFieldsAsDeleted(final ProjectionEntity projectionEntity) {
		projectionEntity.setDeleted(true);
		projectionEntity.setSchemaVersion(null);
		projectionEntity.setContentHash(null);
		projectionEntity.setContent(null);
		projectionEntity.setDisableDateTime(null);
	}

	private ProjectionEntity createAsDeleted(final Projection source) {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setCode(source.getIdentity().getCode());
		projectionId.setType(source.getIdentity().getType());
		projectionId.setStore(source.getIdentity().getStore());

		final ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setProjectionDateTime(Date.from(source.getModifiedDateTime().toInstant()));
		setFieldsAsDeleted(entity);
		return entity;
	}

	private void validate(final Projection projection) {
		try {
			final String jsonProjection = objectMapper.writeValueAsString(projection);

			Optional.ofNullable(validators.get(projection.getIdentity().getType())).ifPresent(validator -> validator.validate(jsonProjection));
		} catch (JsonProcessingException e) {
			throw new ConverterException(CONVERTER_EXCEPTION_MESSAGE + projection, e);
		}
	}

	private Date convertTime(final ZonedDateTime time) {
		return Optional.ofNullable(time)
				.map(ZonedDateTime::toInstant)
				.map(Date::from).orElse(null);
	}
}
