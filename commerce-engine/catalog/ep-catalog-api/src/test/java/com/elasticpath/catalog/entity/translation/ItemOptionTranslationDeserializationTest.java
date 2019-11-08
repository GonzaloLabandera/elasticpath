/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link ItemOptionTranslation}.
 */
public class ItemOptionTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoItemOptionTranslation() throws IOException {
		final String jsonString =
				"{\"name\":\"name\","
						+ "\"displayName\":\"displayName\","
						+ "\"displayValue\":\"displayValue\","
						+ "\"value\":\"value\"}";
		final ItemOptionTranslation translation = getObjectMapper().readValue(jsonString, ItemOptionTranslation.class);
		assertThat(translation.getName()).isEqualTo("name");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getDisplayValue()).isEqualTo("displayValue");
		assertThat(translation.getValue()).isEqualTo("value");
	}
}
