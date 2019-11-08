/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link DetailsTranslation}.
 */
public class OfferAttributeTranslationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferAttributeTranslation() throws IOException {
		final String jsonString =
				"{\"name\":\"name\","
						+ "\"displayName\":\"displayName\","
						+ "\"values\":[]}";
		final DetailsTranslation translation = getObjectMapper().readValue(jsonString, DetailsTranslation.class);
		assertThat(translation.getName()).isEqualTo("name");
		assertThat(translation.getDisplayName()).isEqualTo("displayName");
		assertThat(translation.getValues()).isNotNull();
	}
}
