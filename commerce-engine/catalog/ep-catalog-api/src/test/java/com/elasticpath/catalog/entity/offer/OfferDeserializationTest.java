/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link Offer}.
 */
public class OfferDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoOffer() throws IOException {
		final String jsonString =
				"{\"offerProperties\":{\"projectionProperties\":{},\"offerSpecificProperties\":[]},"
						+ "\"items\":[],"
						+ "\"extensions\":{},"
						+ "\"rules\":{\"availabilityRules\":{}, \"selectionRules\":{}},"
						+ "\"associations\":[],"
						+ "\"components\":{},"
						+ "\"formFields\":[],"
						+ "\"translations\":[{\"language\":\"language\","
						+ "\"displayName\":\"displayName\",\"brand\":{},\"options\":[],\"details\":[]}]}";
		final Offer offer = getObjectMapper().readValue(jsonString, Offer.class);
		assertThat(offer.getProperties()).isNotNull();
		assertThat(offer.getItems()).isNotNull();
		assertThat(offer.getExtensions()).isNotNull();
		assertThat(offer.getAvailabilityRules()).isNotNull();
		assertThat(offer.getAssociations()).isNotNull();
		assertThat(offer.getComponents()).isNotNull();
		assertThat(offer.getFormFields()).isNotNull();
		assertThat(offer.getTranslations().size()).isEqualTo(1);
	}
}
