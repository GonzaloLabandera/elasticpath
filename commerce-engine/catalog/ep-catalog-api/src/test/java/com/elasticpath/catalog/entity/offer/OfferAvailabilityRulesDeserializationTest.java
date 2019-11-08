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
 * Tests {@link OfferAvailabilityRules}.
 */
public class OfferAvailabilityRulesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeOfferAvailabilityRules() throws IOException {
		final String jsonString =
				"{\"canDiscover\":[],"
						+ "\"canView\":[],"
						+ "\"canAddToCart\":[],"
						+ "\"releaseDateTime\" :\"2019-03-15T09:57:11.234+03:00\","
						+ "\"enableDateTime\":null,"
						+ "\"disableDateTime\":null}";
		final OfferAvailabilityRules offerRules = getObjectMapper().readValue(jsonString, OfferAvailabilityRules.class);
		assertThat(offerRules.getCanDiscover()).isNotNull();
		assertThat(offerRules.getCanView()).isNotNull();
		assertThat(offerRules.getCanAddToCart()).isNotNull();
		assertThat(offerRules.getReleaseDateTime()).isEqualTo(ZonedDateTime.parse("2019-03-15T09:57:11.234+03:00", ISO_ZONED_DATE_TIME));
	}
}
