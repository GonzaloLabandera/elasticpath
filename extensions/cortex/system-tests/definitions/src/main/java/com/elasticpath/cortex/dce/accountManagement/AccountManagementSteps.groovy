/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.cortex.dce.accountManagement

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then

class AccountManagementSteps {

	@Then('^there is an account with the field (.+) with value (.+)')
	static void verifyAccountWithFieldValue(String field, String value) {
		def accountsUri = client.body.self.uri
		Boolean found = false
		client.findElement {
			element ->
				if (element["$field"] == value)
					found = true
		}
		assertThat(found)
				.as("Account with $value was not in element list")
				.isTrue()
		client.GET(accountsUri)
	}

	@Then('^I get the account with the field (.+) with value (.+)')
	static void getAccountWithFieldValue(String field, String value) {
		def resultUri = client.body.self.uri
		Boolean found = false
		client.findElement {
			element ->
				if (element["$field"] == value)
					found = true
					resultUri = element.body.self.uri
		}
		assertThat(found)
				.as("Account with $value was not in element list")
				.isTrue()
		client.GET(resultUri)
	}
	


}
