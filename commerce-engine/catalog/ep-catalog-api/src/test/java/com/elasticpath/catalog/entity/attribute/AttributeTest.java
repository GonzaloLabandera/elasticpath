/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.entity.attribute;

import static com.elasticpath.catalog.entity.constants.ProjectionSchemaPath.ATTRIBUTE_SCHEMA_JSON;
import static org.assertj.core.api.Java6Assertions.assertThatCode;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.translation.AttributeTranslation;
import com.elasticpath.catalog.validator.impl.ProjectionValidator;

/**
 * Class for testing Attribute model.
 */

public class AttributeTest extends BaseSetUp {

	private static final String ATTRIBUTE_NAME = "attributeName";
	private static final String STORE_CODE = "storeCode";

	/**
	 * Test for ensure that Attribute json corresponds to schema.
	 *
	 * @throws JsonProcessingException when Attribute cannot serialize to JSON.
	 */
	@Test
	public void testThatAttributeProjectionCorrectTranslatedIntoRequiredJson() throws JsonProcessingException {
		List<AttributeTranslation> translations = new ArrayList<>();
		translations.add(new AttributeTranslation("en", "Fabric", "ShortText", false));
		translations.add(new AttributeTranslation("fr", "Tissue", "ShortText", false));
		final Attribute attributeProjection = new Attribute(ATTRIBUTE_NAME, STORE_CODE, translations, ZonedDateTime.now(), false);

		final String json = getObjectMapper().writeValueAsString(attributeProjection);
		assertThatCode(() -> new ProjectionValidator(ATTRIBUTE_SCHEMA_JSON)
				.validate(json)).doesNotThrowAnyException();
	}
}
