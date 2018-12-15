package com.elasticpath.cortex.dce.lookups

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.cortex.dce.SharedConstants.DISPLAY_NAME_FIELD
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item

class LookupsSteps {

	@When('^I follow a link back to the item$')
	static void clickItemLink() {
		client.item()
				.stopIfFailure()
	}

	@When('^I look up an item with code (.+?)$')
	static void lookupItemByCode(String skuCode) {
		FindItemBy.skuCode(skuCode)
	}

	@When('^I look up an (?:invalid|out of scope) item (.+?)$')
	static void lookupItemByInvalidCode(String skuCode) {
		FindItemBy.lookupWithoutFollow(skuCode)
	}

	@When('^I (?:add item|have item) with code (.+?) (?:to my|in my) cart$')
	static void lookupAddToCart(String itemCode) {
		CommonMethods.lookupAndAddToCart(itemCode, 1)
	}

	@When('^I (?:add item|have item) with code (.+?) (?:to my|in my) cart without the required configurable fields$')
	static void addNotConfiguredItemToCart(String itemCode) {
		FindItemBy.skuCode(itemCode)
		Item.addItemToCartWithoutFollow(1)
	}

	@When('^I (?:add|have) item with code (.+?) (?:to my|in my) cart with quantity (\\d+)$')
	static void lookupAddToCartWithQuantity(String itemCode, int qty) {
		CommonMethods.lookupAndAddToCart(itemCode, qty)
	}

	@When('^I add following items to the cart$')
	static void addMultipleItemsToCart(DataTable skuList) {
		for (def sku : skuList.asList(String)) {
			CommonMethods.lookupAndAddToCart(sku, 1)
		}
	}

	@When('^I add item with code (.+?) to my cart with quantity (\\d+) and do not follow location$')
	static void lookupAddToCartWithQuantityNoFollow(String itemCode, int qty) {
		CommonMethods.lookupAndAddToCartNoFollow(itemCode, qty)
	}

	@Then('^I (?:have a|add an item with code|have an item with code) (.+) (?:in|to) the cart with quantity (.+) and configurable fields:$')
	static void lookupAddToCartConfiguredItemWithQuantity(String itemCode, String itemQty, DataTable modifierFieldsTable) {
		Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		FindItemBy.skuCode(itemCode)
		Item.addItemToCart(itemQty, configurationFields)
	}

	@Given('^a registered shopper (.+) with the following configured item in their cart$')
	static void loginAddConfiguredItemToCart(String email, DataTable modifierFieldsTable) {
		Map<String, String> configurationFields = new HashMap<>(modifierFieldsTable.asMap(String, String))
		def itemcode = configurationFields.get("itemcode")
		configurationFields.remove("itemcode")
		def itemqty = configurationFields.get("itemqty")
		configurationFields.remove("itemqty")

		client.authRegisteredUserByName(DEFAULT_SCOPE, email)
				.stopIfFailure()

		Cart.clearCart()
		FindItemBy.skuCode(itemcode)
		Item.addItemToCart(itemqty, configurationFields)
	}

	@When('^I change the multi sku selection by (.+) and select choice (.+)$')
	static void changeSelectedSkuOption(String itemOption, String itemChoice) {
		Item.selectSkuOption(itemOption, itemChoice)
	}

	@Then('^the item code is (.+)$')
	static void verifyItemCode(String itemSkuCode) {
		Item.code()
		assertThat(client["code"])
				.as("Item code is not as expected")
				.isEqualTo(itemSkuCode)
	}

	@Then('^I should see item name is (.+)$')
	static void verifyItemName(String itemName) {
		client.definition()
				.stopIfFailure()
		assertThat(client[DISPLAY_NAME_FIELD])
				.as("Item name is not as expected")
				.isEqualTo(itemName)
	}

	@Then('^I should see item details shows: display name is (.+) and display value is (.+)$')
	static void verifyItemShouldHaveValues(String itemDisplayName, String itemDisplayValue) {
		assertThat(client.body.details.'display-name')
				.as("Display name is not as expected")
				.isEqualTo([itemDisplayName])
		assertThat(client.body.details.'display-value')
				.as("Display value is not as expected")
				.isEqualTo([itemDisplayValue])
	}

	@When('^I retrieve the lookups link point$')
	static void clickLookupsLink() {
		client.GET("/")
				.lookups()
				.stopIfFailure()
	}

	@Given('^I retrieve the item lookup form$')
	static void clickLookupFormLink() {
		client.GET("/")
				.lookups()
				.itemlookupform()
				.stopIfFailure()
	}

	@Given('^I retrieve the navigation lookup form$')
	static void clickNavigationLookupFormLink() {
		client.GET("/")
				.lookups()
				.navigationlookupform()
				.stopIfFailure()
	}

	@Given('^I retrieve the batch items lookup form$')
	static void clickBatchLookupForm() {
		client.GET("/")
				.lookups()
				.batchitemslookupform()
				.stopIfFailure()
	}

	@When('^I retrieve the purchase lookup form$')
	static void clickPurchaseLookupForm() {
		client.GET("/")
				.lookups()
				.purchaselookupform()
				.stopIfFailure()
	}

	@When('^I submit a batch of sku codes (.*)$')
	static void lookupSkuInBatch(final String skuCodes) {
		FindItemBy.batch(skuCodes)
	}

	@Then('^a batch of (.*) items is returned$')
	static void verifyNumberOfReturnedItems(final int numberOfItems) {
		def items = client.body.links.findAll {
			link ->
				link.rel == "element"
		}
		assertThat(items).size()
				.as("Number of elements is not as expected")
				.isEqualTo(numberOfItems)
	}

	@Then('^the batch lookup returns the correct (.+)$')
	static void verifyLookupReturnedCorrectItems(String skuCodes) {
		List<String> sku_codes = Eval.me(skuCodes)
		def elementResponse = client.save()

		assertThat(client.body.links.size())
				.as("Number of elements is not as expected")
				.isEqualTo(sku_codes.size())

		for (String skucode : sku_codes) {
			boolean itemExists = false
			client.resume(elementResponse)

			client.body.links.find {
				if (it.rel == "element") {
					client.GET(it.href)
					client.code()
					if (client["code"] == skucode) {
						itemExists = true
					}
				}
			}
			assertThat(itemExists)
					.as("Item not found for sku code: " + skucode)
					.isTrue()
		}
	}

	@When('^I submit the invalid item uri (.+)$')
	static void getItemWithInvalidURI(String uri) {
		client.GET(uri)
				.stopIfFailure()
	}

	@When('^I cannot add to cart line item with code (.+?) with quantity (\\d+)$')
	static void verifyItemCannotBeAddedeToCart(String itemCode, int qty) {
		CommonMethods.lookup(itemCode)
		Item.addItemToCartWithoutFollow(qty)
		assertThat(client.response.status)
				.as("The response status is not as expected")
				.isEqualTo(400)
	}
}
