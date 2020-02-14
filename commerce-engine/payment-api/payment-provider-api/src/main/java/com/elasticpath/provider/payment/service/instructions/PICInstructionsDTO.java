/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instructions;

import java.util.Map;

/**
 * A DTO object for PICInstructions.
 */
public class PICInstructionsDTO {

	private Map<String, String> communicationInstructions;
	private Map<String, String> payload;

	/**
	 * Gets communication instructions map.
	 *
	 * @return the communication instructions map
	 */
	public Map<String, String> getCommunicationInstructions() {
		return communicationInstructions;
	}

	/**
	 * Sets the communication instructions map.
	 *
	 * @param communicationInstructions map of strings.
	 */
	public void setCommunicationInstructions(final Map<String, String> communicationInstructions) {
		this.communicationInstructions = communicationInstructions;
	}

	/**
	 * Gets payload.
	 *
	 * @return the payload
	 */
	public Map<String, String> getPayload() {
		return payload;
	}

	/**
	 * Sets the payload.
	 *
	 * @param payload map of strings.
	 */
	public void setPayload(final Map<String, String> payload) {
		this.payload = payload;
	}

}
