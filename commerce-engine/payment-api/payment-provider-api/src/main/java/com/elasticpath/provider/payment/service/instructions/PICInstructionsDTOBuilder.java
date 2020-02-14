/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instructions;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PIC_INSTRUCTIONS_DTO;

import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;

/**
 * A DTO object builder for PICInstructions.
 */
public final class PICInstructionsDTOBuilder {

	private Map<String, String> communicationInstructions;
	private Map<String, String> payload;

	private PICInstructionsDTOBuilder() {
	}

	/**
	 * A payment instruction creation instructions DTO builder.
	 *
	 * @return the builder
	 */
	public static PICInstructionsDTOBuilder builder() {
		return new PICInstructionsDTOBuilder();
	}

	/**
	 * Sets the communication instructions.
	 *
	 * @param communicationInstructions map of strings
	 * @return PICInstructionsDTOBuilder with the control data set
	 */
	public PICInstructionsDTOBuilder withCommunicationInstructions(final Map<String, String> communicationInstructions) {
		this.communicationInstructions = communicationInstructions;
		return this;
	}

	/**
	 * Sets the payload.
	 *
	 * @param payload map of strings
	 * @return PICInstructionsDTOBuilder with the payload data set
	 */
	public PICInstructionsDTOBuilder withPayload(final Map<String, String> payload) {
		this.payload = payload;
		return this;
	}

	/**
	 * Build payment instrument creation instructions DTO.
	 *
	 * @param beanFactory EP bean factory
	 * @return payment instrument creation instructions DTO
	 */
	public PICInstructionsDTO build(final BeanFactory beanFactory) {
		if (communicationInstructions == null) {
			throw new IllegalStateException("Builder is not fully initialized, communicationInstructions map is missing");
		}
		if (payload == null) {
			throw new IllegalStateException("Builder is not fully initialized, payload map is missing");
		}
		final PICInstructionsDTO picInstructionsDTO = beanFactory.getPrototypeBean(
				PIC_INSTRUCTIONS_DTO, PICInstructionsDTO.class);
		picInstructionsDTO.setCommunicationInstructions(communicationInstructions);
		picInstructionsDTO.setPayload(payload);
		return picInstructionsDTO;
	}
}
