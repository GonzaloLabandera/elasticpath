/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Component}.
 */
public class ComponentDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoComponent() throws IOException {
		final String jsonString =
				"{\"offer\":\"offer\","
						+ "\"item\":\"item\","
						+ "\"quantity\":1} ";
		final Component component = getObjectMapper().readValue(jsonString, Component.class);
		assertThat(component.getOffer()).isEqualTo("offer");
		assertThat(component.getItem()).isEqualTo("item");
		assertThat(component.getQuantity()).isEqualTo(1);
	}
}
