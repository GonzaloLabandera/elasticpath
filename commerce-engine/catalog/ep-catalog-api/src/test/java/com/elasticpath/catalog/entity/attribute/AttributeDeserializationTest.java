/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.attribute;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Attribute}.
 */
public class AttributeDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoAttribute() throws IOException {
		final String jsonString =
				"{\"code\":\"231\","
						+ "\"store\":\"storeName\","
						+ "\"translations\":[{\"language\": \"en\",\"displayName\":\"string\",\"dataType\":\"dataType\", \"multiValue\":false}],"
						+ "\"modifiedDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"deleted\":false}";
		final Attribute attribute = getObjectMapper().readValue(jsonString, Attribute.class);
		assertThat(attribute.getIdentity().getCode()).isEqualTo("231");
		assertThat(attribute.getIdentity().getStore()).isEqualTo("storeName");
		assertThat(attribute.getTranslations().size()).isEqualTo(1);
		assertThat(attribute.getModifiedDateTime()).isEqualTo(ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME));
		assertThat(attribute.isDeleted()).isEqualTo(false);
	}
}
