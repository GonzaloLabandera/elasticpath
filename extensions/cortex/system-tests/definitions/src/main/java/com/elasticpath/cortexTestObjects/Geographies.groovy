package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.currentScope
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Geographies.
 */
class Geographies extends CommonMethods {


	static void getGeographies() {
		client.GET("/geographies/$currentScope/countries")
				.stopIfFailure()
	}

	static void getGeographies(def scope) {
		client.GET("/geographies/$scope/countries")
				.stopIfFailure()
	}

	private static void verifyCountryRegion(def nameCode, String key) {
		def countryExists = false
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				if (nameCode == client[key]) {
					return countryExists = true
				}
			}
		}
		assertThat(countryExists)
				.as("Unable to find country $nameCode")
				.isTrue()
	}

	static void verifyCountryByDisplayName(def countryName) {
		verifyCountryRegion(countryName, "display-name")
	}

	static void verifyCountryByCode(def countryCode) {
		verifyCountryRegion(countryCode, "name")
	}

	static void verifyRegionByDisplayName(def regionName) {
		verifyCountryRegion(regionName, "display-name")
	}

	static void verifyRegionByCode(def regionCode) {
		verifyCountryRegion(regionCode, "name")
	}

	static void regions() {
		client.regions()
				.stopIfFailure()
	}

}