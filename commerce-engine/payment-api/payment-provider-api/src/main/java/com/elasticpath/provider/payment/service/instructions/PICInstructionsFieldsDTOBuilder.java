/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instructions;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_FIELDS_DTO;

import java.util.List;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.beanframework.BeanFactory;

/**
 * A DTO object builder for PICInstructionsFields.
 */
public final class PICInstructionsFieldsDTOBuilder {

	private List<String> fields;
	private List<StructuredErrorMessage> structuredErrorMessages;

	private PICInstructionsFieldsDTOBuilder() {
	}

	/**
	 * A payment instrument creation instructions fields DTO builder.
	 *
	 * @return the builder
	 */
	public static PICInstructionsFieldsDTOBuilder builder() {
		return new PICInstructionsFieldsDTOBuilder();
	}

	/**
	 * Sets the encapsulated payment instrument creation fields.
	 *
	 * @param fields list of Strings
	 * @return payment instrument creation instructions fields DTO builder
	 */
	public PICInstructionsFieldsDTOBuilder withFields(final List<String> fields) {
		this.fields = fields;
		return this;
	}

	/**
	 * Sets blocking fields required to create payments instrument.
	 *
	 * @param errorMessages blocking fields required to create payments instrument
	 * @return payment instrument creation instructions fields DTO builder
	 */
	public PICInstructionsFieldsDTOBuilder withStructuredErrorMessages(final List<StructuredErrorMessage> errorMessages) {
		this.structuredErrorMessages = errorMessages;
		return this;
	}

	/**
	 * Build payment instrument DTO.
	 *
	 * @param beanFactory EP bean factory
	 * @return the payment instrument DTO
	 */
	public PICInstructionsFieldsDTO build(final BeanFactory beanFactory) {
		if (fields == null) {
			throw new IllegalStateException("Builder is not fully initialized, fields list is missing");
		}
		if (structuredErrorMessages == null) {
			throw new IllegalStateException("Builder is not fully initialized, structuredErrorMessages list is missing");
		}
		final PICInstructionsFieldsDTO picInstructionsFields = beanFactory.getPrototypeBean(
				PIC_INSTRUCTIONS_FIELDS_DTO, PICInstructionsFieldsDTO.class);
		picInstructionsFields.setFields(fields);
		picInstructionsFields.setStructuredErrorMessages(structuredErrorMessages);
		return picInstructionsFields;
	}
}
