/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link ItemTranslation}.
 */
public class ItemTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoItemTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"language\","
						+ "\"details\":[],"
						+ "\"options\":[]}";
		final ItemTranslation itemTranslation = getObjectMapper().readValue(jsonString, ItemTranslation.class);
		assertThat(itemTranslation.getLanguage()).isEqualTo("language");
		assertThat(itemTranslation.getDetails()).isNotNull();
		assertThat(itemTranslation.getOptions()).isNotNull();
	}
}
