/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.CATEGORY_IDENTITY_TYPE;
import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.CATEGORY_SCHEMA_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.ZonedDateTime;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.category.Category;
import com.elasticpath.catalog.plugin.converter.impl.EntityToCategoryConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Test for {@link EntityToCategoryConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityToCategoryConverterTest {

	private static final String SCHEMA_VERSION = "1.1";
	private static final String STORE = "store";
	private static final String CODE = "code";
	private static final String ZONED_DATE_TIME_START = "2019-01-09T23:12:00.000+03:00";
	private static final String ZONED_DATE_TIME_END = "2019-02-09T23:12:00.000+03:00";
	private static final String PARENT_CODE = "parent";
	private static final String CONTENT_HASH = "hash";
	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoCategoryProjectionWithFilledContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(STORE);
		projectionId.setCode(CODE);
		projectionId.setType(CATEGORY_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"children\":["
				+ "\"<child1Code>\",\"<child2Code>\"],\"translations\":[{\"language\":\"en\","
				+ "\"displayName\":\"<categoryName>\",\"details\":[{\"displayName\":\"ImageGallery\",\"displayValues\":["
				+ "\"<image1Path>\",\"<image2Path>\"],\"name\":\"CATEGORY_IMAGES\",\"values\":[\"<image1Path>\","
				+ "\"<image2Path>\"]}]}],\"extensions\":3}");
		entity.setSchemaVersion("1.1");
		entity.setContentHash(CONTENT_HASH);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));


		final EntityToCategoryConverter converter = new EntityToCategoryConverter(objectMapper);
		Category category = converter.convert(entity);

		assertThat(category.getIdentity().getType()).isEqualTo(CATEGORY_IDENTITY_TYPE);
		assertThat(category.getIdentity().getCode()).isEqualTo(CODE);
		assertThat(category.getIdentity().getStore()).isEqualTo(STORE);
		assertThat(category.getModifiedDateTime()).isNotNull();

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatTombstoneCategoryProjectionJsonCorrespondsToSchema() throws JsonProcessingException {
		final ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(STORE);
		projectionId.setCode(CODE);
		projectionId.setType(CATEGORY_IDENTITY_TYPE);
		final ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setDeleted(true);
		entity.setProjectionDateTime(new Date(1));
		final EntityToCategoryConverter converter = new EntityToCategoryConverter(objectMapper);
		Category category = converter.convert(entity);
		final String categoryJson = objectMapper.writeValueAsString(category);

		assertThatCode(() -> new ProjectionValidator(CATEGORY_SCHEMA_JSON)
				.validate(categoryJson)).doesNotThrowAnyException();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoCategoryProjectionWithFilledAvailabilityRules() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(STORE);
		projectionId.setCode(CODE);
		projectionId.setType(CATEGORY_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"availabilityRules\": {\"enableDateTime\": \"" + ZONED_DATE_TIME_START + "\","
				+ "\"disableDateTime\": \"" + ZONED_DATE_TIME_END + "\"}}");
		entity.setSchemaVersion(SCHEMA_VERSION);
		entity.setContentHash(CONTENT_HASH);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToCategoryConverter converter = new EntityToCategoryConverter(objectMapper);
		Category category = converter.convert(entity);

		assertThat(category).isNotNull();
		assertThat(category.getAvailabilityRules().getDisableDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME_END));
		assertThat(category.getAvailabilityRules().getEnableDateTime()).isEqualTo(ZonedDateTime.parse(ZONED_DATE_TIME_START));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoCategoryProjectionWithFilledParent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(STORE);
		projectionId.setCode(CODE);
		projectionId.setType(CATEGORY_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"parent\": \"" + PARENT_CODE + "\"}");
		entity.setSchemaVersion(SCHEMA_VERSION);
		entity.setContentHash(CONTENT_HASH);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToCategoryConverter converter = new EntityToCategoryConverter(objectMapper);
		Category category = converter.convert(entity);

		assertThat(category).isNotNull();
		assertThat(category.getParent()).isEqualTo(PARENT_CODE);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoCategoryProjectionWithFilledPath() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore(STORE);
		projectionId.setCode(CODE);
		projectionId.setType(CATEGORY_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"path\":[\"a1\",\"a2\",\"a3\"]}");
		entity.setSchemaVersion(SCHEMA_VERSION);
		entity.setContentHash(CONTENT_HASH);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToCategoryConverter converter = new EntityToCategoryConverter(objectMapper);
		Category category = converter.convert(entity);

		assertThat(category).isNotNull();
		assertThat(category.getPath()).containsExactly("a1", "a2", "a3");
	}

	@Test
	public void selectionRulesAreParsedByConverter() {
		final ProjectionEntity projectionEntity = createProjectionEntityWithContent("{\"properties\":[{\"name\":\"CATEGORY_TYPE\","
				+ "\"value\":\"valueCategory\"}]}");
		final EntityToCategoryConverter converter = new EntityToCategoryConverter(objectMapper);
		final Category category = converter.convert(projectionEntity);

		assertThat(category.getProperties().size()).isEqualTo(1);
		assertThat(category.getProperties().get(0).getName()).isEqualTo("CATEGORY_TYPE");
		assertThat(category.getProperties().get(0).getValue()).isEqualTo("valueCategory");
	}

	private ProjectionEntity createProjectionEntityWithContent(final String content) {
		final ProjectionEntity projectionEntity = new ProjectionEntity();
		projectionEntity.setProjectionId(new ProjectionId());
		projectionEntity.setContent(content);
		projectionEntity.setSchemaVersion(SCHEMA_VERSION);
		projectionEntity.setProjectionDateTime(new Date());

		return projectionEntity;
	}
}
