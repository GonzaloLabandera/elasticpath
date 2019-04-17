package com.elasticpath.cortex.dce.carts

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_CART_URL
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.cortex.dce.SharedConstants.ELEMENT_LINK
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortex.dce.DBConnector
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.LineItem

class CartSteps {

	def static CART_URI
	def static NEW_CART_URI

	@When('^I go to add to cart form$')
	static void getAddToCartForm() {
		Item.addtocartform()
	}
	@When('^I go to add to bulk add cart form$')
	static void getBulkAddToCartForm() {
		Cart.getCart()
		client.additemstocartform()
				.stopIfFailure()
	}

	@When('^I add to cart with quantity of (.+)$')
	static void addToCartWithQuantity(String quantity) {
		Item.addItemToCart(quantity)
	}

	@Given('^item sku (.+) does not have a price$')
	static void verifyItemHasNoPrice(String skuCode) {
		FindItemBy.skuCode(skuCode)
		assertLinkDoesNotExist(client, "price")
	}

	@When('^I (?:add|have) item (.+) (?:to|in) the cart with quantity (.+)$')
	static void addProductToCartWithQuantity(String productName, int quantity) {
		CommonMethods.searchAndAddProductToCart(productName, quantity)
	}

	@When('^I update (.+) Details: Message:(.+), RecipientEmail:(.+), RecipientName:(.+), SenderName:(.+) and Quantity:(.+)$')
	static void updataDetailsMessage(String itemDisplayName, msg, recEmail, recName, senderName, itemQty) {
		def lineitemUri = CommonMethods.findCartLineItemUriByDisplayName(itemDisplayName)
		client.PUT(lineitemUri, [
				quantity     : itemQty,
				configuration: ["giftCertificate.message"       : msg,
								"giftCertificate.recipientEmail": recEmail,
								"giftCertificate.recipientName" : recName,
								"giftCertificate.senderName"    : senderName]
		])
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I update the item (.+) in the cart with quantity (.+) and configurable fields:$')
	static void updateItemQuantityAndFields(String skuCode, String quantity, DataTable modifierFieldsTable) {
		def lineItemUri = CommonMethods.findCartLineItemUriBySkuCode(skuCode)
		Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		client.PUT(lineItemUri, [
				quantity     : quantity,
				configuration: configurationFields
		])
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I change the lineitem quantity for (.+) to (.+)$')
	static void updateQuantityForItemByName(String itemDisplayName, String newQuantity) {
		def lineitemUri = CommonMethods.findCartLineItemUriByDisplayName(itemDisplayName)
		client.PUT(lineitemUri, [
				quantity: newQuantity
		])
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I change the lineitem quantity of item code (.+) to (.+)$')
	static void updateQuantityForItemByCode(String itemSkuCode, String newQuantity) {
		def lineitemUri = CommonMethods.findCartLineItemUriBySkuCode(itemSkuCode)

		client.PUT(lineitemUri, [
				quantity: newQuantity
		])
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I delete item (.+) from my cart$')
	static void deleteItemFromCartByName(String itemDisplayName) {
		def lineitemUri = CommonMethods.findCartLineItemUriByDisplayName(itemDisplayName)
		client.DELETE(lineitemUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I delete the lineitem with code (.+) from my cart$')
	static void deleteItemFromCartByCode(String itemSkuCode) {
		def lineitemUri = CommonMethods.findCartLineItemUriBySkuCode(itemSkuCode)
		client.DELETE(lineitemUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@When('^I view (.+) in the catalog$')
	static void viewItemInCatalog(String skuCode) {
		FindItemBy.skuCode(skuCode)
	}

	@When('^I attempt to add (.+) with invalid quantity (.+)$')
	static void addToCartWithInvalidQuantity(String displayName, String InvalidQuantity) {
		CommonMethods.searchAndOpenItemWithKeyword(displayName)
		Item.addtocartform()

		def actionLink = client.body.links[0].href
		client.POST(actionLink, [
				quantity: InvalidQuantity
		])
	}

	@When('^I attempt to change the lineitem quantity for (.+) to (.+)$')
	static void setLineitemQuantity(String itemDisplayName, String newQuantity) {
		def lineitemUri = CommonMethods.findCartLineItemUriByDisplayName(itemDisplayName)
		client.PUT(lineitemUri, [
				quantity: newQuantity
		])
	}

	@Then('^I save the cart URI and login in as another shopper$')
	static void saveCurrentURIAndLoginWithAnotherShopper() {
		Cart.getCart()
		CART_URI = client.body.self.uri

		client.authRegisteredUserByName(DEFAULT_SCOPE, "harry.potter@elasticpath.com")
	}

	@Then('^I attempt to clear the first shopper\'s cart$')
	static void attemptClearSavedShopperCart() {
		client.DELETE(CART_URI + "/lineitems")
	}

	@Then('^the items in the cart are ordered as follows$')
	static void verifyLineitemsSortOrderInCart(DataTable cartItemsTable) {
		def cartItems = cartItemsTable.asList(String)

		Cart.lineitems()
		List<String> items = new ArrayList<>()
		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.uri)
				LineItem.item()
				items.add(Item.getItemName())
			}
		}

		assertThat(items)
				.as("Cart items order or size is not as expected")
				.containsExactlyElementsOf(cartItems)
	}

	@Then('^the items in the zoomed cart are ordered as follows$')
	static void verifyZoomedLineitemsSortOrderInCart(DataTable cartItemsTable) {
		def cartItems = cartItemsTable.asList(String)

		client.GET(DEFAULT_CART_URL + CartConstants.ZOOM_LINE_ITEM_DEFINITION)

		def bodyElements = client.body._lineitems._element[0]

		List<String> items = new ArrayList<>()
		for (def element : bodyElements) {
			String itemName = element
					._item
					._definition[0]
					."display-name"[0]

			items.add(itemName)
		}

		assertThat(items)
				.as("Zoomed cart items order or size is not as expected")
				.containsExactlyElementsOf(cartItems)
	}

	@Then('^capture the uri of the registered shopper\'s cart$')
	static void getRegisteredShopperURI() {
		Cart.getCart()
		CART_URI = client.body.self.uri
	}

	@Then('^I attempt to view another shopper\'s cart$')
	static void viewShopperCart() {
		client.GET(CART_URI)
				.stopIfFailure()
	}

	@Then('^attempt to add to another shopper\'s cart$')
	static void addToAnotherShopperCart() {
		CommonMethods.searchAndOpenItemWithKeyword("firstProductAddedToCart")

		def itemURI = client.body.self.uri
		client.POST(CART_URI + "/lineitems" + itemURI, [
				quantity: 2
		])
	}

	@Then('^I am not able to view the cart$')
	static void attemptToViewCart() {
		assertThat(client.response.status)
				.as("Access to the specified resource is forbidden.")
				.isEqualTo(403)
		client.follow()
	}

	@Then('^the cart total-quantity (?:remains|is) (.+)$')
	static void verifyCartTotalQuantity(int cartTotalQty) {
		Cart.getCart()
		assertThat(client.body.'total-quantity')
				.as("Cart total quantity is not as expected")
				.isEqualTo(cartTotalQty)
	}

	@And('^the cart lineitem for item code (.+) has quantity of (.+)$')
	static void verifyLinetemTotalQuantity(String skuCode, int quantity) {
		Cart.getCart()
		Cart.findCartElementBySkuCode(skuCode)
		assertThat(client.body.'quantity')
				.as("Cart line item quantity does not match.")
				.isEqualTo(quantity)
	}

	@Then('^(?:nothing has been added to the cart|the list of cart lineitems is empty)$')
	static void verifyCartIsEmpty() {
		Cart.lineitems()
		assertLinkDoesNotExist(client, ELEMENT_LINK)
	}

	@Then('^I am not able to add the item to my cart$')
	static void attemptAddItemToCart() {
		Item.addtocartform()
		// Check that there is no addtodefaultcart link
		assertLinkDoesNotExist(client, "addtodefaultcartaction")
	}

	@Then('^I am prevented from adding the item to the cart$')
	static void verifyItemCannotBeAddedToCart() {
		Item.addtocartform()
		assertLinkDoesNotExist(client, "addtodefaultcartaction")
		def lineitemUri = client.body.self.uri
		client.POST(lineitemUri, [
				quantity: 1
		])
				.stopIfFailure()

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(409)
	}

	@Then('^I am allowed to add to cart$')
	static void addItemToCart() {
		Item.addItemToCart(1)
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(200)
	}

	@When('^I add selected multisku item to the cart$')
	static void addMultiSkuItemToCart() {
		def itemUri = client.body.self.uri
		client.GET(itemUri)
				.item()
		client.addtocartform()
				.addtodefaultcartaction(quantity: 1)
				.follow()
				.stopIfFailure()
	}

	@When('^I go to my cart$')
	static void getDefaultCart() {
		Cart.getCart()
	}

	@When('^I add the following SKU codes and their quantities to the cart$')
	static void addItemsToCart(DataTable itemsTable) {
		List<Map<String, String>> items = itemsTable.asMaps(String.class, String.class)
		Cart.addItemsToCart(items)
	}

	@Then('^the number of cart lineitems is (.+)$')
	static void verifyLineitemTotalQuantity(int numberOfLineitems) {
		Cart.lineitems()
		List lineItemElementList = new ArrayList()
		client.body.links.findAll {
			if (it.rel == "element") {
				lineItemElementList.add(it.href)
			}
		}
		assertThat(numberOfLineitems)
				.as("Expected number of cart lineitems do not match.")
				.isEqualTo(lineItemElementList.size())
	}

	@Then('^item (.+) is not in any of my carts$')
	static void checkItemIsNOtInCart(String itemDisplayName) {
		Cart.getCart()
		assertThat(CommonMethods.isLineitemsContainElementWithDisplayName(itemDisplayName))
				.as(itemDisplayName + " should not be present")
				.isFalse()
	}

	@Then('^new cart is created$')
	static void verifyCartExists() {
		Cart.getCart()
		NEW_CART_URI = client.body.self.uri
		assertThat(NEW_CART_URI)
				.as("New cart uri is not as expected")
				.isNotEqualTo(CART_URI)
	}

	@Then('^I try to delete dependent lineitem from the cart$')
	static void deleteDependentLineitem() {
		client.GET("/")
				.defaultcart()
				.lineitems()
				.element()
				.dependentlineitems()
				.element()

		def dependentItemUri = client.body.self.uri
		client.DELETE(dependentItemUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(409)
	}

	@Then('^that (?:.+) is the url of (?:.+)$')
	static void doNothing() {}

	@Before('@setFixedInventorySku')
	static void beforeSetFixedInventorySku() {
		setInventory("physical_product_with_fixed_inventory_sku", "MOBEE Warehouse", 10)
	}

	@After('@resetFixedInventorySku')
	static void afterResetInventorySku() {
		setInventory("physical_product_with_fixed_inventory_sku", "MOBEE Warehouse", 10)
	}

	@Before('@setLimitedInventorySku')
	static void beforeSetLimitedInventorySku() {
		setInventory("physicalProductWithLimitedInventory_sku", "MOBEE Warehouse", 1)
	}

	@After('@resetLimitedInventorySku')
	static void afterResetLimitedInventorySku() {
		setInventory("physicalProductWithLimitedInventory_sku", "MOBEE Warehouse", 1)
	}

	private static void setInventory(final String skuCode, final String warehouseName, int expectedQuantity) {
		DBConnector dbConnector = new DBConnector()
		dbConnector.setAvailableQuantity(skuCode, warehouseName, expectedQuantity)
	}
}
