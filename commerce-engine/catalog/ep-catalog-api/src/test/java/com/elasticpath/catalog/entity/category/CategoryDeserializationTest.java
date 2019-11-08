/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Category}.
 */
public class CategoryDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoCategory() throws IOException {
		final String jsonString =
				"{\"categoryProperties\":{\"projectionProperties\":{},\"categorySpecificProperties\":[]},"
						+ "\"extensions\":{},"
						+ "\"children\":[\"child\"],"
						+ "\"translations\":[{\"language\":\"language\",\"displayName\":\"displayName\",\"details\":[]}]}";
		final Category category = getObjectMapper().readValue(jsonString, Category.class);
		assertThat(category.getProperties()).isNotNull();
		assertThat(category.getExtensions()).isNotNull();
		assertThat(category.getChildren().size()).isEqualTo(1);
		assertThat(category.getChildren().get(0)).isEqualTo("child");
		assertThat(category.getTranslations().size()).isEqualTo(1);
	}
}
