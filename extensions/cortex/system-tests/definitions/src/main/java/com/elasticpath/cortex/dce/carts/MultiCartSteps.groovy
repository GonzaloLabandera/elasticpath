package com.elasticpath.cortex.dce.carts

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.CommonMethods.getLineItemUriForItemCode
import static com.elasticpath.cortex.dce.CommonMethods.verifyLineitemsNotContainElementWithCode
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import com.jayway.jsonpath.JsonPath
import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortex.dce.zoom.ZoomSteps
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.MultiCart
import com.elasticpath.cortexTestObjects.Payment

class MultiCartSteps {

	@When('^I create a new shopping cart with name (.+)$')
	static void createCart(String cartName) {
		MultiCart.createCart(cartName)
	}

	@When('^I create a new shopping cart that has a name of length (.+)$')
	static void createCartWitNameOfLength(int length) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			stringBuilder.append("a");

		}
		MultiCart.createCart(stringBuilder.toString())
	}

	@When('^I update my cart with name (.+) to (.+)$')
	static void updateName(String originalName, String newName) {
		MultiCart.getCart(originalName);
		MultiCart.updateName(newName)
	}

	@Then('^the cart has name (.+)$')
	static void verifyCartExists(String cartName) {
		MultiCart.getCart(cartName)
		client.descriptor()
				.stopIfFailure()
	}
	
	@Then('^the default cart has name (.+)$')
	static void verifyDefaultCartName(String cartName) {
		client.GET("/")
			.defaultcart()
			.descriptor()
			.stopIfFailure()
		assertThat(client.body.name).isEqualTo(cartName)
	}
	
	@When('^I update my default cart name to (.+)$')
	static void updateDefaultCartName(String newName) {
		client.GET("/")
				.defaultcart()
				.stopIfFailure()
		MultiCart.updateName(newName)
		assertThat(client.response.status)
				.as("The response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I go to my carts$')
	static void getCarts() {
		MultiCart.getCarts()
	}

	@When('^I go to cart (.+)$')
	static void getSpecificCart(String cartName) {
		MultiCart.getCart(cartName)
	}

	@When('^I go to create cart form$')
	static void getAddToCartForm() {
		MultiCart.getCreateCartForm();
	}

	@Then('^I have carts with the following names')
	static void checkCartNames(DataTable table) {
		MultiCart.getCarts()
		ZoomSteps.addZoomToCurrentURL("element:descriptor")

		table.raw().each {
			def name = it.get(0)
			def valueField = "_element[*]._descriptor[?(@['name']==$name)].name"
			assertThat(JsonPath.read(client.getBody(), valueField))
					.as("The field $valueField doesn't contain $name")
					.contains(name)
		}
	}

	@Then('^I do not have carts with the following names')
	static void missingCartNames(DataTable table) {
		MultiCart.getCarts()
		ZoomSteps.addZoomToCurrentURL("element:descriptor")

		def cartPresent = false
		table.raw().each {
			def name = it.get(0)
			def valueField = "_element[*]._descriptor[?(@['name']==$name)].name"

			cartPresent |= JsonPath.read(client.getBody(), valueField).contains(name)

			assertThat(cartPresent)
					.as("The cart with $name exists")
		}
	}

	@And("I go to the (.+) cart form")
	static void navigateToSpecificAddToCartForm(String cartName) {
		Item.getAddToSpecificCartForm(cartName)
		client.resume(Item.addToSpecificCartFormResponse)
	}

	@When('I add (.+) to cart (.+) with quantity (.+)$')
	static void navigateToSpecificAddToCartForm(String skuCode, String cartName, String quantity) {
		FindItemBy.skuCode(skuCode)
		Item.addItemToSpecificCart(cartName, quantity);
	}

	@When('I add configurable item (.+) to specific cart (.+) with quantity (.+) and configurable fields:$')
	static void navigateToSpecificAddToCartForm(String skuCode, String cartName, String quantity, DataTable modifierFieldsTable) {
		FindItemBy.skuCode(skuCode)
		Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		Item.addItemToSpecificCart(cartName, quantity, configurationFields);
	}

	@Then('^in the cart (.+) the cart lineitem with itemcode (.+) has quantity (.+) and configurable fields as:$')
	static void verifyConfigurableLineitem(String cartName, String itemSkuCode, int quantity, DataTable itemDetailsTable) {
		MultiCart.getCart(cartName)
		client.lineitems()
		CommonMethods.findCartElementBySkuCodeAndConfigurableFieldValues(itemSkuCode, itemDetailsTable)
		assertThat(client.body.'quantity')
				.as("Line item quantity does not match for itemcode - " + itemSkuCode)
				.isEqualTo(quantity)
	}

	@Then('^cart (.+) total-quantity (?:remains|is) (.+)$')
	static void verifySpecificCartTotalQuantity(String cartName, int cartTotalQty) {
		MultiCart.getCart(cartName)
		assertThat(client.body.'total-quantity')
				.as("Cart total quantity is not as expected")
				.isEqualTo(cartTotalQty)
	}

	@Then('^I am not able to add the item to cart (.+)$')
	static void attemptAddItemToSpecificCart(String cartName) {
		Item.getAddToSpecificCartForm(cartName)
		assertLinkDoesNotExist(client, "addtocartaction")
	}

	@When('^I attempt to change cart (.+) lineitem quantity of (.+) to (.+)$')
	static void setLineitemQuantity(String cartName, String skuCode, String newQuantity) {
		def lineitemUri = MultiCart.findCartLineItemUriBySkuCode(cartName, skuCode)
		client.PUT(lineitemUri, [
				quantity: newQuantity
		])
	}

	@When('^I delete lineitem code (.+) from specific cart (.+)$')
	static void deleteLineitemQuantity(String skuCode, String cartName) {
		def lineitemUri = MultiCart.findCartLineItemUriBySkuCode(cartName, skuCode)
		client.DELETE(lineitemUri)
	}

	@When('^I delete a shopping cart with name (.+)$')
	static void deleteCart(String cartName) {
		MultiCart.deleteCart(cartName)
	}

	@When('^I clear specific cart (.+)$')
	static void clearCart(String cartName) {
		MultiCart.clearCart(cartName)
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@Then('^specific cart (.+) has lineitem code (.+) with quantity of (.+)$')
	static void verifyLineitemQuantity(String cartName, String skuCode, int quantity) {
		MultiCart.findCartLineItemUriBySkuCode(cartName, skuCode)
		assertThat(client.body.'quantity')
				.as("Cart line item quantity does not match.")
				.isEqualTo(quantity)
	}

	@Then('^the cart (.+) total has amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyTotalAmountInCart(String cartName, String amount, String currency, String display) {
		MultiCart.getCart(cartName)
		client.total()
				.stopIfFailure()
		CommonAssertion.assertCost(client.body.cost[0], amount, currency, display)
	}

	@Then('^the cart (.+) discount amount is (.+)$')
	static void verifyMultiCartsDiscount(String cartName, String expectedDiscount) {
		MultiCart.getCart(cartName)
		client.discount()
				.stopIfFailure()
		assertThat(client.body.discount["amount"][0])
				.as("Display amount for cart " + cartName + " is not as expected")
				.isEqualTo(expectedDiscount)
	}

	@Then('^the cart (.+) has (.+) promotion$')
	static void verifyMultiCartPromoNameDisplayed(String cartName, String promoName) {
		MultiCart.getCart(cartName)
		client.appliedpromotions()
				.stopIfFailure();

		def numberOfPromotions = client.body.links.findAll { link ->
			link.rel == "element"
		}.size()

		assertThat(numberOfPromotions)
				.as("Number of promotions is not as expected")
				.isEqualTo(1)

		client.element()
				.stopIfFailure()

		assertThat(client["display-name"])
				.as("Display name is not as expected")
				.isEqualTo(promoName)
	}

	@Then('^there are (.+) carts present$')
	static void verifyNumberOfCarts(expectedNumberOfCarts) {
		MultiCart.getCarts()

		def numberOfCarts = client.body.links.findAll { link ->
			link.rel == "element"
		}.size()

		assertThat(numberOfCarts)
				.as("Number of carts is not as expected")
				.isEqualTo(expectedNumberOfCarts.toInteger())
	}

	@Then('^the cart (.+) does not have promotions$')
	static void verifyMultiCartNoPromotion(String cartName) {
		MultiCart.getCart(cartName)

		client.appliedpromotions()
				.stopIfFailure();

		def numberOfPromotions = client.body.links.findAll { link ->
			link.rel == "element"
		}.size()

		assertThat(numberOfPromotions)
				.as("Number of promotions is not as expected")
				.isEqualTo(0)
	}

	@When('^I move an item with code (.+) from cart (.+) to my default wishlist$')
	static void moveToWishlistByCode(String skuCode, String cartName) {
		MultiCart.getCart(cartName)
		client.lineitems()
		getLineItemUriForItemCode(skuCode)
		Item.moveItemToWishListWithoutFollow()
	}

	@Then('^item with code (.+) is not found in cart (.+)$')
	static void verifyItemNotInCartByCode(String skuCode, String cartName) {
		MultiCart.getCart(cartName)
		client.lineitems()
		verifyLineitemsNotContainElementWithCode(skuCode)
	}

	@Then('^the order for cart (.+) is submitted$')
	static void submitOrder(String cartName) {
		MultiCart.getOrder(cartName)
		MultiCart.submitPurchase()
	}

	@And('^I fill in payment methods needinfo for cart (.+)$')
	static void setPaymentMethod(String cartName) {
		MultiCart.getOrder(cartName)
		Payment.createInstrumentUsingSelectedCartOrder()
	}
}
