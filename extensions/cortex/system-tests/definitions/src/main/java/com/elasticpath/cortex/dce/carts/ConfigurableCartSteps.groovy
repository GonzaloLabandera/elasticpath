package com.elasticpath.cortex.dce.carts

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.CommonAssertion.assertItemConfiguration
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.WishList

class ConfigurableCartSteps {

	@Then('^I (?:have|add) the item (.+) (?:in|to) the cart with quantity (.+) and configurable fields:$')
	static void verifyConfigurableItemInCartWithQuantity(String itemCode, String itemQty, DataTable modifierFieldsTable) {
		FindItemBy.skuCode(itemCode)
		Item.addItemToCartWithoutFollow(itemQty, modifierFieldsTable.asMap(String, String))
	}

	@Then('^I add the item to the cart with quantity (.+) and configurable fields:$')
	static void addConfigurableItemToCartWithQuantity(String itemQty, DataTable modifierFieldsTable) {
		Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		Item.addItemToCartWithoutFollow(itemQty, configurationFields)
	}

	@Then('^I successfully add the item to the cart with quantity (.+) and configurable fields:$')
	static void verifyConfigurableItemIsAddedToCartWithQuantity(String itemQty, DataTable modifierFieldsTable) {
		Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		Item.addItemToCart(itemQty, configurationFields)
	}

	@Then('^the cart lineitem with itemcode (.+) has quantity (.+) and configurable fields as:$')
	static void verifyConfigurableLineitem(String itemSkuCode, int qty, DataTable itemDetailsTable) {
		Cart.lineitems()
		CommonMethods.findCartElementBySkuCodeAndConfigurableFieldValues(itemSkuCode, itemDetailsTable)
		assertThat(client.body.'quantity')
				.as("Line item quantity does not match for itemcode - " + itemSkuCode)
				.isEqualTo(qty)
	}

	@When('^I change the lineitem quantity of configurable item code (.+) with given configuration to (.+)$')
	static void updateConfigurableLineitemQuantity(String itemSkuCode, String newQuantity, DataTable itemDetailsTable) {
		def lineitemUri = CommonMethods.findCartLineItemUriBySkuCodeAndConfigurableFieldValues(itemSkuCode, itemDetailsTable)
		client.PUT(lineitemUri, [
				quantity: newQuantity
		])

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I delete the configurable lineitem with code (.+) and with given configuration from my cart$')
	static void deleteConfigurableLineitem(String itemSkuCode, DataTable itemDetailsTable) {
		def lineitemUri = CommonMethods.findCartLineItemUriBySkuCodeAndConfigurableFieldValues(itemSkuCode, itemDetailsTable)
		client.DELETE(lineitemUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@Then('^I should see wishlist line item configurable fields for itemcode (.+) as:$')
	static void verifyConfigurableItemInWishlist(String itemSkuCode, DataTable itemDetailsTable) {
		client.GET("/")
				.defaultwishlist()
				.lineitems()
				.stopIfFailure()

		WishList.findWishListElementBySkuCode(itemSkuCode)

		assertItemConfiguration(itemDetailsTable)
	}

	@Then('^I should see wishlist line item (.+) with configurable field values as:$')
	static void findConfigurableItemInWishlist(String itemSkuCode, DataTable itemDetailsTable) {
		client.GET("/")
				.defaultwishlist()
				.lineitems()
				.stopIfFailure()
		CommonMethods.findCartElementBySkuCodeAndConfigurableFieldValues(itemSkuCode, itemDetailsTable)
	}

	@Then('^I should see in the response the line item just added with configurable fields as:$')
	static void verifyConfigurableLineitem(DataTable itemDetailsTable) {
		assertItemConfiguration(itemDetailsTable)
	}
}