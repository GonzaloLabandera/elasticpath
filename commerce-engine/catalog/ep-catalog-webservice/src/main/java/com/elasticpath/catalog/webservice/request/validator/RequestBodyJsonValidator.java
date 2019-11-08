/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.request.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import com.elasticpath.catalog.exception.ValidationException;
import com.elasticpath.catalog.webservice.request.entity.RequestBody;

/**
 * Represents the implementation of {@link RequestBodyValidator} for validation of {@link RequestBody} by json schema.
 */
public class RequestBodyJsonValidator implements RequestBodyValidator {

	private final JsonSchema jsonSchema;
	private final ObjectMapper objectMapper;

	/**
	 * Constructor.
	 *
	 * @param schemaLocation Json schema location.
	 * @param objectMapper object mapper for conversion
	 */
	public RequestBodyJsonValidator(final String schemaLocation, final ObjectMapper objectMapper) {
		this.jsonSchema = getJsonSchemaFromClassPathResource(schemaLocation);
		this.objectMapper = objectMapper;
	}

	@Override
	public boolean validate(final RequestBody requestBody) {
		try {
			return jsonSchema.validate(JsonLoader.fromString(objectMapper.writeValueAsString(requestBody))).isSuccess();
		} catch (Exception e) {
			throw new ValidationException(e);
		}
	}

	private JsonSchema getJsonSchemaFromClassPathResource(final String classPathResource) {
		try {
			return JsonSchemaFactory.byDefault().getJsonSchema(getClass().getResource(classPathResource).toURI().toString());
		} catch (Exception e) {
			throw new ValidationException(e);
		}
	}

}
