package com.elasticpath.cortex.dce.addresses

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.currentScope
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.en.When

class PostAddressWithInvalidJson {

	@When('^I POST with (.+)$')
	static void postData(String jsonInput) {
		client.POST("addresses/$currentScope", jsonInput)
	}
}
