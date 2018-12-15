package com.elasticpath.cortex.dce

import cucumber.api.DataTable
import cucumber.api.java.en.Given

class CommonGivenSteps {

	@Given('^the catalog has item (.+) with condition (.+)$')
	static void givenItemWithCondition(String item, String condition) {
		//		Non implementation step.
	}

	@Given('^item name (.+) with sku code (.+) exists in catalog (.+)$')
	static void givenItemNameWithProductCode(String itemName, String productCode, String catalog) {
		//		Non implementation step.
	}

	@Given('^item with (?:product code|name) (.+) has the following prices$')
	static void givenItemWithPrice(String itemName, DataTable priceTable) {
		//		Non implementation step.
	}

	@Given('^item (?:product code|name) (.+) with sku code (.+) has no sku price$')
	static void givenItemSKUWithoutPrice(String itemName, String sku) {
		//		Non implementation step.
	}

	@Given('^item (?:product code|name) (.+) has the following (?:skus|components)')
	static void givenItemWithMultiSku(String itemName, DataTable skuTable) {
		//		Non implementation step.
	}
}
