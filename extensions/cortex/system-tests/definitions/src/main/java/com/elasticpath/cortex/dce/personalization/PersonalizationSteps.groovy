package com.elasticpath.cortex.dce.personalization

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import org.apache.commons.lang3.StringUtils

import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item

class PersonalizationSteps {

	@Given('^(.+) has an original purchase price equal to (.+)$')
	static void verifyItemPurchasePrice(String movieName, String expectedPrice) {
		searchPriceByMovieName(movieName)
		verifyProductPurchasePrice(expectedPrice)
	}

	@When('^I append to the overwritten personalization header the key (.+) and value (.+)$')
	static void addPersonalizationHeader(String personalisationKey, String personalisationValue) {
		addPersonalisationHeader(personalisationKey, personalisationValue)
	}

	@And('^I request the purchase price for item (.+)$')
	static void getItemPurchasePrice(String movieName) {
		searchPriceByMovieName(movieName)
	}

	@And('^I request the purchase price in scope (.+) for item (.+)$')
	static void getItemPurchasePriceInScope(String scope, String movieName) {
		searchPriceByMovieName(movieName)
	}

	@Then('^I get the purchase price equal to (.+)$')
	static void verifyItemPurchasePriceValue(String price) {
		verifyProductPurchasePrice(price);
	}

	private static void searchPriceByMovieName(final String movieName) {
		FindItemBy.productName(movieName)
		Item.price()
	}

	private static void verifyProductPurchasePrice(final String price) {
		assertThat(client.body."purchase-price"[0]["display"])
				.as("The purchase price is not as expected")
				.isEqualTo(price)
	}

	private static void addPersonalisationHeader(final String personalisationKey, final String personalisationValue) {
		String headerKey = "x-ep-user-traits";
		String headerValue = personalisationKey + "=" + personalisationValue;

		String existingHeaderValue = client.headers.getAt(headerKey);
		if (StringUtils.isNotBlank(existingHeaderValue)) {
			headerValue = existingHeaderValue + ", " + headerValue;
		}

		client.headers.putAt(headerKey, headerValue)
	}
}
