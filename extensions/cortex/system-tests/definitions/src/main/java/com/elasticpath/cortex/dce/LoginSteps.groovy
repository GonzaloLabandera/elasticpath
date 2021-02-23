/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortex.dce

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then

import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.Payment

class LoginSteps {

	static PASSWORD = "password"
	static GIVEN_NAME = "testGivenName"
	static FAMILY_NAME = "testFamilyName"
	static NEW_RANDOM_USERNAME = ""

	@Given('^I (?:login as|am logged in as|transition to|relogin as) (?:a|a new) public shopper$')
	static void loginAsPublicUserWithDefaultScope() {
		client.authAsAPublicUser(DEFAULT_SCOPE)
				.stopIfFailure()
	}

	@Given('^I login as (?:a|another) registered shopper$')
	static void loginAsRegisteredPayableUser() {
		client.authAsRegisteredUser()
		Payment.createProfilePaymentInstrumentWithDefaultName()
		Cart.clearCart()
	}

	@Given('^I transition to registered shopper$')
	static void transitionToRegisteredShopper() {
		client.roleTransitionToRegisteredUser()
				.stopIfFailure()
	}

	@Given('^I have authenticated as (?:a newly|another) registered shopper$')
	static void registerNewShopperAndLoginWithDefaultScope() {
		def userName = UUID.randomUUID().toString() + "@elasticpath.com"
		registerShopper(DEFAULT_SCOPE, FAMILY_NAME, GIVEN_NAME, PASSWORD, userName)

		client.authRegisteredUserByName(DEFAULT_SCOPE, userName)
				.stopIfFailure()
	}

	@Given('^I transition to the (?:newly|first) registered shopper$')
	static void transitionToRegisteredShopperByName() {
		client.roleTransitionToRegisteredUserByName(DEFAULT_SCOPE, NEW_RANDOM_USERNAME)
				.stopIfFailure()
	}

	@Given('^I am logged into scope (.+) as a public shopper$')
	static void loginAsPublicUserWithScope(String scope) {
		client.authAsAPublicUser(scope)
				.stopIfFailure()
	}

	@Given('^I have authenticated on scope (.+) as a newly registered shopper$')
	static void registerNewShopperAndLoginWithScope(def scope) {
		NEW_RANDOM_USERNAME = UUID.randomUUID().toString() + "@elasticpath.com"
		println(NEW_RANDOM_USERNAME)
		registerShopper(scope, FAMILY_NAME, GIVEN_NAME, PASSWORD, NEW_RANDOM_USERNAME)

		client.authRegisteredUserByName(scope, NEW_RANDOM_USERNAME)
				.stopIfFailure()
	}

	@Given('^I login as a newly registered shopper$')
	static void loginWithNewlyRegisteredShopper() {
		NEW_RANDOM_USERNAME = UUID.randomUUID().toString() + "@elasticpath.com"
		println(NEW_RANDOM_USERNAME)
		registerShopper(DEFAULT_SCOPE, FAMILY_NAME, GIVEN_NAME, PASSWORD, NEW_RANDOM_USERNAME)

		client.authRegisteredUserByName(DEFAULT_SCOPE, NEW_RANDOM_USERNAME)
				.stopIfFailure()
	}

	@Given('^I re-authenticate on scope (.+) with the (original|newly) registered shopper$')
	static void loginWithShopperAndScope(def scope, def shopper) {
		client.authRegisteredUserByName(scope, NEW_RANDOM_USERNAME)
				.stopIfFailure()
	}

	@Given('^I re-login with the (original|newly) registered shopper$')
	static void loginWithShopperAndDefaultScope(def shopper) {
		client.authRegisteredUserByName(DEFAULT_SCOPE, NEW_RANDOM_USERNAME)
				.stopIfFailure()
	}

	@Given('^I authenticate as a registered shopper (.+) with the default scope$')
	static void loginWithDefaultScopeAndShopperByName(String username) {
		client.authRegisteredUserByName(DEFAULT_SCOPE, username)
				.stopIfFailure()
	}

	@Given('^I authenticate as a registered shopper (.+) on the default scope with a clear cart$')
	static void loginOnDefaultScopeAndClearCart(String username) {
		client.authRegisteredUserByName(DEFAULT_SCOPE, username)
				.stopIfFailure()
		client.GET("/")
				.defaultcart()
				.lineitems()
				.stopIfFailure()
		client.DELETE(client.body.self.uri)
	}

	@Given('^I authenticate as a registered shopper (.+) on scope (.+)$')
	static void loginAsRegisteredShopperOnScope(String username, String scope) {
		client.authRegisteredUserByName(scope, username)
				.stopIfFailure()
	}

	@And('^I register and transition to a new shopper$')
	static void registerNewShopperAndTransitionToIt() {
		def registeredShopperUsername = UUID.randomUUID().toString() + "@elasticpath.com"
		client.GET("/")
				.newaccountform()
				.registeraction("family-name": "fname", "given-name": "gname", "password": PASSWORD, "username": registeredShopperUsername)
		client.roleTransitionToRegisteredUserByName(DEFAULT_SCOPE, registeredShopperUsername)
				.stopIfFailure()
	}

	@Given('^I authenticate with (.+) username (.+) and password (.+) and role (.+) in scope (.+)$')
	static void LoginWithNamePasswordRoleAndScope(def scenario, def username, def password, def role, def scope) {
		client.authenticate(username, password, scope, role)
	}

	@Then('^I invalidate the authentication$')
	static void invalidateAuthentication() {
		client.invalidateAuthentication()
	}

	@Then('^I set (.+) header (.+)$')
	static void setHeaderValue(String header, String value) {
		Map<String, String> headers = new HashMap<String, String>()
		headers.put(header, value)
		client.setHeaders(headers)
	}

	@Then('^I add (.+) header (.+)$')
	static void addHeaderValue(String header, String value) {
		def headers = client.getHeaders()
		headers.put(header, value)
		client.setHeaders(headers)
	}

	static void registerShopper(registrationScope, familyName, givenName, password, username) {
		client.authAsAPublicUser(registrationScope)

		client.GET("registrations/$registrationScope/newaccount/form")
				.registeraction("family-name": familyName, "given-name": givenName, "password": password, "username": username)
	}
}
