/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.converter;

import static com.elasticpath.catalog.entity.constants.ProjectionIdentityTypeNames.BRAND_IDENTITY_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.catalog.entity.brand.Brand;
import com.elasticpath.catalog.plugin.converter.impl.EntityToBrandConverter;
import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.catalog.plugin.entity.ProjectionId;

/**
 * Test for {@link EntityToBrandConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityToBrandConverterTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoBrandProjectionWithFilledContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(BRAND_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent("{\"translations\":[{\"language\":\"fr\",\"displayName\":\"testCode1\"},{\"language\":\"en\","
				+ "\"displayName\":\"testCode2\"}]}");
		entity.setSchemaVersion("1.1");
		entity.setContentHash("hash");
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToBrandConverter converter = new EntityToBrandConverter(objectMapper);
		Brand brand = converter.convert(entity);

		assertThat(brand).isNotNull();
		assertThat(brand.getModifiedDateTime()).isEqualTo(ZonedDateTime.ofInstant(new Date(1).toInstant(), ZoneId.of("GMT")));
		assertFalse(brand.isDeleted());
		assertThat(brand.getIdentity().getCode()).isEqualTo("code");
		assertThat(brand.getIdentity().getStore()).isEqualTo("store");
		assertThat(brand.getIdentity().getType()).isEqualTo(BRAND_IDENTITY_TYPE);
		assertThat(brand.getTranslations().size()).isEqualTo(2);
		assertThat(brand.getTranslations().get(0).getDisplayName()).isEqualTo("testCode1");
		assertThat(brand.getTranslations().get(1).getDisplayName()).isEqualTo("testCode2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThatConverterConvertProjectionEntityIntoBrandProjectionWithEmptyContent() {
		ProjectionId projectionId = new ProjectionId();
		projectionId.setStore("store");
		projectionId.setCode("code");
		projectionId.setType(BRAND_IDENTITY_TYPE);
		ProjectionEntity entity = new ProjectionEntity();
		entity.setProjectionId(projectionId);
		entity.setContent(null);
		entity.setSchemaVersion(null);
		entity.setContentHash(null);
		entity.setDeleted(false);
		entity.setProjectionDateTime(new Date(1));
		entity.setVersion(1L);

		final EntityToBrandConverter converter = new EntityToBrandConverter(objectMapper);
		Brand brand = converter.convert(entity);

		assertThat(brand.getTranslations()).isEmpty();
	}

}
