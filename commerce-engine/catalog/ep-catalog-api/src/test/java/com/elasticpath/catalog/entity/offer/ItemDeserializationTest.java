/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Item}.
 */
public class ItemDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoItem() throws IOException {
		final String jsonString =
				"{\"itemCode\":\"itemCode\","
						+ "\"extensions\":{},"
						+ "\"properties\":[{\"name\":\"name\",\"value\":\"value\"}],"
						+ "\"availabilityRules\":{},"
						+ "\"shippingProperties\":{},"
						+ "\"translations\":[{\"language\":\"language\", \"details\":[], \"options\":[]}]}";
		final Item item = getObjectMapper().readValue(jsonString, Item.class);
		assertThat(item.getItemCode()).isEqualTo("itemCode");
		assertThat(item.getExtensions()).isNotNull();
		assertThat(item.getProperties().size()).isEqualTo(1);
		assertThat(item.getAvailabilityRules()).isNotNull();
		assertThat(item.getShippingProperties()).isNotNull();
		assertThat(item.getTranslations().size()).isEqualTo(1);
	}
}
