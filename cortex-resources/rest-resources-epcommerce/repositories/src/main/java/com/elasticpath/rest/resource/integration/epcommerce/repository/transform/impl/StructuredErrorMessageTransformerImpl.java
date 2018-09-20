/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elasticpath.common.dto.StructuredErrorMessage;
import com.elasticpath.rest.advise.Message;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.StructuredErrorMessageTransformer;

/**
 * Implementation of {@link StructuredErrorMessageTransformer}.
 */
public class StructuredErrorMessageTransformerImpl implements StructuredErrorMessageTransformer {

	private Map<String, String> fieldMap;

	private static final String DATA_KEY_FOR_FIELD_NAME = "field-name";

	public void setFieldMap(final Map<String, String> fieldMap) {
		this.fieldMap = fieldMap;
	}

	@Override
	public List<Message> transform(final List<StructuredErrorMessage> structuredErrorMessages) {
		return structuredErrorMessages.stream().map(structuredErrorMessage ->
				Message.builder()
						.withType(structuredErrorMessage.getType().getName())
						.withId(structuredErrorMessage.getMessageId())
						.withDebugMessage(replaceDebugMessageCommerceEngineFieldNameWithCortexFieldName(structuredErrorMessage))
						.withData(replaceCommerceEngineFieldNameValueWithCortexFieldNameValue(structuredErrorMessage.getData()))
						.build()
		).collect(Collectors.toList());
	}

	private Map<String, String> replaceCommerceEngineFieldNameValueWithCortexFieldNameValue(final Map<String, String> data) {
		final String fieldName = data.get(DATA_KEY_FOR_FIELD_NAME);
		String cortexFieldName = fieldMap.getOrDefault(fieldName, fieldName);

		// Replace field name.
		Map<String, String> cortexData = new HashMap<>();
		data.forEach((key, value) -> {
			if (DATA_KEY_FOR_FIELD_NAME.equalsIgnoreCase(key)) {
				value = cortexFieldName;
			}
			cortexData.put(key, value);
		});
		return cortexData;
	}

	private String replaceDebugMessageCommerceEngineFieldNameWithCortexFieldName(final StructuredErrorMessage structuredErrorMessage) {
		Map<String, String> data = structuredErrorMessage.getData();
		String commerceEngineFieldName = data.get(DATA_KEY_FOR_FIELD_NAME);
		String cortexFieldName = fieldMap.getOrDefault(commerceEngineFieldName, commerceEngineFieldName);
		return structuredErrorMessage.getDebugMessage().replace(commerceEngineFieldName, cortexFieldName);
	}

}
