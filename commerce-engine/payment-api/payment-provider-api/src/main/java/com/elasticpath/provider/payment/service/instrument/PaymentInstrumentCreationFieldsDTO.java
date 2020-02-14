/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument;

import java.util.List;

import com.elasticpath.base.common.dto.StructuredErrorMessage;

/**
 * A DTO object for payment instrument creation fields.
 */
public class PaymentInstrumentCreationFieldsDTO {

	private List<String> fields;
	private List<StructuredErrorMessage> structuredErrorMessages;
	private boolean saveable;

	/**
	 * Return the payment instrument creation fields.
	 *
	 * @return list of Strings representing the fields
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
	 * Gets structured error messages required to create payments instrument.
	 *
	 * @return structured error messages required to create payments instrument.
	 */
	public List<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}

	/**
	 * Sets structured error messages required to create payments instrument.
	 *
	 * @param structuredErrorMessages structured error messages required to create payments instrument.
	 */
	public void setStructuredErrorMessages(final List<StructuredErrorMessage> structuredErrorMessages) {
		this.structuredErrorMessages = structuredErrorMessages;
	}

	/**
	 * Return a flag indicating whether the corresponding payment instrument is saveable.
	 *
	 * @return list of Strings representing the fields
	 */
	public boolean isSaveable() {
		return saveable;
	}

	/**
	 * Set the flag indicating whether the corresponding payment instrument is saveable.
	 *
	 * @param isSaveable boolean flag indicating saveability
	 */
	public void setSaveable(final boolean isSaveable) {
		this.saveable = isSaveable;
	}
}
