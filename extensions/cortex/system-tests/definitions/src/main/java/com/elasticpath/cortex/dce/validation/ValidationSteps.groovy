package com.elasticpath.cortex.dce.validation

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.fail

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import java.util.stream.Collectors

import com.elasticpath.CucumberDTO.StructuredError
import com.elasticpath.CucumberDTO.ValidationStructuredError
import com.elasticpath.cortexTestObjects.Cart

class ValidationSteps {

	@And('^Structured error message contains:$')
	static void verifyStructuredMessage(DataTable responsesTable) {
		List<String> responseMessages = responsesTable.asList(String)

		assertThat(client.body.messages.size())
				.as("The number of error messages is not as expected")
				.isEqualTo(responseMessages.size())
		client.body.messages.each { message ->
			assertThat(responseMessages.contains(message["debug-message"]))
					.as("The structured message " + message["debug-message"] + " was not expected")
					.isTrue()
		}
	}

	/**
	 * Verifies structured error message by passing in a DataTable object mapping to the Cucumber DTO class
	 * that stores the attributes.
	 */
	@Then('^I should see validation error message with message type, message id, debug message, and field$')
	static void verifyStructuredMessageFields(DataTable errorDataTable) {
		assertValidationErrorStatus(client.response.status)

		def expectedErrors = new HashSet(errorDataTable.asList(ValidationStructuredError))

		def actualErrors = client.body.messages.stream()
			.map { ValidationStructuredError.fromResponseMessage it }
			.collect(Collectors.toSet())

		def commonErrors = expectedErrors.intersect(actualErrors)
		expectedErrors.removeAll(commonErrors)
		actualErrors.removeAll(commonErrors)

		if (expectedErrors.size() > 0 || actualErrors.size() > 0) {
			def msgBuilder = StringBuilder.newInstance()
			if (expectedErrors.size() > 0) {
				msgBuilder.append("Expected error messages but not found ")
				msgBuilder.append(expectedErrors).append("\n")
			}
			if (actualErrors.size() > 0) {
				msgBuilder.append("Unexpected error messages ")
				msgBuilder.append(actualErrors).append("\n")
			}
			fail(msgBuilder.toString())
		}
	}

	@Then('^I should see validation error message with message type, message id, and debug message$')
	static void verifyDebugMessage(DataTable error) {

		def structuredErrorList = error.asList(StructuredError)

		assertValidationErrorStatus(client.response.status)
		/**
		 * First loop is looping the data table coming from feature.
		 * Second loop is looping each message.
		 */
		for (StructuredError structureError : structuredErrorList) {
			boolean messageExists = false;
			client.body.messages.each { message ->
				if (structureError.getMessageId() == message.'id' && structureError.getMessageType() == message.'type'
						&& structureError.getDebugMessage() == message.'debug-message') {
					messageExists = true
					return true
				}
			}
			assertThat(messageExists)
					.as("Unable to retrieve expected error - " + structureError.getMessageId())
					.isTrue()
		}
	}

	@When('^I update (.+) in cart with Quantity: (.+) and Configurable Fields:$')
	static void updateLineitemQtyAndFields(String itemSkuCode, String itemQty, DataTable modifierFieldsTable) {
		def Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		def lineitemUri = findLineitemUriBySkuCode(itemSkuCode)
		client.PUT(lineitemUri, [
				"quantity"   : itemQty,
				configuration: configurationFields
		])
	}

	private static def findLineitemUriBySkuCode(String itemSkuCode) {
		Cart.lineitems()
		Cart.findCartElementBySkuCode(itemSkuCode)
		return client.body.self.uri
	}

	private static def assertValidationErrorStatus(int status) {
		assertThat(status == 400 || status == 409)
				.as("Response status message is not as expected.  Expecting 4xx but was " + client.response.status)
				.isTrue()
	}
}
