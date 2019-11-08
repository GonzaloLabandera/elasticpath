/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.FIELD_METADATA_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.fieldmetadata.FieldMetadata;
import com.elasticpath.catalog.plugin.converter.impl.EntityToFieldMetadataConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;

/**
 * Test for {@link EntityToFieldMetadataConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityToFieldMetadataConverterTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoFieldMetadataProjectionWithFilledContent() {
		final int maxSize = 5;

		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(FIELD_METADATA_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"translations\":[{\"language\":\"en\",\"displayName\":\"displayName\",\"fields\":[{\"name\":\"TestField\", "
				+ "\"displayName\":\"TestField\",\"dataType\":\"PickSingleOption\",\"required\":true,\"maxSize\":5, "
				+ "\"fieldValues\":[{\"value\":\"TestValue\",\"displayValue\":\"TestDisplayValue\"}]}]}]}");
		entity.setSchemaVersion("1.1");
		entity.setContentHash("hash");
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));

		final EntityToFieldMetadataConverter converter = new EntityToFieldMetadataConverter(objectMapper);
		FieldMetadata fieldMetadata = converter.convert(entity);

		assertThat(fieldMetadata).isNotNull();
		assertThat(fieldMetadata.getModifiedDateTime()).isEqualTo(ZonedDateTime.ofInstant(new Date(1).toInstant(), ZoneId.of("GMT")));
		assertFalse(fieldMetadata.isDeleted());
		assertThat(fieldMetadata.getIdentity().getCode()).isEqualTo("code");
		assertThat(fieldMetadata.getIdentity().getStore()).isEqualTo("store");
		assertThat(fieldMetadata.getIdentity().getType()).isEqualTo(FIELD_METADATA_IDENTITY_TYPE);
		assertThat(fieldMetadata.getTranslations().size()).isEqualTo(1);
		assertThat(fieldMetadata.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(fieldMetadata.getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(fieldMetadata.getTranslations().get(0).getFields().size()).isEqualTo(1);
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getDataType()).isEqualTo("PickSingleOption");
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getDisplayName()).isEqualTo("TestField");
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getMaxSize()).isEqualTo(maxSize);
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getName()).isEqualTo("TestField");
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).isRequired()).isTrue();
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getFieldValues().size()).isEqualTo(1);
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getFieldValues().get(0).getDisplayValue()).isEqualTo("TestDisplayValue");
		assertThat(fieldMetadata.getTranslations().get(0).getFields().get(0).getFieldValues().get(0).getValue()).isEqualTo("TestValue");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoFieldMetadataProjectionWithEmptyContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(FIELD_METADATA_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent(null);
		entity.setSchemaVersion(null);
		entity.setContentHash(null);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));

		final EntityToFieldMetadataConverter converter = new EntityToFieldMetadataConverter(objectMapper);
		FieldMetadata fieldMetadata = converter.convert(entity);

		assertThat(fieldMetadata.getTranslations()).isEmpty();
	}
}
