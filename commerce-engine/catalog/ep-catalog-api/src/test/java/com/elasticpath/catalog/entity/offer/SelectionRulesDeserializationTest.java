/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.entity.offer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.elasticpath.catalog.entity.BaseSetUp;

/**
 * Tests {@link SelectionRules}.
 */
public class SelectionRulesDeserializationTest extends BaseSetUp {

	@Test
	public void testThatJsonCorrectDeserializeIntoSelectionRules() throws IOException {
		final String jsonString =
				"{\"selectionType\":\"NONE\","
						+ "\"quantity\":1}";
		final SelectionRules rules = getObjectMapper().readValue(jsonString, SelectionRules.class);
		assertThat(rules.getSelectionType()).isNotNull();
		assertThat(rules.getQuantity()).isEqualTo(1);
	}
}
