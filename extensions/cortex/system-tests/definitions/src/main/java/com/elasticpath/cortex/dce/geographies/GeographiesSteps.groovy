package com.elasticpath.cortex.dce.geographies

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.currentScope
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.rest.id.util.Base32Util.encode
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortexTestObjects.Geographies
import com.elasticpath.cortexTestObjects.Profile

class GeographiesSteps {

	static String countryCode
	static String regionCode

	@Given('^there is a list of (?:|.+ )supported (?:countries|countries and regions) for scope (.+)$')
	static void verifyListOfSupportedCountries(def storeScope) {
		// Lists of countries and regions is configured in properties files in CE.
		client.authAsAPublicUser(storeScope)
				.stopIfFailure()
	}

	@Given('^there is a list of (?:.+) supported regions for (?:.+) in scope (.+)$')
	static void verifyListOfSupportedRegions(def storeScope) {
		// Lists of countries and regions is configured in properties files in CE.
		client.authAsAPublicUser(storeScope)
				.stopIfFailure()
	}

	@Given('^scope (.+) supports (.+) language$')
	static void verifyScopeSupportsLanguage(String storeScope, String language) {
		client.authAsAPublicUser(storeScope)
				.stopIfFailure()
	}

	@And('^one of the supported countries is (.+)$')
	static void verifyCountryIsSupported(def supportedCountry) {
		// Countries has been defined in the backend system (CE)
	}

	@Given('^one of the supported regions for (.+) is (.+)$')
	static void verifyCountrySupportsRegion(def country, def region) {
		// List of regions has been defined in the backend system (CE)
	}

	@Given('^there are no supported regions for (.+)$')
	static void verifyNoSupportedRegionsForCountry(def countryName) {
		// List of regions has been defined in the backend system (CE)
		client.authAsAPublicUser(DEFAULT_SCOPE)
				.stopIfFailure()
	}

	@When('^the country Canada and the region British Columbia is selected$')
	static void verifyRegionBCForCanadaSelected() {
		Geographies.getGeographies()
		Geographies.verifyCountryByDisplayName("Canada")
		countryCode = client["name"]
		Geographies.regions()
		Geographies.verifyRegionByDisplayName("British Columbia")
		regionCode = client["name"]
	}

	@When('^I request the list of countries$')
	static void getListOfCountries() {
		Geographies.getGeographies()
	}

	@When('^I request region (.+)$')
	static void getRegionByID(def regionId) {
		def CanadaCountryCode = "CA"
		client.GET("/geographies/$currentScope/countries/${encode(CanadaCountryCode)}/regions/${encode(regionId)}")
				.stopIfFailure()
	}

	@When('^I request a region with an undecodable id$')
	static void getRegionWithInvalidID() {
		def CanadaCountryCode = "CA"
		def undecodableId = "2="
		client.GET("/geographies/$currentScope/countries/${encode(CanadaCountryCode)}/regions/${undecodableId}")
				.stopIfFailure()
	}

	@When('^I request the list of countries in language (.+) in scope (.+)$')
	static void getListOfCountriesByLanguageInScope(def locale, def storeScope) {
		client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
		Geographies.getGeographies()
	}

	@When('^I request the list of sub-countries in language (.+) in scope (.+)$')
	static void getListOfSubCountriesByLanguageInScope(def locale, def storeScope) {
		client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
		Geographies.getGeographies(storeScope)
		Geographies.verifyCountryByCode("CA")
		Geographies.regions()
	}

	@When('^I request the list of countries in scope (.+)$')
	static void getListOfCountriesInScope(def storeScope) {
		client.GET("/geographies/${storeScope}/countries")
				.stopIfFailure()
	}

	@When('^I request the list of regions for Canada in scope (.+)$')
	static void getListOfCanadianRegionsInScope(def storeScope) {
		Geographies.getGeographies(storeScope)
		Geographies.verifyCountryByCode("CA")
		Geographies.regions()
	}

	@When('^I request the list of regions for Japan$')
	static void getListOfJapaneseRegions() {
		Geographies.getGeographies()
		Geographies.verifyCountryByCode("JP")
		Geographies.regions()
	}

	@Then('^I can obtain the country and region code to create an address$')
	static void verifyCountryAndRegionCodesAvailable() {

		Profile.createAddress(countryCode, "", "Vancouver", "", "", "V7V7V7", regionCode,
				"1234 Hello World", "itest created", "address")

		assertThat(client.response.status)
				.as("HTTP status is not as expected")
				.isEqualTo(201)
		client.follow()
				.stopIfFailure()

		assertThat(client.body.address."country-name")
				.as("Country is not as expected")
				.isEqualTo(countryCode)
		assertThat(client.body.address."region")
				.as("Region is not as expected")
				.isEqualTo(regionCode)
	}

	@Then('^I get back all ([0-9]*) supported countries$')
	static void verifyNumberOfSupportedCountries(Integer numSupportedCountries) {
		def countries = client.body.links.findAll {
			link ->
				link.rel == "element"
		}
		assertThat(countries)
				.size()
				.as("Number of supported countries is not as expected")
				.isEqualTo(numSupportedCountries)
	}

	@Then('^one of the countries is (.+)$')
	static void getCountryByName(def countryName) {
		Geographies.verifyCountryByDisplayName(countryName)
	}

	@Then('^I get back all ([0-9]*) supported regions for Canada$')
	static void verifyNumberOfCanadianRegions(Integer numSupportedRegions) {
		def regions = client.body.links.findAll {
			link ->
				link.rel == "element"
		}
		client.stopIfFailure()
		assertThat(regions)
				.size()
				.as("Number of regions is not as expected")
				.isEqualTo(numSupportedRegions)
	}

	@Then('^I get back an empty list$')
	static void verifyListIsEmpty() {
		assertLinkDoesNotExist(client, "element")
	}

	@Then('^one of the regions is (.+)$')
	static void verifyRegionByName(def regionName) {
		Geographies.verifyRegionByDisplayName(regionName)
	}
}
