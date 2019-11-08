/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.entity.brand;

import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.BRAND_SCHEMA_JSON;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.translation.Translation;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Tests {@link Brand}.
 */
public class BrandTest extends BaseSetUp {

	private static final String BRAND_PROJECTION = "PROJECTION";
	private static final String STORE_CODE = "Store 123";
	private static final List<Translation> TRANSLATIONS = Stream.of(new Translation("en", "Tim Hortons"),
			new Translation("fr", "Chez Tim Horton"))
			.collect(Collectors.toList());

	/**
	 * Test for ensure that Brand json corresponds to schema.
	 *
	 * @throws JsonProcessingException if wrong json format is a directory.
	 */
	@Test
	public void testThatBrandProjectionJsonCorrespondsToSchema() throws JsonProcessingException {

		Brand brandProjection = new Brand(BRAND_PROJECTION, STORE_CODE, TRANSLATIONS, ZonedDateTime.now(), false);

		String json = getObjectMapper().writeValueAsString(brandProjection);

		assertThatCode(() -> new ProjectionValidator(BRAND_SCHEMA_JSON)
				.validate(json)).doesNotThrowAnyException();
	}
}