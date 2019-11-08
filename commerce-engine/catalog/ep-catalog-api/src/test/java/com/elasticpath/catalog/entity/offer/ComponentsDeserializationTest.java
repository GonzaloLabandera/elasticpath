/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Components}.
 */
public class ComponentsDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoComponents() throws IOException {
		final String jsonString =
				"{\"list\":[{\"offer\":\"offer\","
						+ "\"item\":\"item\","
						+ "\"quantity\":1}]}";
		final Components components = getObjectMapper().readValue(jsonString, Components.class);
		assertThat(components.getList().size()).isEqualTo(1);
	}
}
