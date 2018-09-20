/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.transform;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.StructuredErrorMessageTransformerImpl;

/**
 * Tests for {@link StructuredErrorMessageTransformer}.
 */
public class StructuredErrorMessageTransformerTest {

	private StructuredErrorMessageTransformer structuredErrorMessageTransformer;
	private static final Map<String, String> COMMERCE_ENGINE_FIELD_TO_CORTEX_FIELD_MAP =
			ImmutableMap.of(
					"customerProfile.profileValueMap[CP_FIRST_NAME]", "given-name"
			);

	@Before
	public void initialize() {
		StructuredErrorMessageTransformerImpl structuredErrorMessageTransformerImpl =
				new StructuredErrorMessageTransformerImpl();
		structuredErrorMessageTransformerImpl.setFieldMap(COMMERCE_ENGINE_FIELD_TO_CORTEX_FIELD_MAP);
		structuredErrorMessageTransformer = structuredErrorMessageTransformerImpl;
	}

	@Test
	public void shouldTransformAndReplaceCommerceEngineFieldNameByCortexFieldName() {
		StructuredErrorMessage structuredErrorMessage = new StructuredErrorMessage(
				"message id",
				"customerProfile.profileValueMap[CP_FIRST_NAME] cannot be empty.",
				ImmutableMap.of(
						"field-name", "customerProfile.profileValueMap[CP_FIRST_NAME]",
						"key", "value"
				)
		);

		List<Message> messages = structuredErrorMessageTransformer.transform(Arrays.asList(structuredErrorMessage));

		assertEquals(messages.get(0).getId(), "message id");
		assertEquals(messages.get(0).getDebugMessage(), "given-name cannot be empty.");
		assertEquals(messages.get(0).getData().get("field-name"), "given-name");
		assertEquals(messages.get(0).getData().get("key"), "value");
	}

}
