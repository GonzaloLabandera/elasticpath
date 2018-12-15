package com.elasticpath.cortex.dce.discounts

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Purchase

class DiscountSteps {

	def static PURCHASE_URI

	@Then('^the line item triggering (?:.+) has purchase price of (.+)$')
	static void verifyLineitemPurchasePrice(def value) {
		client.GET("/")
				.defaultcart()
				.total()
		assertThat(client.body.cost[0]["display"])
				.as("Purchase price is not as expected")
				.isEqualTo(value)
	}

	@Then('^the cart discount fields has amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyCartDiscountHasValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		Cart.discount()
		def listDiscountElement = client.body.discount
		CommonAssertion.assertCost(listDiscountElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^the cart discount has currency (.+) and display (.+)$')
	static void verifyCartDiscount(def expectedCurrency, def expectedDisplayName) {
		Cart.discount()
		def listDiscountElement = client.body.discount
		String expectedAmount = client.body.discount[0].amount
		CommonAssertion.assertCost(listDiscountElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@Then('^the cart discount amount is (.+?)$')
	static void verifyCartDiscountAmount(String expectedDisplay) {
		Cart.discount()
		def listDiscountElement = client.body.discount
		assertThat(listDiscountElement["display"][0])
				.as("Display amount is not as expected")
				.isEqualTo(expectedDisplay)
	}

	@Then('^the cart total is unaffected by the shipping discount and has value (.+)$')
	static void verifyCartTotalValue(def value) {
		Cart.total()
		assertThat(client.body.cost[0]["display"])
				.as("Cart total is not as expected")
				.isEqualTo(value)
	}

	@Then('^I can traverse back to the cart following a link$')
	static void followCartLink() {
		client.cart()
				.stopIfFailure()
	}

	@Then('^I retrieve the purchase$')
	static void getPurchaseURI() {
		Order.submitPurchase()
		PURCHASE_URI = client.body.self.uri
	}

	@Then('^the purchase discount amount is (.+)$')
	static void verifyPurchaseDiscountAmount(def expectedDisplay) {
		Purchase.resume()
		Purchase.discount()
		def costElement = client.body.'discount'
		assertThat(costElement["display"][0])
				.as("Display amount is not as expected")
				.isEqualTo(expectedDisplay)
	}

	@Then('^the purchase total after discount is (.+)$')
	static void verifyPurchaseTotalAfterDiscount(def expectedDisplay) {
		Purchase.resume()
		def costElement = client.body."monetary-total"
		assertThat(costElement["display"][0])
				.as("Display amount is not as expected")
				.isEqualTo(expectedDisplay)
	}

	@Then('^the line item (.+?) that had (?:.+) discount has the amount (.+)$')
	static void verifyLineitemHasAmount(def itemName, def expectedDisplay) {
		Purchase.resume()
		Purchase.findPurchaseItemByProductName(itemName)
		def costElement = client.body."line-extension-amount"
		assertThat(costElement["display"][0])
				.as("Display amount is not as expected")
				.isEqualTo(expectedDisplay)
	}

	@Given('^item (?:.+) triggers the (?:.+) promotion (?:.+)$')
	static void checkItemTriggersPromotion() { }

	@Given('^item (?:.+) has a purchase price of (?:.+)$')
	static void verifyItemPurchasePrice() { }
}
