/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link AttributeTranslation}.
 */
public class AttributeTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoAttributeTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"en\","
						+ "\"displayName\":\"displayName\","
						+ "\"dataType\":\"dataType\","
						+ "\"multiValue\":false}";
		final AttributeTranslation translation = getObjectMapper().readValue(jsonString, AttributeTranslation.class);
		assertThat(translation.getLanguage()).isEqualTo("en");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getDataType()).isEqualTo("dataType");
		assertThat(translation.getMultiValue()).isFalse();
	}
}
