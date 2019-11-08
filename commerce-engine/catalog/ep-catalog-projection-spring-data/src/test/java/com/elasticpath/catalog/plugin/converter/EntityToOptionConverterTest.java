/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.OPTION_IDENTITY_TYPE;
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

import com.elasticpath.catalog.entity.option.Option;
import com.elasticpath.catalog.plugin.converter.impl.EntityToOptionConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;

/**
 * Test for {@link EntityToOptionConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityToOptionConverterTest {

	private ObjectMapper objectMapper;

	@Before
	public void setup() {
		objectMapper = new ObjectMapper();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoSkuOptionProjectionWithFilledContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(OPTION_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"translations\":[{\"language\":\"en\",\"displayName\":\"displayName\",\"optionValues\":[{\"value\":\"value\","
				+ "\"displayValue\":\"displayValue\"}]},{\"language\":\"en_CA\",\"displayName\":\"v\",\"optionValues\":[{\"value\":\"vvv\","
				+ "\"displayValue\":\"vv\"}]}]}");
		entity.setSchemaVersion("1.1");
		entity.setContentHash("hash");
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToOptionConverter converter = new EntityToOptionConverter(objectMapper);
		Option option = converter.convert(entity);

		assertThat(option).isNotNull();
		assertThat(option.getModifiedDateTime()).isEqualTo(ZonedDateTime.ofInstant(new Date(1).toInstant(), ZoneId.of("GMT")));
		assertFalse(option.isDeleted());
		assertThat(option.getIdentity().getCode()).isEqualTo("code");
		assertThat(option.getIdentity().getStore()).isEqualTo("store");
		assertThat(option.getIdentity().getType()).isEqualTo(OPTION_IDENTITY_TYPE);
		assertThat(option.getTranslations().size()).isEqualTo(2);
		assertThat(option.getTranslations().get(0).getLanguage()).isEqualTo("en");
		assertThat(option.getTranslations().get(0).getDisplayName()).isEqualTo("displayName");
		assertThat(option.getTranslations().get(0).getOptionValues().size()).isEqualTo(1);
		assertThat(option.getTranslations().get(0).getOptionValues().get(0).getDisplayValue()).isEqualTo("displayValue");
		assertThat(option.getTranslations().get(0).getOptionValues().get(0).getValue()).isEqualTo("value");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoSkuOptionProjectionWithEmptyContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(OPTION_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent(null);
		entity.setSchemaVersion(null);
		entity.setContentHash(null);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToOptionConverter converter = new EntityToOptionConverter(objectMapper);
		Option option = converter.convert(entity);

		assertThat(option.getTranslations()).isEmpty();
	}
}
