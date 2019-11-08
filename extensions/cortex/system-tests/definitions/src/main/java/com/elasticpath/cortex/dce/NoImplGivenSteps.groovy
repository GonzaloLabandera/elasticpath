package com.elasticpath.cortex.dce

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then

/**
 * Steps without actual implementations, used to provide business clarity.
 */
class NoImplGivenSteps {

	@Given('^the catalog has item (.+) with condition (.+)$')
	static void givenItemWithCondition(String item, String condition) { }

	@Given('^item name (.+) with sku code (.+) exists in catalog (.+)$')
	static void givenItemNameWithProductCode(String itemName, String productCode, String catalog) { }

	@Given('^item with (?:product code|name) (.+) has the following prices$')
	static void givenItemWithPrice(String itemName, DataTable priceTable) { }

	@Given('^item (?:product code|name) (.+) with sku code (.+) has no sku price$')
	static void givenItemSKUWithoutPrice(String itemName, String sku) { }

	@Given('^item (?:product code|name) (.+) has the following (?:skus|components)')
	static void givenItemWithMultiSku(String itemName, DataTable skuTable) { }

	@Given('^the following data policies are assigned to data policy segment (.+)$')
	static void assignDataPoliciesStatement(String value, DataTable dataPolicyListTable) { }

	@Given('^the following data policies with segment (.+) are in (?:Disabled|Draft) state$')
	static void verifyDataPoliciesDraftState(String segment, DataTable dataPolicyListTable) { }

	@And('^one of the supported countries is (.+)$')
	static void verifyCountryIsSupported(def supportedCountry) { }

	@Given('^one of the supported regions for (.+) is (.+)$')
	static void verifyCountrySupportsRegion(def country, def region) { }

	@Given('^(?:item|category) (?:.+) has (?:.+) (?:of|with) (?:.+) in language (?:.+)$')
	static void viewItem() {}

	@Given('^an item with code (?:.+) has an attribute with name (?:.+) with no value defined$')
	static void viewItemAttributeByName() {}

	@Given('^(?:item|category) (?:.+) has no attributes$')
	static void verifyItemHasNoAttributes() {}

	@Then('^a bundle item with code (?:.+) that has (?:.+) constituents$')
	static void verifyBundleItemHasConstituents() {}

	@Then('^a (?:nested bundle|bundle) with code (?:.+) that has a bundle constituent with name (?:.+)$')
	static void verifyNestedBundleHasConstituents() {}

	@Then('^nested bundle (?:.+) has a component (?:.+) that is not sold separately$')
	static void verifyBundleHasComponent() {}

	@Then('^a product with items having codes (?:.+) and (?:.+) distinguished by option (?:.+)$')
	static void distinguishTwoItemsByOption() {}

	@Then('^item with code (?:.+) has (?:.+) value (?:.+)$')
	static void verifyItemValue() {}

	@Given('^(?:.+) is missing a value for (?:.+)')
	static void isMissingValue() { }

	@Given('^category (?:.+) has a subcategory and no parent category$')
	static void verifyCategoryHasSubNoParent() { }

	@Given('^the (?:catalog|category) (?:.+) has (?:.+) (?:categories|subcategories|subcategory)$')
	static void verifyCategoryHas() { }

	@Given('^the category (?:.+) is a top level category with no subcategories')
	static void verifyCategoryIsTopLevel() { }

	@Given('^the category (?:.+) contains (?:.+) (?:item|items)')
	static void verifyCategoryContains() { }

	@Given('^the category (?:.+) contains (?:item|items) (?:.+)$')
	static void verifyCategoryContainsItem() { }

	@Given('^the item (?:.+) belongs to a subcategory of (?:.+)')
	static void verifyItemBelongsToCategory() { }

	@Given('^featured items are configured for the category (?:.+)')
	static void verifyFeaturedItemsForCategory() { }

	@Given('^that (?:.+) does not belong to the current scope')
	static void verifyNotBelongToScope() { }

	@Then('^the corresponding promotion of the coupon is inactive$')
	static void verifyPromotionInactive() { }

	@Given('^an item with cost of (.+) and shipping cost is (.+) percent of order total$')
	static void verifyShippingCostPercentOfItemPrice(cost, percentage) { }

	@Given('^a shipping option (.+) has cost of (.+)$')
	static void shippingOptionCost(shippingOption, amount) { }

	@Given('^a shipping promotion (.+) for the shipping option (.+)$')
	static void verifyShippingOptionHasPromotion(promotion, shippingOption) { }

	@Given('^sku (.+) has weight of (.+)$')
	static void itemWeight(String sku, String weight) { }

	@Given('^shipping option (.+) has (.+) for (.+)$')
	static void shippingOptionInfo(String option, String value, String type) { }

	@Then('^the cart total does not include discounts$')
	static void verifyCartTotalExcludesDiscount() { }

	@Then('^the cost is the sum of each lineitem$')
	static void verifyTotalCost() { }

	@Then('^there is no lineitem in cart$')
	static void verifyCartIsEmpty() { }

	@Given('^offer search for keyword (.+) yields the following (.+) facet choices$')
	static void verifyFacetChoices(keyword, facetName, facetChoices) {}
}
