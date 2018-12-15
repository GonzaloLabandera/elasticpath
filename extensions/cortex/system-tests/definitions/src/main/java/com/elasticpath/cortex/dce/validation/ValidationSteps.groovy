package com.elasticpath.cortex.dce.validation

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

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
	static void verifyStructuredMessageFields(DataTable error) {
		def structuredErrorList = error.asList(ValidationStructuredError)

		assertValidationErrorStatus(client.response.status)

		/**
		 * First loop is looping the data table coming from feature.
		 * Second loop is looping each message.
		 */
		for (ValidationStructuredError structureError : structuredErrorList) {
			boolean messageExists = false;
			client.body.messages.each { message ->
				if (message.data.'field-name' == structureError.getFieldName()) {
					assertThat(message.'type')
							.as("Error type is not as expected")
							.isEqualTo(structureError.getMessageType())
					assertThat(message.'id')
							.as("Error Id is not as expected")
							.isEqualTo(structureError.getMessageId())
					assertThat(message.'debug-message')
							.as("Debug Message is not as expected")
							.isEqualTo(structureError.getDebugMessage())

					messageExists = true
					return true
				}
			}
			assertThat(messageExists)
					.as("Unable to retrieve expected error - " + structureError.getFieldName())
					.isTrue()
		}
	}

	@Then('^I should see validation error message with message type, message id, and debug message$')
	static void verifyDebugMessage(DataTable error) {

		def structuredErrorList = error.asList(ValidationStructuredError)

		assertValidationErrorStatus(client.response.status)

		/**
		 * First loop is looping the data table coming from feature.
		 * Second loop is looping each message.
		 */
		for (ValidationStructuredError structureError : structuredErrorList) {
			boolean messageExists = false;
			client.body.messages.each { message ->
				if (structureError.getMessageId() == message.'id' && structureError.getMessageType() == message.'type') {
					assertThat(structureError.getDebugMessage())
							.as("Debug Message is not as expected")
							.isEqualTo(message.'debug-message')

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
