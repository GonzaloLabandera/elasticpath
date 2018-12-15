package com.elasticpath.cortex.dce.customerSegments

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.Item

class CustomerSegmentSteps {

	def static final CUSTOMER_SEGMENT_CONTENT_RELATIVE_LOCATION = "images/gift_bag.gif"
	def static final USER_TRAITS_HEADER = "x-ep-user-traits"

	@When('^I am a member of customer segment (.+?)$')
	static void setCustomerSegment(String valueUserTraits) {
		client.headers.put(USER_TRAITS_HEADER, "CUSTOMER_SEGMENT=" + valueUserTraits)
	}

	@Then('^the customer segment promotion discount is (.+)$')
	static void verifySegmentPromotionDiscount(String expectedDiscountDisplay) {
		Cart.discount()
		def actualCustSegDiscountDisplay = client.body.discount[0].display
		assertThat(actualCustSegDiscountDisplay)
				.as("The customer segment promotion discount is not as expected.")
				.isEqualTo(expectedDiscountDisplay)
	}

	@When('^I go to item price$')
	static void getItemPrice() {
		client.price()
	}

	@And('^I view the line item price$')
	static void getLineitemPrice() {
		client.item()
				.price()
				.stopIfFailure()
	}

	@And('^the line-item has list amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyLineitemHasListAmount(String listAmount, String listCurrency, String listDisplay) {
		def listPrice = client.body.'list-price'
		CommonAssertion.assertCost(listPrice, listAmount, listCurrency, listDisplay)
	}

	@And('^the item list price currency is (.+) and the display is (.+)$')
	static void verifyItemListPrice(String listCurrency, String listDisplay) {
		Item.price()

		def listPrice = client.body.'list-price'
		String listAmount = client.body.'list-price'[0].amount
		CommonAssertion.assertCost(listPrice, listAmount, listCurrency, listDisplay)
	}

	@And('^the item purchase price currency is (.+) and the display is (.+)$')
	static void verifyItemPurchasePrice(String purchaseCurrency, String purchaseDisplay) {
		def purchasePrice = client.body.'purchase-price'
		String purchaseAmount = client.body.'purchase-price'[0].amount
		CommonAssertion.assertCost(purchasePrice, purchaseAmount, purchaseCurrency, purchaseDisplay)
	}

	@And('^the line-item has purchase amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyLineitemHasPurchaseAmount(String purchaseAmount, String purchaseCurrency, String purchaseDisplay) {
		def purchasePrice = client.body.'purchase-price'
		CommonAssertion.assertCost(purchasePrice, purchaseAmount, purchaseCurrency, purchaseDisplay)
	}

	@Then('^the item (.+) list price is (.+) and purchase price is (.+)$')
	static void verifyItemPrices(String itemName, String expectedListPrice, String expectedPurchasePrice)  {
		Cart.getCart()
		Cart.findCartElementByProductName(itemName)
		Item.price()

		def listPrice = client.body.'list-price'[0].display
		def purchasePrice = client.body.'purchase-price'[0].display
		assertThat(listPrice)
				.as("The list price is not as expected")
				.isEqualTo(expectedListPrice)
		assertThat(purchasePrice)
				.as("The purchase price is not as expected")
				.isEqualTo(expectedPurchasePrice)
	}

	@Given('^a customer segment (?:promotion|price list assignment) exists.*$')
	static void verifySegmentAssignment() { }

	@Given('^an item .* with purchase price of .*$')
	static void verifyItemWithPrice() { }
}
