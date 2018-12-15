package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Offer.
 */
class Offer extends CommonMethods {

	static void availability() {
		client.availability()
				.stopIfFailure()
	}

	static void definition() {
		client.definition()
				.stopIfFailure()
	}

	static void code() {
		client.code()
				.stopIfFailure()
	}

	static void components() {
		client.components()
				.stopIfFailure()
	}

	static void items() {
		client.items()
				.stopIfFailure()
	}

	static void pricerange() {
		client.pricerange()
				.stopIfFailure()
	}

	static String getSkuCode() {
		code()
		return client["code"]
	}

	static String getItemName() {
		definition()
		return client["display-name"]
	}

	static void offer() {
		client.offer()
				.stopIfFailure()

	}

	static void verifyOfferPriceRange(String priceType, String priceRange, String priceValue) {
		println("Offer Price Range: " + priceType + "." + priceRange + ":" + client.body."$priceType"."$priceRange"["display"][0])

		client.body."$priceType"."$priceRange"["display"]
		assertThat(client.body."$priceType"."$priceRange"["display"][0])
			.as("Value does not match for " + priceType + ":" + priceRange)
			.isEqualTo(priceValue)
	}

}