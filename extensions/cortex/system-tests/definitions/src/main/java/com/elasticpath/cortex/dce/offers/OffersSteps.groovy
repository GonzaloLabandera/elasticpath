package com.elasticpath.cortex.dce.offers

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import cucumber.api.java.en.When


import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Offer

class OffersSteps {

	@When('^I search and open the offer for offer name (.+?)$')
	static void searchAndOpenOfferByName(String searchItemName) {
		CommonMethods.searchForOfferAndOpenOfferWithKeyword(searchItemName)
	}

	@When('^I search and open the offer for sku code (.+?)$')
	static void searchAndOpenOfferBySkuCode(String skuCode) {
		CommonMethods.searchForOfferAndOpenOfferWithSkuCode(skuCode)
	}

	@When('^I go to offer code$')
	static void goToOfferCode() {
		Offer.code()
	}

	@When('^I go to offer definition')
	static void goToOfferDefinition() {
		Offer.definition()
	}

	@When('^I go to offer availability')
	static void goToOfferAvailability() {
		Offer.availability()
	}

	@When('^I search for offer (.+?)$')
	static void searchForOfferByKeyword(String keyword) {
		CommonMethods.searchForOffer(keyword, "5")
		client.follow()
				.stopIfFailure()
	}

	@When('^I go to offer (?:component|item) with name (.+?)$')
	static void goToOfferComponentOrItem(String offerComponentName) {
		CommonMethods.findOfferByDisplayName(offerComponentName)
	}

	@When('^I search for the offer (.*) with page-size (.+)$')
	static void searchByForOfferKeyWordAndPageSize(String keyword, String pageSize) {
		CommonMethods.searchForOffer(keyword, pageSize)
		client.follow()
				.stopIfFailure()
	}

	@When('^I POST to the offer search form the keyword (.+?) with page-size (.+)$')
	static void searchForOfferByKeyWordAndPageSizeNoFollow(String keyword, String pageSize) {
		CommonMethods.searchForOffer(keyword, pageSize)
	}

	@When('^I POST to the offer search form with a (.+) char keyword$')
	static void searchForOfferWithChars(int numberOfChars) {
		String keyword = ""
		for (def i = 1; i < numberOfChars; i++) {
			keyword = keyword + "a"
		}
		CommonMethods.searchForOffer(keyword, "5")
	}

	@Then('^the offer price range has the following result$')
	static void verifyOfferPriceRange(DataTable priceTable) {
		Map<String, String> priceTableMap = priceTable.asMap(String, String)
		Offer.verifyOfferPriceRange("list-price-range", "from-price", priceTableMap.get("LIST_PRICE_RANGE_FROM"))
		Offer.verifyOfferPriceRange("list-price-range", "to-price", priceTableMap.get("LIST_PRICE_RANGE_TO"))

		Offer.verifyOfferPriceRange("purchase-price-range", "from-price", priceTableMap.get("PURCHASE_PRICE_RANGE_FROM"))
		Offer.verifyOfferPriceRange("purchase-price-range", "to-price", priceTableMap.get("PURCHASE_PRICE_RANGE_TO"))

	}
}
