/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cortex.dce.payment

import static org.assertj.core.api.Assertions.assertThat

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

class PaymentInstructionsSteps {

	@When('^I request payment instruction without supplying any fields$')
	static def requestPaymentInstruction() {
		client.requestinstructionsform()
				.requestpaymentinstructionsaction()
				.follow()
				.stopIfFailure()
	}

	@When('^I request payment instruction without expected fields$')
	static def requestPaymentInstructionWithoutRequiredFields() {
		client.requestinstructionsform()
				.requestpaymentinstructionsaction()
	}

	@When('^I open payment instructions form$')
	static def openPaymentInstructionsForm() {
		client.requestinstructionsform()
	}

	@When('^I request payment instruction supplying following fields:$')
	static def requestPaymentInstruction(DataTable dataTable) {
		client.requestinstructionsform()
				.requestpaymentinstructionsaction(dataTable.asMap(String, String))
				.follow()
				.stopIfFailure()
	}

	@When('^I request invalid payment instructions supplying following fields:$')
	static def requestInvalidPaymentInstruction(DataTable dataTable) {
		client.requestinstructionsform()
				.requestpaymentinstructionsaction(dataTable.asMap(String, String))
	}

	@When('^I request payments instructions with a valid address and data$')
	static def submitWithValidBillingAddress() {
		def randomAddress = UUID.randomUUID().toString() + "random street"

		client.requestpaymentinstructionsaction(
				[
						"billing-address"        : [
								organization  : "organization corp",
								"phone-number": "800-674-2677",
								address       : ["country-name"    : "CA",
												 "extended-address": "extended address",
												 "locality"        : "Vancouver",
												 "postal-code"     : "V7V7V7",
												 "region"          : "BC",
												 "street-address"  : randomAddress],
								name          : ["family-name": "family-name",
												 "given-name" : "given-name"]
						],
						"PIC Instruction Field A": "Field A",
						"PIC Instruction Field B": "Field B"
				]
		)
				.follow()
	}

	@When('^I request payments instructions with a blank billing address and data$')
	static def submitWithBlankBillingAddress() {
		client.requestpaymentinstructionsaction(
				[
						"billing-address": [
								organization  : " ",
								"phone-number": " ",
								address       : ["country-name"    : " ",
												 "extended-address": " ",
												 "locality"        : " ",
												 "postal-code"     : " ",
												 "region"          : " ",
												 "street-address"  : " "],
								name          : ["family-name": " ",
												 "given-name" : " "]
						],
						data             : [
								"PIC Instruction Field A": "Field A",
								"PIC Instruction Field B": "Field B"
						]
				]
		)
	}

	@Then('^I should see payment instruction created$')
	static def verifyPaymentInstruction() {
		assertThat(client.body['communication-instructions'])
				.as("\"communication-instructions\" property not found")
				.isNotNull()
		assertThat(client.body['payload'])
				.as("\"payload\" property not found")
				.isNotNull()
	}

}
