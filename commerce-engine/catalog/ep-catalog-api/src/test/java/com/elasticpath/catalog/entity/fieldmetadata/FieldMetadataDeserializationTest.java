/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.fieldmetadata;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link FieldMetadata}.
 */
public class FieldMetadataDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoFieldMetadata() throws IOException {
		final String jsonString =
				"{\"code\":\"231\","
						+ "\"store\":\"storeName\","
						+ "\"translations\":[],"
						+ "\"modifiedDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"deleted\":false}";
		final FieldMetadata fieldMetadata = getObjectMapper().readValue(jsonString, FieldMetadata.class);
		assertThat(fieldMetadata.getIdentity().getCode()).isEqualTo("231");
		assertThat(fieldMetadata.getIdentity().getStore()).isEqualTo("storeName");
		assertThat(fieldMetadata.getTranslations()).isNotNull();
		assertThat(fieldMetadata.getModifiedDateTime()).isEqualTo(ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME));
		assertThat(fieldMetadata.isDeleted()).isEqualTo(false);
	}
}
