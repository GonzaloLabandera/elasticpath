/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
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

	static void getProfileAttributes() {
		getProfile()
		client.attributes()
				.stopIfFailure()
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
						"organization": organization,
						"phone-number": phoneNumber,
						address       : ["country-name"    : countryCode,
										 "extended-address": extendedAddress,
										 "locality"        : locale,
										 "postal-code"     : postalCode,
										 "region"          : regionCode,
										 "street-address"  : streetAddress],
						name          : ["family-name": familyName,
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

		putAddress(organization, phoneNumber, countryCode, extendedAddress, locale, postalCode, regionCode, streetAddress, familyName, givenName)
	}

	static void putAddress(String organization, String phoneNumber, String countryCode, String extendedAddress, String locale, String postalCode, String regionCode, String streetAddress, String familyName, String givenName) {
		client.PUT(client.body.self.uri,
				[
						"organization": organization,
						"phone-number": phoneNumber,
						address       : ["country-name"    : countryCode,
										 "extended-address": extendedAddress,
										 "locality"        : locale,
										 "postal-code"     : postalCode,
										 "region"          : regionCode,
										 "street-address"  : streetAddress],
						name          : ["family-name": familyName,
										 "given-name" : givenName]])
	}

	static void createAddressWithInvalidAddressKey(String countryCode, String locale, String postalCode, String regionCode, String streetAddress,
												   String organization, String phoneNumber, String familyName, String givenName) {

		addressform()
		client.createaddressaction(
				[
						"organization": organization,
						"phone-number": phoneNumber,
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

	static void addGBBillingAddress() {
		createAddress("GB", "", "London", "", "", "12345", "",
				"111 Main Street", "GB User", "GB test")
		assert client.response.status == 201
	}


	static void addUSBillingAddress() {
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

	static void defaultPaymentInstrument() {
		paymentinstruments()
		client.default()
				.stopIfFailure()
	}

	static def getDefaultPaymentInstrumentName() {
		defaultPaymentInstrument()
		return client["name"]
	}

	static void verifyInstrument(final String instrumentName) {
		assertThat(findInstrument(instrumentName))
				.as("Unable to find instrument: $instrumentName")
				.isTrue()
	}

	static def findInstrument(final String instrumentName) {
		def instrumentExists = false
		paymentinstruments()
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				if (instrumentName == client["name"]) {
					return instrumentExists = true
				}
			}
		}
		return instrumentExists
	}

	static def isDefaultPaymentInstrument(final String instrumentName) {
		paymentinstruments()
		client.body.links.find {
			if (it.rel == "default") {
				client.GET(it.uri)
				if (instrumentName == client["name"]) {
					return true
				}
			}
		}
		return false
	}

	static void deleteInstrument(final String instrumentName) {
		verifyInstrument(instrumentName)
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

	static void paymentinstruments() {
		getProfile()
		client.paymentinstruments()
				.stopIfFailure()
	}

	static void paymentInstrumentsFromDefaultPaymentInstrumentSelector() {
		client.paymentinstruments()
				.stopIfFailure()
	}

	static void paymentInstrumentsChoicesFromChoice() {
		client.selector()
				.stopIfFailure()
	}

	static void getPaymentConfigurationWithName(String configurationName) {
		paymentmethods()
		openLinkRelWithFieldWithValue("element", "name", configurationName)
	}

	static void paymentInstrumentChoiceDescription() {
		defaultinstrumentselector()
		client.choice()
				.description()
				.stopIfFailure()
	}

	static void paymentInstrumentChoice() {
		defaultinstrumentselector()
		client.choice()
				.stopIfFailure()
	}

	static void chosenPaymentInstrument() {
		defaultinstrumentselector()
		client.chosen()
				.stopIfFailure()
	}

	static void chosenPaymentInstrumentDescription() {
		chosenPaymentInstrument()
		client.description()
				.stopIfFailure()
	}

	static void defaultinstrumentselector() {
		getProfile()
		paymentinstruments()
		client.defaultinstrumentselector()
				.stopIfFailure()
	}

}
