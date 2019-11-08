/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Association}.
 */
public class AssociationDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoAssociation() throws IOException {
		final String jsonString =
				"{\"type\":\"type\","
						+ "\"list\":[{\"offer\":\"offer\", \"enableDateTime\":\"2019-03-15T09:57:11.234+03:00\","
						+ "\"disableDateTime\":\"2019-03-15T09:57:11.234+03:00\"}]}";
		final Association association = getObjectMapper().readValue(jsonString, Association.class);
		assertThat(association.getType()).isEqualTo("type");
		assertThat(association.getList().size()).isEqualTo(1);
	}
}
