/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;
import com.elasticpath.catalog.entity.ProjectionProperties;

/**
 * Tests {@link Offer}.
 */
public class OfferSerializationTest extends BaseSetUp {

	@Test
	public void testOfferProjectionShouldBeEqualsJsonStringWithCorrectOrderOfFields() throws IOException {
		final String jsonString = "{\"identity\":{\"type\":\"offer\",\"code\":\"code\",\"store\":\"store\"},\"deleted\":false,\"properties\":[],"
				+ "\"components\":{\"list\":[]},\"translations\":[],\"categories\":[],\"associations\":[],\"items\":[]}";
		final Offer offer = new Offer(new OfferProperties(
				new ProjectionProperties("code", "store", null, false),
				Collections.emptyList()), Collections.emptyList(), null, Collections.emptyList(),
				new Components(Collections.emptyList()), new OfferRules(null, null),
				Collections.emptyList(), Collections.emptyList(), Collections.emptySet());
		final String newJson = getObjectMapper().writeValueAsString(offer);
		assertThat(newJson).isEqualTo(jsonString);
	}
}
