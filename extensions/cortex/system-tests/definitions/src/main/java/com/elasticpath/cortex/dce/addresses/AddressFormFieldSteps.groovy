package com.elasticpath.cortex.dce.addresses

import static org.assertj.core.api.Assertions.assertThat

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.cortex.dce.addresses.AddressConstants.ADDRESS_LINK_TYPE

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Profile

class AddressFormFieldSteps {

	@Then('^Response\'s self link has type addresses$')
	static void verifyResponseType() {
		Profile.addressform();
		assertThat(client.body.self.type)
				.as("Self link is not as expected")
				.isEqualTo(ADDRESS_LINK_TYPE)
	}

	@And('^link has no type$')
	static void verifyLinkHasNoType() {
		assertThat(client.body.links[0].type)
				.as("Link type is not as expected")
				.isEqualTo(null)
	}

	@And('^link has rel (.+)')
	static void verifyLinkHasRel(String expectedRel) {
		assertThat(client.body.links[0].rel)
				.as("Link rel is not as expected")
				.isEqualTo(expectedRel)
	}

	@And('^link does not have rev$')
	static void verifyLinkHasNoRev() {
		assertThat(client.body.links[0].rev)
				.as("Link rev is not as expected")
				.isEqualTo(null)
	}

	@And('^link has uri to addresses$')
	static void verifyLinkHasAddressLink() {
		String link_uri = client.body.links[0].href;
		String last_part = link_uri.split("/")[-1];
		assertThat(last_part)
				.as("Link is not as expected")
				.isEqualTo(DEFAULT_SCOPE)
	}

	@When('^I create address with Country (.*), Extended-Address (.*), Locality (.*), Organization (.*), Phone-Number (.*), Postal-Code (.*), Region (.*), Street-Address (.*), Family-Name (.*) and Given-Name (.*)$')
	static void createNewAddressWithParameters(String countryCode,
											   String extendedAddress,
											   String locale,
											   String organization,
											   String phoneNumber,
											   String postalCode,
											   String regionCode,
											   String streetAddress,
											   String familyName,
											   String givenName) {
		Profile.createAddress(countryCode, extendedAddress, locale, organization, phoneNumber, postalCode, regionCode, streetAddress, familyName, givenName);
	}

	@When('^I modify existing address with postal code (.+) with Country (.*), Extended-Address (.*), Locality (.*), Organization (.*), Phone-Number (.*), Postal-Code (.*), Region (.*), Street-Address (.*), Family-Name (.*) and Given-Name (.*)$')
	static void modifyExistingAddressWithParameters(String toBeModifiedAddressPostalCode,
													String countryCode,
													String extendedAddress,
													String locale,
													String organization,
													String phoneNumber,
													String postalCode,
													String regionCode,
													String streetAddress,
													String familyName,
													String givenName) {
		Profile.updateAddress(toBeModifiedAddressPostalCode, countryCode, extendedAddress, locale, organization, phoneNumber, postalCode, regionCode, streetAddress, familyName, givenName);
	}

	@When('^I create address with invalid address key with Country (.*), Locality (.*), Postal-Code (.*), Region (.*), Street-Address (.*), Family-Name (.*) and Given-Name (.*)$')
	static void createNewAddressWithInvalidAddressKey(String countryCode,
													  String locale,
													  String postalCode,
													  String regionCode,
													  String streetAddress,
													  String familyName,
													  String givenName) {
		Profile.createAddressWithInvalidAddressKey(countryCode, locale, postalCode, regionCode, streetAddress, familyName, givenName);
	}

	@When('^I create address with invalid name key with Country (.*), Locality (.*), Postal-Code (.*), Region (.*), Street-Address (.*), Family-Name (.*) and Given-Name (.*)$')
	static void createNewAddressWithInvalidNamedKey(String countryCode,
													String locale,
													String postalCode,
													String regionCode,
													String streetAddress,
													String familyName,
													String givenName) {
		Profile.createAddressWithInvalidNameKey(countryCode, locale, postalCode, regionCode, streetAddress, familyName, givenName);
	}

	@Then('^the address with postal code (.+) should match the following (.+) values?$')
	static void verifyAddressContainsValues(String postalCode, String addressFormNode, DataTable dataTable) {
		Profile.getAddressWithPostalCode(postalCode)

		def mapList = dataTable.asMap(String, String)

		for (def map : mapList) {
			def key = map.getKey()
			def value = map.getValue()

			assertThat(client.body."$addressFormNode"."$key")
					.as("Expected $key does not match")
					.isEqualTo(value)
		}
	}

	@Then('^the address with postal code (.+) should contain the top level values$')
	static void verifyAddressContainsTopLevelValues(String postalCode, DataTable dataTable) {
		Profile.getAddressWithPostalCode(postalCode)

		def mapList = dataTable.asMap(String, String)

		for (def map : mapList) {
			def key = map.getKey()
			def value = map.getValue()

			assertThat(client.body."$key")
					.as("Expected $key does not match")
					.isEqualTo(value)
		}
	}

	@Then('^the address matches the following$')
	static void verifyAddressMAtchesData(DataTable dataTable) {

		def mapList = dataTable.asMap(String, String)
		for (def map : mapList) {
			def key = map.getKey()
			def value = map.getValue()

			assertThat(client.body.address."$key")
					.as("Expected $key does not match")
					.isEqualTo(value)
		}
	}
}
