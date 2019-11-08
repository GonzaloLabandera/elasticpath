/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests {@link NameIdentity}.
 */
public class NameIdentityDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoNameIdentity() throws IOException {
		final String jsonString =
				"{\"type\":\"type\","
						+ "\"code\":\"code\","
						+ "\"store\":\"store\"}";
		final NameIdentity identity = getObjectMapper().readValue(jsonString, NameIdentity.class);
		assertThat(identity.getCode()).isEqualTo("code");
		assertThat(identity.getType()).isEqualTo("type");
		assertThat(identity.getStore()).isEqualTo("store");
	}
}
