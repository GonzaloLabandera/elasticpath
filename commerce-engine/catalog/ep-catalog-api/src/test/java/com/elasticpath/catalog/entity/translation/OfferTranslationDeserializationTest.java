/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OfferTranslation}.
 */
public class OfferTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferTranslation() throws IOException {
		final String jsonString =
				"{\"language\":\"language\","
						+ "\"displayName\":\"displayName\","
						+ "\"brand\":{},"
						+ "\"options\":[],"
						+ "\"details\":[]}";
		final OfferTranslation translation = getObjectMapper().readValue(jsonString, OfferTranslation.class);
		assertThat(translation.getBrand()).isNotNull();
		assertThat(translation.getOptions()).isNotNull();
		assertThat(translation.getDetails()).isNotNull();
		assertThat(translation.getLanguage()).isEqualTo("language");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
	}
}
