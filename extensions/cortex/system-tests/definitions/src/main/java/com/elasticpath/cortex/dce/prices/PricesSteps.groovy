package com.elasticpath.cortex.dce.prices

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Item

class PricesSteps {

	@Given('^an item (.+?) exists in my catalog$')
	static void verifyItemInCatalog(def productName) {
		CommonMethods.searchAndOpenItemWithKeyword(productName)
	}

	@When('^I view the (?:item|lineitem) price$')
	static void clickPriceLink() {
		Item.price()
	}

	@When('^I follow the fromprice link on the item definition$')
	static void clickFrompriceLink() {
		client.definition()
				.fromprice()
				.stopIfFailure()
	}

	@When('^I hack a URI to attempt to GET the price for (.+?)$')
	static void modifyURIToGetItemPrice(def itemDisplayName) {
		CommonMethods.searchAndOpenItemWithKeyword(itemDisplayName)

		def itemUri = client.body.self.uri
		def hackedUri = "/prices" + itemUri

		client.GET(hackedUri)
				.stopIfFailure()
	}

	@When('^I view the pricing for the option (.+?) and value (.+)$')
	static void getItemWithOptionPrice(def option, def value) {
		Item.getItem()
		Item.selectSkuOption(option, value)
		Item.price()
	}

	@Then('^the list-price has fields amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyListPriceHasValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def listPriceElement = client.body.'list-price'
		CommonAssertion.assertCost(listPriceElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@Then('^the from-price has fields amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyFromPriceHasValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def fromPriceElement = client.body.'from-price'
		CommonAssertion.assertCost(fromPriceElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@Then('^the lineitem price has list-price fields amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyLineitemHasListPriceValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		client.price()
		def listPriceElement = client.body.'list-price'
		CommonAssertion.assertCost(listPriceElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^the lineitem price has purchase-price fields amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyLineitemPurchasePriceHasValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def costElement = client.body.'purchase-price'
		CommonAssertion.assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
		client.stopIfFailure()
	}

	@Then('^the item does not have a (.+?) link$')
	static void verifyLinkNotExists(def link) {
		assertLinkDoesNotExist(client, link)
	}

	@Then('^the cost fields has amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyCostFieldsHaveAmount(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def listPriceElement = client.body.cost
		CommonAssertion.assertCost(listPriceElement, expectedAmount, expectedCurrency, expectedDisplayName)
		client.stopIfFailure()
	}
}
