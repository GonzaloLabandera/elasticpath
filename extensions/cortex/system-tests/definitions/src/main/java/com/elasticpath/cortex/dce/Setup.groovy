package com.elasticpath.cortex.dce

import static ClasspathFluentRelosClientFactory.createClient
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.Before

class Setup {

	@Before(order = 0)
	static void before() {
		createClient()
	}

	@Before(value = "@HAL", order = 1)
	static beforeHAL() {
		client.getHeaders().Accept = "application/hal+json"
		def restClient = client.getRestClient()
		restClient.parser.'application/hal+json' = restClient.parser.'application/json'
	}
}
