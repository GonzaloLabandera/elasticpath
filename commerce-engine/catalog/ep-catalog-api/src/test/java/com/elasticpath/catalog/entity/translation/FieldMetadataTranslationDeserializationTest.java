/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link FieldMetadataTranslation}.
 */
public class FieldMetadataTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoFieldMetadataTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"en\","
						+ "\"displayName\":\"displayName\","
						+ "\"fields\":[]}";
		final FieldMetadataTranslation translation = getObjectMapper().readValue(jsonString, FieldMetadataTranslation.class);
		assertThat(translation.getLanguage()).isEqualTo("en");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getFields()).isNotNull();
	}
}
