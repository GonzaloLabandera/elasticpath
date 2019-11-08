/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

/**
 * Tests {@link ProjectionProperties}.
 */
public class ProjectionPropertiesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoProjectionProperties() throws IOException {
		final String jsonString =
				"{\"code\":\"code\","
						+ "\"store\":\"store\","
						+ "\"modifiedDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"deleted\":false}";
		final ProjectionProperties properties = getObjectMapper().readValue(jsonString, ProjectionProperties.class);
		assertThat(properties.getCode()).isEqualTo("code");
		assertThat(properties.getStore()).isEqualTo("store");
		assertThat(properties.getModifiedDateTime()).isEqualTo(ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME));
		assertThat(properties.isDeleted()).isFalse();
	}
}
