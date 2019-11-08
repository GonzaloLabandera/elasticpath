/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.request.validator;

import com.elasticpath.catalog.webservice.request.entity.RequestBody;

/**
 * Represents the interface for validation of {@link RequestBody}.
 */
public interface RequestBodyValidator {

	/**
	 * Validates RequestBody.
	 *
	 * @param requestBody request body for validation.
	 * @return "true" if RequestBody is valid, "false" if RequestBody is invalid.
	 */
	boolean validate(RequestBody requestBody);

}
