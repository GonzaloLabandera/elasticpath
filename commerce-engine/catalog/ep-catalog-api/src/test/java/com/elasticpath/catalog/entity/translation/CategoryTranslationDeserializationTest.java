/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link CategoryTranslation}.
 */
public class CategoryTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferAttributeTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"language\","
						+ "\"displayName\":\"displayName\","
						+ "\"details\":[]}";
		final CategoryTranslation translation = getObjectMapper().readValue(jsonString, CategoryTranslation.class);
		assertThat(translation.getLanguage()).isEqualTo("language");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getDetails()).isNotNull();
	}
}
