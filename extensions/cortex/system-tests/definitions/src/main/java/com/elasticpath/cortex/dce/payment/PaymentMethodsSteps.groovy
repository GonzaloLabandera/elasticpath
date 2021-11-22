/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortex.dce.payment

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile

class PaymentMethodsSteps {

	@Then('^I should not see (.+) payment method in my profile$')
	static void verifyProfilePaymentMethodNotExists(def paymentMethodName) {
		Profile.paymentmethods()
		verifyPaymentMethodsNotExist(paymentMethodName)
	}

	@Then('^I should not see (.+) payment method in my order')
	static void verifyOrderPaymentMethodNotExists(def paymentMethodName) {
		Order.paymentmethodsresource()
		verifyPaymentMethodsNotExist(paymentMethodName)
	}

	@Then('^I should not see (.+) payment method in the account')
	static void verifyAccountPaymentMethodNotExists(def paymentMethodName) {
		verifyPaymentMethodsNotExist(paymentMethodName)
	}

	@Then('^I should see the following payment methods in my profile$')
	static void verifyProfilePaymentMethodsExist(DataTable paymentMethods) {
		Profile.paymentmethods()
		verifyPaymentMethodsExist(paymentMethods)
	}

	@Then('^I should see the following payment methods in the account')
	static void verifyAccountPaymentMethodsExist(DataTable paymentMethods) {
		client.paymentmethods()
		verifyPaymentMethodsExist(paymentMethods)
	}

	@Then('^I should see the following payment methods in my order')
	static void verifyOrderPaymentMethodsExist(DataTable paymentMethods) {
		Order.paymentmethodsresource()
		verifyPaymentMethodsExist(paymentMethods)
	}

	private static void verifyPaymentMethodsExist(DataTable paymentMethods) {
		def methodsList = paymentMethods.asList(String)
		List<String> actualLinks = []

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.uri)
				actualLinks.add(client["name"])
			}
		}
		assertThat(actualLinks)
				.as("available payment methods are not as expected")
				.containsExactlyInAnyOrderElementsOf(methodsList)
	}

	static void verifyPaymentMethodsNotExist(def paymentMethodName) {
		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.uri)
				assertThat(client["name"])
						.as("Payment method: $paymentMethodName should not exist but was found")
						.isNotEqualTo(paymentMethodName)
			}
		}
	}

	@Given('^I get the list of payment methods from my profile$')
	static void getProfilePaymentConfigurations() {
		Profile.paymentmethods()
	}

	@Given('^I get the list of payment methods from my order$')
	static void getOrderPaymentConfigurations() {
		Order.paymentmethodsresource()
	}

	@When('^I open (.+) payment method$')
	static def openPaymentConfiguration(final String configurationName) {
		CommonMethods.openLinkRelWithFieldWithValue("element", "name", configurationName)
	}

	@When('^I open (.+) payment method in language (.+)$')
	static def openPaymentConfigurationInLanguage(final String configurationName, def locale) {
		client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
		CommonMethods.openLinkRelWithFieldWithValue("element", "name", configurationName)
	}

	@Then('^I should arrive at the (.+) payment method$')
	static def verifyPaymentConfiguration(final String expectedConfigurationName) {
		String actualName = client["name"]

		assertThat(actualName)
				.as("Expected to arrive at " + expectedConfigurationName + " payment method, but arrived at " + actualName + " instead.")
				.isEqualTo(expectedConfigurationName)

		String selfUri = client.body.self.uri
		def actualLinkRels = []
		def expectedLinks = new ArrayList<String>()
		expectedLinks.add("requestinstructionsform")
		expectedLinks.add("paymentinstrumentform")
		expectedLinks.add("paymentmethods")

		client.body.links.findAll {
			actualLinkRels.add(it.rel)
		}

		assertThat(actualLinkRels)
				.as("Links from " + selfUri + " are not as expected.")
				.isEqualTo(expectedLinks)
	}


}
