/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OfferRules}.
 */
public class OfferRulesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferRules() throws IOException {
		final String jsonString =
				"{\"availabilityRules\":{},"
						+ "\"selectionRules\":{}}";
		final OfferRules offerRules = getObjectMapper().readValue(jsonString, OfferRules.class);
		assertThat(offerRules.getAvailabilityRules()).isNotNull();
		assertThat(offerRules.getSelectionRules()).isNotNull();
	}
}
