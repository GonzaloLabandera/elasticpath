package com.elasticpath.cortex.dce

import cucumber.api.java.en.When

class ExampleSteps {


	@When("^I search for an item name (.+)\$")
	void searchItemByName(String itemName) {
		CommonMethods.searchAndOpenItemWithKeyword(itemName)
	}

}
