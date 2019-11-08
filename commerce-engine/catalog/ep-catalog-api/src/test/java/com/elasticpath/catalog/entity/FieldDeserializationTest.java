/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests {@link Field}.
 */
public class FieldDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoField() throws IOException {
		final String jsonString =
				"{\"name\":\"name\","
						+ "\"displayName\":\"displayName\","
						+ "\"dataType\":\"dataType\","
						+ "\"required\":false,"
						+ "\"maxSize\":1,"
						+ "\"fieldValues\":[]}";
		final Field field = getObjectMapper().readValue(jsonString, Field.class);
		assertThat(field.getName()).isEqualTo("name");
		assertThat(field.getDisplayName()).isEqualTo("displayName");
		assertThat(field.getDataType()).isEqualTo("dataType");
		assertThat(field.isRequired()).isFalse();
		assertThat(field.getMaxSize()).isEqualTo(1);
		assertThat(field.getFieldValues()).isNotNull();
	}
}
