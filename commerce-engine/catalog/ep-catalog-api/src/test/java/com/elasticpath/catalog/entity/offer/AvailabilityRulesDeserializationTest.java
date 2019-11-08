/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.elasticpath.catalog.entity.AvailabilityRules;
import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link AvailabilityRules}.
 */
public class AvailabilityRulesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoAvailabilityRules() throws IOException {
		final String dateTime = "2019-03-15T09:57:11.234+03:00";
		final String jsonString =
				"{\"enableDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"disableDateTime\":\"2019-03-15T09:57:11.234+03:00\"}}";
		final AvailabilityRules rules = getObjectMapper().readValue(jsonString, AvailabilityRules.class);
		assertThat(rules.getEnableDateTime()).isEqualTo(ZonedDateTime.parse(dateTime, ISO_ZONED_DATE_TIME));
		assertThat(rules.getDisableDateTime()).isEqualTo(ZonedDateTime.parse(dateTime, ISO_ZONED_DATE_TIME));
	}
}
