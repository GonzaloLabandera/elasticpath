package com.elasticpath.cortex.dce.addresses

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Profile

/**
 * Default address selection steps.
 */
class DefaultAddressSelectionSteps {


	@When ('^I view the default billing address selector$')
	static void viewDefaultBillingAddressSelectors() {
		client.GET("/")
				.defaultprofile()
				.addresses()
				.billingaddresses()
				.selector()
				.stopIfFailure()
	}

	@When ('^I view the default shipping address selector$')
	static void viewDefaultShippingAddressSelectors() {
		client.GET("/")
				.defaultprofile()
				.addresses()
				.shippingaddresses()
				.selector()
				.stopIfFailure()
	}

	@When ('^I select the address with country (.+) and region (.+)$')
	static void selectAddress(final String countryName, final String region) {
		client.findChoice {
			address ->
				def description = address.description()
				description["address"]["region"] == region && description["address"]["country-name"] == countryName
		}
		.selectaction()
				.follow()
				.stopIfFailure()
	}

	@Then ('^the address with country (.+) and region (.+) is selected$')
		static void addressIsSelected(final String countryName, final String region) {
		def address = client.chosen()
				.description()["address"]
		assertThat(address["country-name"])
				.as("The country name is not as expected")
				.isEqualTo(countryName)
		assertThat(address["region"])
				.as("The region is not as expected")
				.isEqualTo(region)
	}

	@When('^I get the default account billing address$')
	static void getAccountBillingAddress() {
		client.addresses()
			.billingaddresses()
	}

	@When('^I get the default account shipping address$')
	static void getAccountShippingAddress() {
		client.addresses()
				.shippingaddresses()
	}
}