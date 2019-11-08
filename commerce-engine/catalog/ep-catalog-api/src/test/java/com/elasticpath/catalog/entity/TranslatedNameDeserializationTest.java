/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests {@link TranslatedName}.
 */
public class TranslatedNameDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoTranslatedNameTest() throws IOException {
		final String jsonString =
				"{\"value\":\"value\","
						+ "\"displayValue\":\"displayValue\"}";
		final TranslatedName translatedName = getObjectMapper().readValue(jsonString, TranslatedName.class);
		assertThat(translatedName.getDisplayValue()).isEqualTo("displayValue");
		assertThat(translatedName.getValue()).isEqualTo("value");
	}
}
