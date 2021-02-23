package com.elasticpath.cortex.dce.totals

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist

import cucumber.api.java.en.And
import cucumber.api.java.en.Then

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Cart

class TotalsSteps {

	@Then('^I retrieve cart lineitem for (.+)$')
	static void retrieveCartLineItemForProduct(def itemName) {
		Cart.getCart()
		Cart.findCartElementByProductName(itemName)
	}

	@And('^I follow the total link')
	static void followTotalLink() {
		CommonMethods.total()
	}

	@And('^the line item purchase price fields has amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyLineitemPurchasePriceHasFields(String expectedAmount, String expectedCurrency, String expectedDisplay) {
		def purchasePriceElement = client.price().body."purchase-price"[0]
		CommonAssertion.assertCost(purchasePriceElement, expectedAmount, expectedCurrency, expectedDisplay)
	}

	@Then('^the line item total link is missing$')
	static void verifyLineItemTotalLinkIsMissing() {
		assertLinkDoesNotExist(client, "total")
	}

	@Then('^I retrieve the cart total$')
	static void clickCartTotalLink() {
		Cart.total()
	}

}
