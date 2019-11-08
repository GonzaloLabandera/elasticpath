/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link CategoryProperties}.
 */
public class CategoryPropertiesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferProperties() throws IOException {
		final String jsonString =
				"{\"projectionProperties\":{},"
						+ "\"categorySpecificProperties\":[]}";
		final CategoryProperties categoryProperties = getObjectMapper().readValue(jsonString, CategoryProperties.class);
		assertThat(categoryProperties.getProjectionProperties()).isNotNull();
		assertThat(categoryProperties.getCategorySpecificProperties()).isNotNull();
	}
}
