/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.brand;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Brand}.
 */
public class BrandDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoBrand() throws IOException {
		final String jsonString =
				"{\"code\":\"231\","
						+ "\"store\":\"storeName\","
						+ "\"translations\":[{\"language\": \"en\",\"displayName\":\"string\"}],"
						+ "\"modifiedDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"deleted\":false}";
		final Brand brand = getObjectMapper().readValue(jsonString, Brand.class);
		assertThat(brand.getIdentity().getCode()).isEqualTo("231");
		assertThat(brand.getIdentity().getStore()).isEqualTo("storeName");
		assertThat(brand.getTranslations().size()).isEqualTo(1);
		assertThat(brand.getModifiedDateTime()).isEqualTo(ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME));
		assertThat(brand.isDeleted()).isEqualTo(false);
	}
}
