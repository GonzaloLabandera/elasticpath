/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.FIELD_METADATA_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.FIELD_METADATA_SCHEMA_JSON;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.Field;
import com.elasticpath.catalog.entity.Projection;
import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.entity.translation.FieldMetadataTranslation;
import com.elasticpath.catalog.entity.translation.OptionTranslation;
import com.elasticpath.catalog.plugin.converter.impl.EntityToOptionConverter;
import com.elasticpath.catalog.plugin.converter.impl.ProjectionToEntityConverterImpl;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.validator.Validator;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Test for {@link ProjectionToEntityConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectionToEntityConverterTest {

	public static final String CODE = "name";
	public static final String SCHEMA_VERSION = "1.0";
	public static final String STORE = "store";

	private ObjectMapper objectMapper;

	private EntityToOptionConverter optionConverter;

	@Before
	public void setup() {
		optionConverter = mock(EntityToOptionConverter.class);
		objectMapper = new ObjectMapper();
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		objectMapper.registerModule(javaTimeModule);
	}

	/**
	 * Given Option with all filled fields.
	 * Then method "convertFromProjection" converts Option to ProjectionEntity.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertSkuOptionProjectionWithFilledFieldsIntoProjectionEntity() {
		final List<OptionTranslation> translatedNames = new ArrayList<>();
		translatedNames.add(new OptionTranslation("name", "name_en", Collections.emptyList()));

		ZonedDateTime currentDate = ZonedDateTime.now();

		final Projection projection = new Option(CODE, "storeCode", translatedNames, currentDate, false);

		final Validator<String> validator = mock(Validator.class);
		doNothing().when(validator).validate(any());

		final ProjectionToEntityConverter converter = new ProjectionToEntityConverterImpl(Collections.singletonMap(OPTION_IDENTITY_TYPE, validator),
				Collections.singletonMap(OPTION_IDENTITY_TYPE, optionConverter),
				objectMapper, SCHEMA_VERSION);
		final ProjectionEntity entity = converter.convertFromProjection(projection);

		assertThat(entity).isNotNull();
		assertThat(entity.getType()).isEqualTo(projection.getIdentity().getType());
		assertThat(entity.getCode()).isEqualTo(CODE);
		assertThat(entity.getVersion()).isNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ensureThatMethodValidateIsCalledFromConverter() {
		final Validator<String> validator = mock(Validator.class);
		final ProjectionToEntityConverter converter = new ProjectionToEntityConverterImpl(Collections.singletonMap(OPTION_IDENTITY_TYPE, validator),
				Collections.singletonMap(OPTION_IDENTITY_TYPE, optionConverter),
				objectMapper,
				SCHEMA_VERSION);

		converter.convertFromProjection(new Option(CODE, STORE, Collections.emptyList(), ZonedDateTime.now(), false));

		verify(validator).validate(any());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ensureThatMethodConvertIsCalledFromConverter() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setType(OPTION_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);

		final EntityToProjectionConverter<Option> optionConverter = mock(EntityToProjectionConverter.class);
		when(optionConverter.convert(entity)).thenReturn(new Option(CODE, "storeCode", null, null, false));
		final Validator<String> validator = mock(Validator.class);
		final Map<String, EntityToProjectionConverter<? extends Projection>> converters = mock(Map.class);
		when(converters.get(OPTION_IDENTITY_TYPE)).thenReturn((EntityToProjectionConverter) optionConverter);

		final ProjectionToEntityConverter converter = new ProjectionToEntityConverterImpl(Collections.singletonMap(OPTION_IDENTITY_TYPE, validator),
				converters,
				objectMapper,
				SCHEMA_VERSION);

		converter.convertToProjection(entity);

		verify(optionConverter).convert(entity);
		verify(converters).get(OPTION_IDENTITY_TYPE);
	}

	@Test
	public void convertFieldMetadataShouldNotThrowAnyExceptionWhenFieldMaxSizeIsNull() {
		final Field field = new Field("field", "fieldDisplayName", "integer", false, null, new ArrayList<>());

		final FieldMetadataTranslation fieldMetadataTranslation = new FieldMetadataTranslation("en", "translationDisplayName",
				Collections.singletonList(field));
		FieldMetadata fieldMetadata = new FieldMetadata(CODE, "store", Collections.singletonList(fieldMetadataTranslation), ZonedDateTime.now(),
				false);

		final Validator<String> validator = new ProjectionValidator(FIELD_METADATA_SCHEMA_JSON);
		final ProjectionToEntityConverter converter = new ProjectionToEntityConverterImpl(
				Collections.singletonMap(FIELD_METADATA_IDENTITY_TYPE, validator),
				Collections.singletonMap(FIELD_METADATA_IDENTITY_TYPE, optionConverter),
				objectMapper,
				SCHEMA_VERSION);

		assertThatCode(() -> converter.convertFromProjection(fieldMetadata)).doesNotThrowAnyException();
	}

	@Test
	public void convertDeletedProjectionToDeletedProjectionEntity() {
		final Field field = new Field("field", "fieldDisplayName", "integer", false, null, new ArrayList<>());

		final FieldMetadataTranslation fieldMetadataTranslation = new FieldMetadataTranslation("en", "translationDisplayName",
				Collections.singletonList(field));
		FieldMetadata fieldMetadata = new FieldMetadata(CODE, "store", Collections.singletonList(fieldMetadataTranslation), ZonedDateTime.now(),
				true);

		final Validator<String> validator = new ProjectionValidator(FIELD_METADATA_SCHEMA_JSON);
		final ProjectionToEntityConverter converter = new ProjectionToEntityConverterImpl(Collections.singletonMap(FIELD_METADATA_IDENTITY_TYPE,
				validator),
				Collections.singletonMap(FIELD_METADATA_IDENTITY_TYPE, optionConverter),
				objectMapper,
				SCHEMA_VERSION);

		final ProjectionEntity deletedEntity = converter.convertFromProjection(fieldMetadata);

		assertTrue(deletedEntity.isDeleted());
		assertThat(deletedEntity.getSchemaVersion()).isNull();
		assertThat(deletedEntity.getContentHash()).isNull();
		assertThat(deletedEntity.getContent()).isNull();
	}

	@Test
	public void convertProjectionEntityToDeletedProjectionEntity() {
		final ProjectionId projectionId = new ProjectionId();
		final ProjectionEntity entity = new ProjectionEntity();
		entity.setDeleted(false);
		entity.setSchemaVersion("1");
		entity.setContentHash("hash");
		entity.setContent("content");
		entity.setProjectionId(projectionId);
		entity.setVersion(1L);

		final Validator<String> validator = new ProjectionValidator(FIELD_METADATA_SCHEMA_JSON);
		final ProjectionToEntityConverter converter = new ProjectionToEntityConverterImpl(Collections.singletonMap(FIELD_METADATA_IDENTITY_TYPE,
				validator),
				Collections.singletonMap(FIELD_METADATA_IDENTITY_TYPE, optionConverter),
				objectMapper,
				SCHEMA_VERSION);
		final Date modifiedDate = new Date();
		final ProjectionEntity deletedEntity = converter.convertToDeletedEntity(entity, modifiedDate);

		assertTrue(deletedEntity.isDeleted());
		assertThat(deletedEntity.getSchemaVersion()).isNull();
		assertThat(deletedEntity.getContentHash()).isNull();
		assertThat(deletedEntity.getContent()).isNull();
		assertThat(deletedEntity.getProjectionDateTime()).isEqualTo(modifiedDate);
		assertThat(deletedEntity.getProjectionId()).isEqualTo(projectionId);
		assertThat(deletedEntity.getVersion()).isEqualTo(1L);

	}
}
