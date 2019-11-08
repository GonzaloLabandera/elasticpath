/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.ATTRIBUTE_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.attribute.Attribute;
import com.elasticpath.catalog.plugin.converter.impl.EntityToAttributeConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;

/**
 * Test for {@link EntityToAttributeConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityToAttributeConverterTest {
	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoSkuOptionProjectionWithFilledContent() throws JsonProcessingException {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(ATTRIBUTE_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"translations\":[{\"language\":\"en\",\"displayName\":\"displayName\",\"dataType\":\"String\",\"multiValue\":true},"
				+ "{\"language\":\"fr\",\"displayName\":\"attr1\",\"dataType\":\"Char\",\"multiValue\":false}]}");
		entity.setSchemaVersion("1.1");
		entity.setContentHash("hash");
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToAttributeConverter converter = new EntityToAttributeConverter(objectMapper);
		Attribute attribute = converter.convert(entity);

		assertThat(attribute).isNotNull();
		assertThat(attribute.getModifiedDateTime()).isEqualTo(ZonedDateTime.ofInstant(new Date(1).toInstant(), ZoneId.of("GMT")));
		assertFalse(attribute.isDeleted());
		assertThat(attribute.getIdentity().getCode()).isEqualTo("code");
		assertThat(attribute.getIdentity().getStore()).isEqualTo("store");
		assertThat(attribute.getIdentity().getType()).isEqualTo(ATTRIBUTE_IDENTITY_TYPE);
		assertThat(attribute.getTranslations().size()).isEqualTo(2);
		assertThat(attribute.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(attribute.getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(attribute.getTranslations().get(0).getDataType()).isEqualTo("String");
		assertThat(attribute.getTranslations().get(0).getMultiValue()).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoSkuOptionProjectionWithEmptyContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(ATTRIBUTE_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent(null);
		entity.setSchemaVersion(null);
		entity.setContentHash(null);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToAttributeConverter converter = new EntityToAttributeConverter(objectMapper);
		Attribute attribute = converter.convert(entity);

		assertThat(attribute.getTranslations()).isEmpty();
	}
}
