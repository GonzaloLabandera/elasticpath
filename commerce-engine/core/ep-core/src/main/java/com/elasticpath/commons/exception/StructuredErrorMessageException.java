/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.commons.exception;

import java.util.List;

import com.elasticpath.common.dto.StructuredErrorMessage;

/**
 * Exception type which provides structured error messages.
 */
public interface StructuredErrorMessageException {

	/**
	 * Returns the structured error messages.
	 *
	 * @return StructuredErrorMessages the structured error messages
	 */
	List<StructuredErrorMessage> getStructuredErrorMessages();

	/**
	 * Returns the exception message.
	 *
	 * @return exception message
	 */
	String getMessage();
}
