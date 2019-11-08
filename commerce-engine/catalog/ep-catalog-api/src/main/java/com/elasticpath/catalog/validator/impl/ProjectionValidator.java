/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.validator.impl;

import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import com.elasticpath.catalog.exception.ValidationException;
import com.elasticpath.catalog.validator.Validator;

/**
 * Implementation of {@link Validator}.
 */
public class ProjectionValidator implements Validator<String> {

	private final JsonSchema jsonSchema;

	/**
	 * Constructor.
	 *
	 * @param schemaLocation Json schema location.
	 */
	public ProjectionValidator(final String schemaLocation) {
		this.jsonSchema = getJsonSchemaFromClassPathResource(schemaLocation);
	}

	@Override
	public void validate(final String json) {
		try {
			final ProcessingReport report = jsonSchema.validate(JsonLoader.fromString(json));
			checkReport(report);
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

	private void checkReport(final ProcessingReport report) {
		if (!report.isSuccess()) {
			throw new ValidationException(report.toString());
		}
	}

}
