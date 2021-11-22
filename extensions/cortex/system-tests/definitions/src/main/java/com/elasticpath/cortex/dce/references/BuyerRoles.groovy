/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.cortex.dce.references

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then

class BuyerRoles {

	@Then('^I get back the list of roles')
	static void getComponentsCodes() {
		assertThat(client.body.roles)
				.as("list of roles not found")
				.isNotNull()
				.isNotEmpty()
	}

}
