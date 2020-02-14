package com.elasticpath.cortex.dce.hal

import static org.assertj.core.api.Assertions.assertThat

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

class HalFormatSteps {

	@When('^I open root in HAL format$')
	static void getRoot() {
		client.GET("/")
				.stopIfFailure()
	}

	@When('^I open navigations in HAL format$')
	static void getNavigation() {
		client.GET("/")
				.stopIfFailure()
		client.GET(client.body._links.navigations.href)
				.stopIfFailure()
	}

	@When('^I open order in HAL format$')
	static void getOrder() {
		client.GET("/")
				.stopIfFailure()
		client.GET(client.body._links.defaultcart.href)
				.stopIfFailure()
		client.GET(client.body._links.order.href)
				.stopIfFailure()
	}

	@And('^I should see the (.+) link in HAL format$')
	static void verifyLinkInHALFormat(String linkKey) {
		def link = client.body._links[linkKey]
		assertThat(link)
				.as("\"$linkKey\" link property not found")
				.isNotNull()
		linkIsHAL(link)
	}

	@Then('^I should see a list of links mapped to (.+) and not to (.+)$')
	static void verifyLinkMapping(String correctLinkKey, String incorrectLinkKey) {
		assertThat(client.body[correctLinkKey])
				.as("list of links not found under \"$correctLinkKey\"")
				.isNotNull()
				.isNotEmpty()

		assertThat(client.body[incorrectLinkKey])
				.as("list of links found under \"$incorrectLinkKey\" should be under \"$correctLinkKey\"")
				.isNull()
	}

	@And('^each link in the list should be in HAL format$')
	static void verifyLinksInHALFormat() {
		client.body._links.each { key, value ->
			linkIsHAL(value)
		}
	}

	@And('^I should see an array of links mapped to (.+)$')
	static void verifyLinksMappedTo(String key) {
		def linkArr = client.body._links[key]
		assertThat(linkArr)
				.as("list of links not found")
				.isNotNull()
				.isNotEmpty()
	}

	@And('^each link in the element array should be in HAL format$')
	static void verifyElementArrayLinksHALFormat() {
		client.body._links.element.each { link ->
			linkIsHAL(link)
		}
	}

	@And('^the list should exactly contain the following links$')
	static void verifyListContainsLinks(DataTable linkTable) {
		def linkList = linkTable.asList(String)
		def linkKeys = client.body._links.keySet()

		assertThat(linkKeys)
				.hasSameElementsAs(linkList)
	}

	@Then('^I should see the (.+) property$')
	static void verifyPropertyByName(String jsonPropertyName) {
		assertThat(client.body[jsonPropertyName])
				.as("\"$jsonPropertyName\" property not found")
				.isNotNull()
	}

	@Then('^I should not see the (.+) property$')
	static void verifyPropertyNotDisplayed(String jsonPropertyName) {
		assertThat(client.body[jsonPropertyName])
				.as("\"$jsonPropertyName\" property found but should not be found")
				.isNull()
	}

	@When('^I post to address form with Country (.*), Extended-Address (.*), Locality (.*), Organization (.*), Phone-Number (.*), Postal-Code (.*), Region (.*), Street-Address (.*), Family-Name (.*) and Given-Name (.*)$')
	static void createAddressWithFollowingData(String countryCode,
											   String extendedAddress,
											   String locale,
											   String organization,
											   String phoneNumber,
											   String postalCode,
											   String regionCode,
											   String streetAddress,
											   String familyName,
											   String givenName) {
		goToAddressForm()
		def addressformHref = client.body._links.self.href + "?followlocation"

		client.POST(addressformHref, [
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
		])
	}

	@Then('^I should see the (.+) from the POST$')
	static void verifyPOSTContains(def addressFormNode, DataTable dataTable) {
		def mapList = dataTable.asMap(String, String)
		for (def map : mapList) {
			def key = map.getKey()
			def value = map.getValue()

			assertThat(client.body."$addressFormNode"."$key")
					.as("Expected $key does not match")
					.isEqualTo(value)
		}
	}

	@Then('^I should see top level values from the POST$')
	static void verifyPOSTContains(DataTable dataTable) {
		def mapList = dataTable.asMap(String, String)
		for (def map : mapList) {
			def key = map.getKey()
			def value = map.getValue()

			assertThat(client.body."$key")
					.as("Expected $key does not match")
					.isEqualTo(value)
		}
	}

	static void goToAddressForm() {
		client.GET("/")
				.stopIfFailure()
		client.GET(client.body._links.defaultprofile.href)
				.stopIfFailure()
		client.GET(client.body._links.addresses.href)
				.stopIfFailure()
		client.GET(client.body._links.addressform.href)
				.stopIfFailure()
	}

	static void linkIsHAL(link) {
		assertThat(link.name)
				.as("link \"name\" property not found")
				.isNotNull()
		assertThat(link.href)
				.as("link \"href\" property not found")
				.isNotNull()
		assertThat(link.rel)
				.as("links should not have the \"rel\" property")
				.isNull()
		assertThat(link.rev)
				.as("links should not have the \"rev\" property")
				.isNull()
		assertThat(link.type)
				.as("links should not have the \"type\" property")
				.isNull()
		assertThat(link.uri)
				.as("links should not have the \"uri\" property")
				.isNull()
	}
}
