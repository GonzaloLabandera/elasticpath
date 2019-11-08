/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OfferCategories}.
 */
public class OfferCategoriesSerializationTest extends BaseSetUp {

	@Test
	public void testOfferCategoriesProjectionShouldBeEqualsJsonStringWithCorrectOrderOfFields() throws IOException {
		final ZonedDateTime dateTime = ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME);
		final String jsonString = "{\"code\":\"code\",\"path\":[],\"enableDateTime\":\"2019-03-15T09:57:11.234+03:00\","
				+ "\"disableDateTime\":\"2019-03-15T09:57:11.234+03:00\",\"default\":false,\"featured\":2}";
		final OfferCategories offerCategories = new OfferCategories("code", Collections.emptyList(), dateTime, dateTime, false, 2);
		final String newJson = getObjectMapper().writeValueAsString(offerCategories);
		assertThat(newJson).isEqualTo(jsonString);
	}
}
