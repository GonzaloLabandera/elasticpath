/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OptionTranslation}.
 */
public class OptionTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOptionTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"language\","
						+ "\"displayName\":\"displayName\","
						+ "\"optionValues\":[]}";
		final OptionTranslation translation = getObjectMapper().readValue(jsonString, OptionTranslation.class);
		assertThat(translation.getLanguage()).isEqualTo("language");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getOptionValues()).isNotNull();
	}
}
