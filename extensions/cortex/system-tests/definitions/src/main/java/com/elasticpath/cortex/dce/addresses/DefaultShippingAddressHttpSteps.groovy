package com.elasticpath.cortex.dce.addresses

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Profile

class DefaultShippingAddressHttpSteps {

	@When('^I get the default shipping address$')
	static void getShippingAddress() {
		String uri = Profile.getDefaultShippingAddressUri()
		client.GET(uri)
	}

	@When('I put to default shipping address$')
	static void putShippingAddress() {
		String uri = Profile.getDefaultShippingAddressUri()
		client.PUT(uri, '{}')
	}

	@When('I post to default shipping address$')
	static void postShippingAddress() {
		String uri = Profile.getDefaultShippingAddressUri()

		client.POST(uri, '{}')
	}

	@When('I delete the default shipping address$')
	static void deleteShippingAddress() {
		String uri = Profile.getDefaultShippingAddressUri()
		client.DELETE(uri)
	}

}
