/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link OfferTranslation}.
 */
public class OfferTranslationSerializationTest extends BaseSetUp {

	@Test
	public void testOfferTranslationProjectionShouldBeEqualsJsonStringWithCorrectOrderOfFields() throws IOException {
		final String jsonString = "{\"language\":\"language\",\"details\":[],\"options\":[],\"displayName\":\"displayName\","
				+ "\"brand\":{\"displayName\":\"displayName\",\"name\":\"name\"}}";
		final OfferTranslation translation = new OfferTranslation(new Translation("language", "displayName"),
				new TranslationUnit("displayName", "name"), Collections.emptyList(), Collections.emptyList());
		final String newJson = getObjectMapper().writeValueAsString(translation);
		assertThat(newJson).isEqualTo(jsonString);
	}
}
