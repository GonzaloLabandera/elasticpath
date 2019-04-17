package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN_DISPLAY_NAME
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Profile.
 */
class Profile extends CommonMethods {


	static void getProfile() {
		client.GET("/")
				.defaultprofile()
				.stopIfFailure()
		CortexResponse.profileResponse = client.save()
	}

	static void resume() {
		if (CortexResponse.profileResponse == null) {
			getProfile()
		}
		client.resume(CortexResponse.profileResponse)
	}

	static def getFamilyName() {
		getProfile()
		return client["family-name"]
	}

	static def getGivenName() {
		getProfile()
		return client["given-name"]
	}

	static void updateProfile(def familyName, def firstName) {
		getProfile()
		updateProfile(client.body.self.uri, familyName, firstName)
	}

	static void updateProfile(def uri, def familyName, def givenName) {
		client.PUT(uri,
				[
						"family-name": familyName,
						"given-name" : givenName
				]
		)
	}

	static void updateProfile(def jsonInput) {
		getProfile()
		client.PUT(client.body.self.uri, jsonInput)
	}

	static void addresses() {
		getProfile()
		client.addresses()
				.stopIfFailure()
	}

	static void datapolicies() {
		getProfile()
		client.'data-policies'()
				.stopIfFailure()
	}

	static void datapolicyconsentform() {
		client.datapolicyconsentform()
				.stopIfFailure()
	}


	static void paymentmethods() {
		getProfile()
		client.paymentmethods()
				.stopIfFailure()
	}

	static void addressform() {
		addresses()
		client.addressform()
				.stopIfFailure()
	}

	static void billingaddresses() {
		addresses()
		client.billingaddresses()
				.stopIfFailure()
	}

	static void shippingaddresses() {
		addresses()
		client.shippingaddresses()
				.stopIfFailure()
	}

	static void emailform() {
		getProfile()
		client.emails()
				.emailform()
				.stopIfFailure()
	}

	static void createAddress(String countryCode, String extendedAddress, String locale, String organization, String phoneNumber, String postalCode, String regionCode,
							  String streetAddress, String familyName, String givenName) {
		addressform()

		client.createaddressaction(
				[
						address: ["country-name"    : countryCode,
								  "extended-address": extendedAddress,
								  "locality"        : locale,
								  "organization"    : organization,
								  "phone-number"    : phoneNumber,
								  "postal-code"     : postalCode,
								  "region"          : regionCode,
								  "street-address"  : streetAddress],
						name   : ["family-name": familyName,
								  "given-name" : givenName]
				]
		)
				.stopIfFailure()
	}

	static void createUniqueAddress() {
		def randomAddress = UUID.randomUUID().toString() + "random street"

		createAddress("CA", "", "Vancouver", "", "", "V7V7V7", "BC",
				randomAddress, "itest", "generated")
	}

	static void updateAddress(String toBeModifiedAddressPostalCode, String countryCode, String extendedAddress, String locale, String organization, String phoneNumber, String postalCode, String regionCode,
							  String streetAddress, String familyName, String givenName) {

		getAddressWithPostalCode(toBeModifiedAddressPostalCode)

		client.PUT(client.body.self.uri,
				[
						address: ["country-name"    : countryCode,
								  "extended-address": extendedAddress,
								  "locality"        : locale,
								  "organization"    : organization,
								  "phone-number"    : phoneNumber,
								  "postal-code"     : postalCode,
								  "region"          : regionCode,
								  "street-address"  : streetAddress],
						name   : ["family-name": familyName,
								  "given-name" : givenName]])
	}

	static void createAddressWithInvalidAddressKey(String countryCode, String locale, String postalCode, String regionCode,
												   String streetAddress, String familyName, String givenName) {

		addressform()
		client.createaddressaction(
				[
						invalidAddressKey: ["country-name"  : countryCode,
											"locality"      : locale,
											"postal-code"   : postalCode,
											"region"        : regionCode,
											"street-address": streetAddress],
						name             : ["family-name": familyName,
											"given-name" : givenName]
				]
		)

	}

