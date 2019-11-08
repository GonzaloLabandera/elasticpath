/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OfferCategories}.
 */
public class OfferCategoriesDeserializationTest extends BaseSetUp {
	@Test
	public void testThatJsonCorrectDeserializeOfferCategories() throws IOException {
		final ZonedDateTime dateTime = ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME);
		final String jsonString =
				"{\"code\":\"code\","
						+ "\"path\":[],"
						+ "\"default\":true,"
						+ "\"enableDateTime\" :\"2019-03-15T09:57:11.234+03:00\","
						+ "\"disableDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"featured\":2}";
		final OfferCategories offerCategories = getObjectMapper().readValue(jsonString, OfferCategories.class);
		assertThat(offerCategories.getCode()).isEqualTo("code");
		assertThat(offerCategories.getPath()).isNotNull();
		assertThat(offerCategories.getFeatured()).isEqualTo(2);
		assertThat(offerCategories.isDefaultCategory()).isTrue();
		assertThat(offerCategories.getEnableDateTime()).isEqualTo(dateTime);
		assertThat(offerCategories.getDisableDateTime()).isEqualTo(dateTime);
	}
}
