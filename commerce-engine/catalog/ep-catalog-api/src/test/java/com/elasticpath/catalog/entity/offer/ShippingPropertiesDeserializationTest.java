/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link ShippingProperties}.
 */
public class ShippingPropertiesDeserializationTest  extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoShippingProperties() throws IOException {
		final String value = "20.3";
		final String jsonString =
				"{\"weight\":\"20.3\","
						+ "\"length\":\"20.3\","
						+ "\"height\":\"20.3\","
						+ "\"unitsWeight\":\"unitsWeight\","
						+ "\"unitsLength\":\"unitsLength\","
						+ "\"width\":\"20.3\"}";
		final ShippingProperties properties = getObjectMapper().readValue(jsonString, ShippingProperties.class);
		assertThat(properties.getWeight()).isEqualTo(value);
		assertThat(properties.getWidth()).isEqualTo(value);
		assertThat(properties.getLength()).isEqualTo(value);
		assertThat(properties.getHeight()).isEqualTo(value);
		assertThat(properties.getUnitsLength()).isEqualTo("unitsLength");
		assertThat(properties.getUnitsWeight()).isEqualTo("unitsWeight");
	}
}
