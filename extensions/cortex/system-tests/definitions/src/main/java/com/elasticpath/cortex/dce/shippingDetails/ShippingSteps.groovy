package com.elasticpath.cortex.dce.shippingDetails

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.en.Given
import cucumber.api.java.en.Then

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortexTestObjects.Order

class ShippingSteps {

	@Given('^an item with cost of (.+) and shipping cost is (.+) percent of order total$')
	static void verifyShippingCostPercentOfItemPrice(cost, percentage) {
		//	non-implementation step
	}

	@Given('^a shipping option (.+) has cost of (.+)$')
	static void verifyShippingOptionCost(shippingOption, amount) {
		//	non-implementation step
	}

	@Given('^a shipping promotion (.+) for the shipping option (.+)$')
	static void verifyShippingOptionHasPromotion(promotion, shippingOption) {
		//	non-implementation step
	}

	@Given('^I select a shipping option (.+)$')
	static void selectShippingOption(String optionName) {
		Order.getShippingServiceLevel(optionName)
	}

	@Then('^the shipping cost has fields amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyShippingOptionHasFields(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def listCostElement = client.body.cost[0]
		CommonAssertion.assertCost(listCostElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}
}
