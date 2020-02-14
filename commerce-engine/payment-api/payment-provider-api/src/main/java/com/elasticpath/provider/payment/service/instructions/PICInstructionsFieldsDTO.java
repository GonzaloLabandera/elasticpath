/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instructions;

import java.util.List;

import com.elasticpath.base.common.dto.StructuredErrorMessage;

/**
 * A DTO object for PICInstructionsFields.
 */
public class PICInstructionsFieldsDTO {

	private List<String> fields;
	private List<StructuredErrorMessage> structuredErrorMessages;

	/**
	 * Return the payment instrument creation instructions fields.
	 *
	 * @return list of strings representing the fields
	 */
	public List<String> getFields() {
		return fields;
	}

	/**
	 * Set the payment instrument creation instructions fields.
	 *
	 * @param fields list of strings representing the fields
	 */
	public void setFields(final List<String> fields) {
		this.fields = fields;
	}

	/**
	 * Gets structured error messages required to create payments instructions.
	 *
	 * @return additional fields required to create payments instructions.
	 */
	public List<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}

	/**
	 * Sets structured error messages required to create payments instructions.
	 *
	 * @param structuredErrorMessages additional fields required to create payments instructions.
	 */
	public void setStructuredErrorMessages(final List<StructuredErrorMessage> structuredErrorMessages) {
		this.structuredErrorMessages = structuredErrorMessages;
	}

}
