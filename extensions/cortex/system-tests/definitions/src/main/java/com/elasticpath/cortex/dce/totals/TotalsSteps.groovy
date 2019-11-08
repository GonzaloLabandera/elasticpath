package com.elasticpath.cortex.dce.totals

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.java.en.And
import cucumber.api.java.en.Then

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Cart

class TotalsSteps {

	@Then('^I follow the total link from the cart lineitem for (.+)$')
	static void clickLineitemTotalLink(def itemName) {
		Cart.getCart()
		Cart.findCartElementByProductName(itemName)
		CommonMethods.total()
	}

	@And('^the line item purchase price fields has amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyLineitemPurchasePriceHasFields(String expectedAmount, String expectedCurrency, String expectedDisplay) {
		def purchasePriceElement = client.price().body."purchase-price"[0]
		CommonAssertion.assertCost(purchasePriceElement, expectedAmount, expectedCurrency, expectedDisplay)
	}

	@Then('^I retrieve the cart total$')
	static void clickCartTotalLink() {
		Cart.total()
	}

}
