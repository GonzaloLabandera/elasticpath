/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests {@link FieldOption}.
 */
public class FieldOptionDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoFieldOption() throws IOException {
		final String jsonString =
				"{\"value\":\"value\","
						+ "\"displayValue\":\"displayValue\"}";
		final FieldOption fieldOption = getObjectMapper().readValue(jsonString, FieldOption.class);
		assertThat(fieldOption.getValue()).isEqualTo("value");
		assertThat(fieldOption.getDisplayValue()).isEqualTo("displayValue");
	}
}
