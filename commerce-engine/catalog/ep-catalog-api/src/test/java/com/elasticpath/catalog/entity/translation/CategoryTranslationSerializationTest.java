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
 * Tests {@link CategoryTranslation}.
 */
public class CategoryTranslationSerializationTest extends BaseSetUp {
	@Test
	public void testCategoryTranslationProjectionShouldBeEqualsJsonStringWithCorrectOrderOfFields() throws IOException {
		final String jsonString = "{\"language\":\"language\",\"displayName\":\"displayName\",\"details\":[]}";
		final CategoryTranslation categoryTranslation = new CategoryTranslation(new Translation("language", "displayName"),
				Collections.emptyList());
		final String newJson = getObjectMapper().writeValueAsString(categoryTranslation);
		assertThat(newJson).isEqualTo(jsonString);
	}
}
