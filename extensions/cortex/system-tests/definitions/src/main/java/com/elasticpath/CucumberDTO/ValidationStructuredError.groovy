package com.elasticpath.CucumberDTO
/**
 * StructuredError object corresponding to validation errors.
 */
class ValidationStructuredError extends StructuredError {
	String fieldName

	static ValidationStructuredError fromResponseMessage(message) {
		def error = new ValidationStructuredError()
		error.messageType = message.type
		error.messageId = message.id
		error.debugMessage = message.'debug-message'
		error.fieldName = message.data.'field-name'
		return error
	}

	@Override
	int hashCode() {
		return Objects.hash(messageType, messageId, debugMessage, fieldName)
	}

	@Override
	boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode()
	}

	@Override
	String toString() {
		return String.format(
				"{messageType=\"%s\", messageId=\"%s\", debugMessage=\"%s\", fieldName=\"%s\"}",
				messageType, messageId, debugMessage, fieldName)
	}
}
