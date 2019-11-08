/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link CategoryDetailsTranslation}.
 */
public class CategoryDetailsTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferAttributeTranslation() throws IOException {
		final String jsonString =
				"{\"name\":\"name\","
						+ "\"displayName\":\"displayName\","
						+ "\"displayValues\":[\"displayValue\"],"
						+ "\"values\":[\"value\"]}";
		final CategoryDetailsTranslation translation = getObjectMapper().readValue(jsonString, CategoryDetailsTranslation.class);
		assertThat(translation.getName()).isEqualTo("name");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getDisplayValues().get(0)).isEqualTo("displayValue");
		assertThat(translation.getDisplayValues().size()).isEqualTo(1);
		assertThat(translation.getValues().get(0)).isEqualTo("value");
		assertThat(translation.getValues().size()).isEqualTo(1);
	}
}
