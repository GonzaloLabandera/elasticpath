/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OfferProperties}.
 */
public class OfferPropertiesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOfferProperties() throws IOException {
		final String jsonString =
				"{\"projectionProperties\":{},"
						+ "\"offerSpecificProperties\":[]}";
		final OfferProperties properties = getObjectMapper().readValue(jsonString, OfferProperties.class);
		assertThat(properties.getProjectionProperties()).isNotNull();
		assertThat(properties.getOfferSpecificProperties()).isNotNull();
	}
}
