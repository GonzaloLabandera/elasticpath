/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link TranslationUnit}.
 */
public class TranslationUnitDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoTranslationUnit() throws IOException {
		final String jsonString =
				"{\"name\":\"name\","
						+ "\"displayName\":\"displayName\"}";
		final TranslationUnit unit = getObjectMapper().readValue(jsonString, TranslationUnit.class);
		assertThat(unit.getName()).isEqualTo("name");
		assertThat(unit.getDisplayName()).isEqualTo("displayName");
	}
}
