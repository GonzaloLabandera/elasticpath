package com.elasticpath.cortex.dce.accountManagement

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then
import cucumber.api.java.en.When

class AccountAssociateSteps {

	@When('^I create an account associate with email (.*), role (.*)$')
	static void createNewAccountAddressWithParameters(String email, String role) {

		client.addassociateaction(
				[
					"role": role,
					"email": email
				]
		)
				.stopIfFailure()

		if (client.response.status == 200 || client.response.status == 201) {
			getClient().follow()
		}
	}

	@Then('the account associate with email (.*) should have the role (.*)')
	static void verifyAssociateEmailAndRole(String email, String role) {
		Boolean found = false
		client.findElement {
			element ->
				if (element["role"] == role) {
					element.associatedetails()
					if (client["email"] == email) {
						found = true
					}
				}
		}

		assertThat(found)
				.as("Associate with email and role was not in element list")
				.isTrue()
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
				.as("Associate with $value was not in element list")
				.isTrue()
		client.GET(resultUri)
	}

	@Then('^I get the (?:associate|associatedetails) with the field (.+) with value (.+)')
	static void getAssociateWithFieldValue(String field, String value) {
		assertThat(client[field])
				.as("Email is not as expected")
				.isEqualTo(value)
	}

	@Then('^I navigate to the account associate with email (.+)$')
	static void naviateToAccountAssociateWithEmail(String email) {
		def tempUri = client.body.self.uri
		def resultUri = tempUri
		client.findElement {
			element ->
				tempUri = client.body.self.uri
				element.associatedetails()
				if (client["email"] == email) {
					resultUri = tempUri
				}
		}
		client.GET(resultUri)
	}

	@Then('^I update role as (.+)$')
	static void updateExistingAssociateRole(String role) {
		client.PUT(client.body.self.href, ["role": role])
	}
}
