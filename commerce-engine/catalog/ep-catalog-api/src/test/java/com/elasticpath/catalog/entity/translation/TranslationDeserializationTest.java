/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Translation}.
 */
public class TranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"language\","
						+ "\"displayName\":\"displayName\"}";
		final Translation translation = getObjectMapper().readValue(jsonString, Translation.class);
		assertThat(translation.getLanguage()).isEqualTo("language");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
	}
}
