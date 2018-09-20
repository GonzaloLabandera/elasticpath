/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.commons.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.common.dto.StructuredErrorMessage;

/**
 * Validation exception represents an error in input data and contains a structured error
 * message to give detailed information about the invalid input which caused the error.
 */
public class EpValidationException extends EpSystemException implements StructuredErrorMessageException {

	private static final long serialVersionUID = -8999932578270387947L;

	private final String message;
	private final List<StructuredErrorMessage> structuredErrorMessageList;

	/**
	 * Default constructor.
	 * @param message the message
	 * @param structuredErrorMessageList a list of commerce message
	 */
	public EpValidationException(final String message, final List<StructuredErrorMessage> structuredErrorMessageList) {
		super(message);
		this.message = message;
		this.structuredErrorMessageList = structuredErrorMessageList;
	}

	@Override
	public List<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessageList;
	}

	@Override
	public String getMessage() {
		return message + ": " + structuredErrorMessageList.stream()
				.map(structuredErrorMessage -> "[" + structuredErrorMessage.toString() + "]")
				.collect(Collectors.joining(", "));
	}
}
