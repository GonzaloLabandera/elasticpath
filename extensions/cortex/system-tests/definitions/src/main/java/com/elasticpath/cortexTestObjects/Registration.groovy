package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Registration.
 */
class Registration extends CommonMethods {


	static void getRegistrationForm() {
		client.GET("/")
				.newaccountform()
				.stopIfFailure()
	}

	static void registerWithoutFollow(familyName, givenName, password, username) {
		getRegistrationForm()
		client.registeraction("family-name": familyName, "given-name": givenName, "password": password, "username": username)
	}

	static void register(familyName, givenName, password, username) {
		registerWithoutFollow(familyName, givenName, password, username)
		client.follow()
				.stopIfFailure()
	}

	static void registerWithoutFollow(jsonInput) {
		getRegistrationForm()
		client.registeraction(jsonInput)
	}

	static void register(jsonInput) {
		registerWithoutFollow(jsonInput)
		client.follow()
				.stopIfFailure()
	}

}