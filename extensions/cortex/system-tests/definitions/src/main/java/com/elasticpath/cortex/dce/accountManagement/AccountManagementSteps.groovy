/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.cortex.dce.accountManagement

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
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
		checkElement(field, value)
	}

	@Then('^I get the order with the field (.+) with value (.+)')
	static void getOrderWithFieldValue(String field, String value) {
		checkElement(field, value)
	}

	static void checkElement(String field, String value) {
		def resultUri = client.body.self.uri
		Boolean found = false
		client.findElement {
			element ->
				if (element["$field"] == value) {
					found = true
					resultUri = element.body.self.uri
				}
		}
		assertThat(found)
				.as("Account with $value was not in element list")
				.isTrue()
		client.GET(resultUri)
	}

	@Then('^I should see account attribute (.+) with value (.+)$')
	static void verifyAccountAttributeField(final String attributeName, final String attributeValue) {
		assertThat(client[attributeName])
				.as("Attribute is not as expected")
				.isEqualTo(attributeValue)
	}

	@Then('^I should not see (.+) link$')
	static void verifyLinkDoesNotExist(final String link) {
		assertLinkDoesNotExist(client, link)
	}

	@Then('^I update attributes with following values$')
	static void updateAttributeKey(DataTable dataTable) {
		def dataMap = dataTable.asMap(String, String)
		client.PUT(client.body.self.href, dataMap);
	}
}
