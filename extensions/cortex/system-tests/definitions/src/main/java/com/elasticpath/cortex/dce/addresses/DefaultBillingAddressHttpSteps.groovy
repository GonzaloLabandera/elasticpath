package com.elasticpath.cortex.dce.addresses

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Profile

class DefaultBillingAddressHttpSteps {

	@When('^I get the default billing address$')
	static void getBillingAddress() {
		String uri = Profile.getDefaultBillingAddressUri()
		client.GET(uri)
	}

	@When('I put to default billing address$')
	static void putBillingAddress() {
		String uri = Profile.getDefaultBillingAddressUri()
		client.PUT(uri, '{}')
	}

	@When('I post to default billing address$')
	static void postBillingAddress() {
		String uri = Profile.getDefaultBillingAddressUri()
		client.POST(uri, '{}')
	}

	@When('I delete the default billing address$')
	static void deleteBillingAddress() {
		String uri = Profile.getDefaultBillingAddressUri()
		client.DELETE(uri)
	}

}
