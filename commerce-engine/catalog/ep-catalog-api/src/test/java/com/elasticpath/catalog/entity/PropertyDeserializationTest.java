/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests {@link Property}.
 */
public class PropertyDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoProperty() throws IOException {
		final String jsonString =
				"{\"name\":\"name\","
						+ "\"value\":\"value\"}";
		final Property property = getObjectMapper().readValue(jsonString, Property.class);
		assertThat(property.getName()).isEqualTo("name");
		assertThat(property.getValue()).isEqualTo("value");
	}
}
