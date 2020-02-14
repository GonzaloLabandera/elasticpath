/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.exception;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Payment API error message data object, which is converted to Cortex StructuredErrorMessage.
 */
public class ErrorMessage {

	private final StructuredMessageType type;
	private final String messageId;
	private final String debugMessage;
	private final Map<String, String> data;

	/**
	 * Constructor.
	 *
	 * @param type         message type.
	 * @param messageId    messageId.
	 * @param debugMessage debug message.
	 * @param data         data of error message.
	 */
	public ErrorMessage(final StructuredMessageType type,
						final String messageId,
						final String debugMessage,
						final Map<String, String> data) {
		this.type = type;
		this.messageId = messageId;
		this.debugMessage = debugMessage;
		this.data = data;
	}

	/**
	 * Get the type of the message.
	 *
	 * @return the type
	 */
	public StructuredMessageType getType() {
		return type;
	}

	/**
	 * Get the id of the message.
	 *
	 * @return message Id
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Get the debug message for the structured message.
	 *
	 * @return the debug message
	 */
	public String getDebugMessage() {
		return debugMessage;
	}

	/**
	 * Get the map of additional information related to the message. This includes
	 * values that can be used to replace placeholders in message templates.
	 *
	 * @return the map.
	 */
	public Map<String, String> getData() {
		return data;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		ErrorMessage that = (ErrorMessage) other;

		return Objects.equals(type, that.type)
				&& Objects.equals(messageId, that.messageId)
				&& Objects.equals(debugMessage, that.debugMessage)
				&& Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, messageId, debugMessage, data);
	}

	@Override
	public String toString() {
		return "ErrorMessage {messageId= " + messageId + ", type=" + type + ", debugMessage='" + debugMessage + "', data=" + dataToString() + '}';
	}

	private String dataToString() {
		return data.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + entry.getValue())
				.collect(Collectors.joining(", ", "{", "}"));
	}
}