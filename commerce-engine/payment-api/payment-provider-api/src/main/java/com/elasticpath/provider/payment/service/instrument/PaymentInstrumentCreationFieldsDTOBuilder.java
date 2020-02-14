/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument;

import java.util.List;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;

/**
 * A DTO object builder for PICInstructions.
 */
public final class PaymentInstrumentCreationFieldsDTOBuilder {

	private List<String> fields;
	private List<StructuredErrorMessage> errorMessages;
	private boolean isSaveable;

	private PaymentInstrumentCreationFieldsDTOBuilder() {
	}

	/**
	 * A payment instrument creation fields DTO builder.
	 *
	 * @return the builder
	 */
	public static PaymentInstrumentCreationFieldsDTOBuilder builder() {
		return new PaymentInstrumentCreationFieldsDTOBuilder();
	}

	/**
	 * Set the payment instrument creation instructions fields.
	 *
	 * @param fields list of strings representing the fields
	 * @return payment instrument creation fields DTO builder with the provider fields
	 */
	public PaymentInstrumentCreationFieldsDTOBuilder withFields(final List<String> fields) {
		this.fields = fields;
		return this;
	}

	/**
	 * Sets the blocking fields.
	 *
	 * @param blockingField blocking fields required to create payments instrument
	 * @return payment instrument creation instructions fields DTO builder
	 */
	public PaymentInstrumentCreationFieldsDTOBuilder withBlockingFields(final List<StructuredErrorMessage> blockingField) {
		this.errorMessages = blockingField;
		return this;
	}

	/**
	 * Set the flag indicating whether the corresponding payment instrument is saveable.
	 *
	 * @param isSaveable boolean flag indicating saveability
	 * @return payment instrument creation fields DTO builder with the provider fields
	 */
	public PaymentInstrumentCreationFieldsDTOBuilder withIsSaveable(final boolean isSaveable) {
		this.isSaveable = isSaveable;
		return this;
	}

	/**
	 * Build payment instrument DTO.
	 *
	 * @param beanFactory EP bean factory
	 * @return the payment instrument DTO
	 */
	public PaymentInstrumentCreationFieldsDTO build(final BeanFactory beanFactory) {
		if (fields == null) {
			throw new IllegalStateException("Builder is not fully initialized, fields list is missing");
		}
		if (errorMessages == null) {
			throw new IllegalStateException("Builder is not fully initialized, errorMessages list is missing");
		}
		final PaymentInstrumentCreationFieldsDTO paymentInstrumentCreationFields = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PIC_FIELDS_DTO, PaymentInstrumentCreationFieldsDTO.class);
		paymentInstrumentCreationFields.setFields(fields);
		paymentInstrumentCreationFields.setStructuredErrorMessages(errorMessages);
		paymentInstrumentCreationFields.setSaveable(isSaveable);
		return paymentInstrumentCreationFields;
	}
}