	static void createAddressWithInvalidNameKey(String countryCode, String locale, String postalCode, String regionCode,
												String streetAddress, String familyName, String givenName) {

		addressform()
		client.createaddressaction(
				[
						address       : ["country-name"  : countryCode,
										 "locality"      : locale,
										 "postal-code"   : postalCode,
										 "region"        : regionCode,
										 "street-address": streetAddress],
						invalidNameKey: ["family-name": familyName,
										 "given-name" : givenName]
				]
		)

	}

	static void addCanadianBillingAddress() {
		createAddress("CA", "", "Vancouver", "", "", "V1V 2K2", "BC",
				"123 Somestreet", "User", "Test")
		assert client.response.status == 201
	}

	static void addUSBillingAddress(){
		createAddress("US", "", "Seattle", "", "", "98119", "WA",
				"555 Elliott Avenue W", "User", "Test")
		assert client.response.status == 201
	}

	static void getAddressWithPostalCode(postalCode) {
		def addressExists = false
		addresses()
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				if (client["address"]["postal-code"] == postalCode) {
					return addressExists = true
				}
			}
		}
		assertThat(addressExists)
				.as("Unable to find $postalCode")
				.isTrue()
	}

	static def getDefaultBillingAddressUri() {
		billingaddresses()
		return client.body.self.uri + "/default"
	}

	static def getDefaultShippingAddressUri() {
		shippingaddresses()
		return client.body.self.uri + "/default"
	}

	static void selectDataPolicy(def policyName) {
		datapolicies()
		def policyExist = false
		client.body.links.find {
			if (it.rel == 'element') {
				client.GET(it.href)
						.stopIfFailure()
				if (client.body.'policy-name' == policyName) {
					return policyExist = true
				}
			}
		}
		assertThat(policyExist)
				.as("Unable to find the given data policy name - $policyName")
				.isTrue()
	}

	static void addDefaultToken() {
		addToken(TEST_TOKEN_DISPLAY_NAME, TEST_TOKEN)
	}

	static void addToken(String displayName, String token) {
		getProfile()
		client.paymentmethods()
				.paymenttokenform()
				.createpaymenttokenaction(
				['display-name': displayName,
				 'token'       : token]
		)
				.follow()
				.stopIfFailure()
	}

	static void defaultBillingAddress() {
		addresses()
		client.billingaddresses()
				.default()
				.stopIfFailure()
	}

	static void defaultShippingAddress() {
		shippingaddresses()
		client.default()
				.stopIfFailure()
	}

	static void defaultPaymentMethod() {
		paymentmethods()
		client.default()
				.stopIfFailure()
	}

	static def getDefaultPaymentMethodDisplayName() {
		defaultPaymentMethod()
		return getDisplayName()
	}

	static void verifyToken(final String tokenDisplayName) {
		assertThat(findToken(tokenDisplayName))
				.as("Unable to find token: $tokenDisplayName")
				.isTrue()
	}

	static def findToken(final String tokenDisplayName) {
		def tokenExists = false
		paymentmethods()
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				if (tokenDisplayName == getDisplayName()) {
					return tokenExists = true
				}
			}
		}
		return tokenExists
	}

	static void deleteToken(final String tokenDisplayName) {
		verifyToken(tokenDisplayName)
		delete(client.body.self.uri)
	}

	static void navigateToAddEmailForm() {
		emailform()
	}

	static void addEmailWithoutFollow(String userName) {
		navigateToAddEmailForm()
		client.createemailaction("email": userName)
				.stopIfFailure()
	}

	static void addEmail(String userName) {
		addEmailWithoutFollow(userName)
		client.follow()
				.stopIfFailure()
	}

	static void wishlists() {
		getProfile()
		client.wishlists()
				.stopIfFailure()
	}

}
