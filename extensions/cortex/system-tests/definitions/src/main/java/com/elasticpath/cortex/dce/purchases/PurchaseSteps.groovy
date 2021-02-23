/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortex.dce.purchases

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.CommonAssertion.assertAddress
import static com.elasticpath.cortex.dce.CommonAssertion.assertCost
import static com.elasticpath.cortex.dce.CommonMethods.searchAndOpenItemWithKeyword
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_PAYMENT_CONFIGURATION_NAME
import static com.elasticpath.cortex.dce.SharedConstants.RESERVE_FAILS_PAYMENT_CONFIGURATION_NAME
import static com.elasticpath.cortex.dce.SharedConstants.DISPLAY_NAME_FIELD
import static com.elasticpath.cortex.dce.SharedConstants.NAME_FIELD
import static com.elasticpath.cortex.dce.SharedConstants.TEST_EMAIL_VALUE
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortex.dce.LoginSteps
import com.elasticpath.cortex.dce.SharedConstants
import com.elasticpath.cortex.dce.addresses.AddressQuery
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Payment
import com.elasticpath.cortexTestObjects.Profile
import com.elasticpath.cortexTestObjects.Purchase 

class PurchaseSteps {

	static final ADDRESS_FIELD = 'address'
	static final CARRIER_FIELD = 'carrier'
	static purchaseUri
	static savedPurchaseNumber

	static final Map<String, String> ADDRESS = ["country-name"  : "CA",
												"locality"      : "Vancouver",
												"postal-code"   : "v7v7v7",
												"region"        : "BC",
												"street-address": "1111 EP Road"]
	static createPurchaseWithFollow = { ->
		Order.submitPurchaseAndWaitForRelease()
		purchaseUri = client.save()
		savedPurchaseNumber = client.body.'purchase-number'
	}

	@When('^I (?:can make|make) a purchase and wait for it to be released$')
	static void submitPurchaseAndWaitForRelease() {
		Order.submitPurchaseAndWaitForRelease()
	}

	@When('^I (?:can make|make) a purchase$')
	static void submitPurchase() {
		Order.submitPurchase()
	}

	@And('^I submit the order and retrieve the HTTP status$')
	static void submitPurchaseNoFollow() {
		Order.submitPurchaseWithoutFollow()
	}

	@When('^I create a purchase and view the purchase details$')
	static void submitPurchaseViewDetails() {
		CommonMethods.addEmailPaymentInstrumentAndAddress()
		createPurchaseWithFollow()
	}

	@Given('^I have previously made a purchase with \"(\\d+?)\" (?:physical item|digital item|bundle item|item) \"([^\"]+)\"$')
	static void createAndSubmitNewOrder(int quantity, String productName) {
		CommonMethods.searchAndAddProductToCart(productName, quantity)
		Order.selectAnyShippingOption()
		Order.submitPurchase()
	}

	@Given('^I have previously made a purchase with item code (.+)$')
	static void createAndSubmitNewOrder(def skuCode) {
		CommonMethods.lookupAndAddToCart(skuCode, 1)
		CommonMethods.addEmailPaymentInstrumentAndAddress()
		createPurchaseWithFollow()
	}

	@When('^I view the shipment line items$')
	static void viewPurchaseShipmentLineitemsLink() {
		Purchase.shipmentLineItems()
	}

	@When('^I view the purchase line items$')
	static void viewPurchaseLineitems() {
		client.GET("/")
				.defaultprofile()
				.purchases()
				.element()
				.lineitems()
				.stopIfFailure()
	}

	@When('^I view the components of a purchase line item (.+)$')
	static void viewPurchaseLineItemComponents(String skuCode) {
		def codes = [skuCode]
		Purchase.verifyPurchaseItemsBySkuCode(codes)

		client.components()
				.stopIfFailure()
	}

	@When('^the purchase line items should contain following skus?$')
	static void verifyPurchaseLineItems(List<String> skuCodeList) {
		Purchase.verifyPurchaseItemsBySkuCode(skuCodeList)
	}

	@When('^all (.+) constituents of a purchase line item (.+) are displayed as components$')
	static void verifyPurchaseLineItemComponents(int componentsAmount, String skuCode) {
		CommonMethods.verifyNumberOfElements(componentsAmount)
		verifySkuCodesOfPurchaseLineItemComponents(skuCode)
	}

	static void verifySkuCodesOfPurchaseLineItemComponents(String purchaseLineItemSkuCode) {
		def componentsResponse = client.save()
		def componentsCodes = getComponentsCodes().toSorted()
		def constituentsCodes = CommonMethods.getConstituentsCodes(purchaseLineItemSkuCode).toSorted()
		assertThat(constituentsCodes)
				.as("The components of a purchase line item do not contain the expected items.")
				.containsExactlyInAnyOrderElementsOf(componentsCodes)
		client.resume(componentsResponse)
	}

	@When('^I see the only following product names among shipment line items (.+)$')
	static void verifyShipmentLineItems(List expectedNames) {
		def shipmentLineItemsNames = Purchase.getShipmentLineItemsNames()
		assertThat(shipmentLineItemsNames)
				.as("The shipment line items do not contain the expected items.")
				.containsExactlyInAnyOrderElementsOf(expectedNames)
	}

	@When('^I view the components of a purchase line item\'s (.+) component (.+)$')
	static void viewComponentComponents(String lineitenSkuCode, String componentSkuCode) {
		def componentUri = ""

		viewPurchaseLineItemComponents(lineitenSkuCode)

		client.findElement {
			purchaseLineItem ->
				def uri = purchaseLineItem.body.self.uri
				purchaseLineItem.item().code()
				if (client["code"] == componentSkuCode) {
					componentUri = uri
				}
		}

		assertThat(componentUri)
				.as("Component with provided sku code " + componentSkuCode + " was not found.")
				.isNotEqualTo("")

		client.GET(componentUri)
				.components()
	}

	static List getComponentsCodes() {
		def codes = []

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
						.item()
						.code()
				codes.add(client["code"])
			}
		}

		return codes
	}

	@When('^I view the purchase payment instruments$')
	static void viewPurchasePaymentInsruments() {
		client.GET("/")
				.defaultprofile()
				.purchases()
				.element()
				.paymentinstruments()
				.stopIfFailure()
	}

	@When('^I view a single purchase payment instrument')
	static void viewRandomPurchasePaymentInstrument() {
		client.GET("/")
				.defaultprofile()
				.purchases()
				.element()
				.paymentinstruments()
				.element()
				.stopIfFailure()
	}

	@Then('^I see shipment line items: (.+)$')
	static void verifyShipmentLineitemQty(int numLineItems) {
		assertThat(client.body.links.findAll { link -> link.rel == "element" })
				.size()
				.as("Number of shipment line items is not as expected")
				.isEqualTo(numLineItems)
	}

	@And('^I see a back link to the shipment$')
	static void clickPurchaseShipmentLink() {
		Purchase.shipment()
	}

	@When('^I view the shipment line item for item "(.+?)"$')
	static void viewShipmentLineitemByName(String productName) {
		client.shipments()
				.element()
				.lineitems()
				.findElement { lineitem ->
			lineitem[SharedConstants.NAME_FIELD] == productName
		}
		.stopIfFailure()
	}

	@Then('^the purchase line item configurable fields for item (.+) are:$')
	static void verifyPurchaseLineitemConfigurationFields(String itemName, DataTable itemDetailsTable) {
		Map<String, String> itemDetails = itemDetailsTable.asMap(String, String)
		Purchase.findPurchaseItemByProductName(itemName)

		for (Map.Entry<String, String> itemDetail : itemDetails.entrySet()) {
			assertThat(client.body.'configuration'.(itemDetail.key).toString())
					.as(itemDetail.key + " is not as expected")
					.isEqualTo(itemDetail.value)
		}
	}

	@Then('^I open purchase line item (.+)$')
	static void viewPurchaseLineitemByName(String itemName) {
		Purchase.findPurchaseItemByProductName(itemName)
	}

	@Then('^I see the quantity "(\\d+?)" and name "(.+?)" fields on the shipment line item$')
	static void verifyLineitemNameAndQty(int quantity, String productName) {
		assertThat(client.body.quantity)
				.as("Quantity is not as expected")
				.isEqualTo(quantity)
		assertThat(client.body.name.toString())
				.as("Name is not as expected")
				.isEqualTo(productName)
	}

	@And('^I see a back link to the list of shipment line items$')
	static void verifyBackLinkExists() {
		assertThat(client.body.links.findAll { link -> link.rel == "list" })
				.size()
				.as("Link back to the list of shipment line items was not found")
				.isEqualTo(1)
	}

	@Then('^I can follow the shipment line item price link$')
	static void clickPriceLink() {
		client.price()
				.stopIfFailure()
	}

	@And('^the purchase-price has fields amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyItemPurchasePriceValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def costElement = client.body.'purchase-price'
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@Then('^purchase item monetary total has fields amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyItemTotalHasFields(def expectedAmount, def expectedCurrency, def expectedDisplayName) {
		def costElement = client.body.'monetary-total'
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^the purchase item monetary total has currency (.+) and display (.+)$')
	static void verifyItemTotalValues(String expectedCurrency, String expectedDisplayName) {
		def costElement = client.body.'monetary-total'
		String costAmount = client.body.'monetary-total'[0].amount
		assertCost(costElement, costAmount, expectedCurrency, expectedDisplayName)
	}

	@Then('^purchase item tax total has fields amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyPurchaseItemTaxTotalHasValues(def expectedAmount, def expectedCurrency, def expectedDisplayName) {
		def taxElement = client.body.'tax-total'
		assertCost(taxElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^the purchase item tax total has currency (.+) and display (.+)$')
	static void verifyPurchaseItemHasCurrencyAndDisplay(def expectedCurrency, def expectedDisplayName) {
		def taxElement = client.body.'tax-total'
		String expectedAmount = client.body.'tax-total'.amount
		assertCost(taxElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^I see the cost field has amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyCostFieldHasValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def costElement = client.body.cost
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^I see the total field has amount: (.+?), currency: (.+?) and display: (.+?)$')
	static void verifyTotalFieldHasValues(String expectedAmount, String expectedCurrency, String expectedDisplayName) {
		def costElement = client.body.total
		assertCost(costElement, expectedAmount, expectedCurrency, expectedDisplayName)
	}

	@And('^I see the tax type is (.+) currency is (.+) cost is (.+)$')
	static void verifyTaxTypeAndValues(String taxType, currency, amount) {
		assertThat(client["cost"].find {
			it ->
				it.title == taxType && it.currency == currency
		}.display)
				.as("Display is not as expected")
				.isEqualTo(amount)
	}

	@And('^I can follow the back link to the shipment line item$')
	static void clickBackLinkToShipment() {
		assertThat(client.body.links.findAll { link -> link.rel == "lineitem" })
				.size()
				.as("Link back to the shipment line item was not found")
				.isEqualTo(1)
		client.lineitem()
				.stopIfFailure()
	}

	@When('^I have previously made a purchase with a physical item (.+) in a tax free state code is (.+)$')
	static void createNewPurchaseNoFollow(String productName, String taxFreePostCode) {
		addNewUSAddressForZeroTaxState()
		FindItemBy.productName(productName)
		Item.addItemToCart(1)
		Order.selectShippingAddressByPostalCode(taxFreePostCode)
		Order.selectAnyShippingOption()
		Order.submitPurchase()
	}

	@When('^I view the details of a line item (.+) in a shipment$')
	static void viewLineitemShipmentDetails(String productName) {
		client.findElement { lineitem ->
			lineitem[NAME_FIELD] == productName
		}
		.stopIfFailure()
	}

	@When('^I navigate to shipment$')
	static void clickShipmentsLink() {
		Purchase.shipmentsElement()
	}

	@When('^I navigate to the billing address$')
	static void clickBillingaddressLink() {
		Purchase.billingaddress()
	}

	@Then('^I can see the shipment status (.+)$')
	static void verifyShipmentStatus(String shipmentStatus) {
		assertThat(client.body.status.code.toString())
				.as("The shipment status is not as expected")
				.isEqualTo(shipmentStatus)
	}

	@Then('^I can follow a link back to the list of shipments$')
	static void clickListLink() {
		Purchase.list()
	}

	@Then('^I can follow a link back to the purchase$')
	static void clickPurchaseLink() {
		Purchase.purchase()
	}

	@When('^I follow the shipping address link$')
	static void clickDestinationLink() {
		Purchase.destination()
	}

	@When('^I follow the shipment tax link$')
	static void clickTaxLink() {
		client.tax()
				.stopIfFailure()
	}

	@When('^I navigate to the shipment line items$')
	static void navigateToShipmentLineitem() {
		Purchase.shipmentLineItems()
	}

	@Given('^I have purchased physical and digital items: "(.+?)" "(.+?)" "(.+?)"$')
	static void purchasePhysicalAndDigitalItems(String prod1, String prod2, String prod3) {
		List<String> productList = new ArrayList<String>()
		productList.add(prod1)
		productList.add(prod2)
		productList.add(prod3)

		for (String product : productList) {
			searchAndOpenItemWithKeyword(product)
			client.addtocartform()
					.addtodefaultcartaction(quantity: 1)
		}

		CommonMethods.selectAnyShippingOption()
		createPurchaseWithFollow()
	}

	@And('^I can follow the line item link for product "(.+?)" and back to the list$')
	static void findProductByName(String productName) {
		client.findElement { lineitem ->
			lineitem[NAME_FIELD] == productName
		}
		.list()
				.stopIfFailure()
	}

	static isProductLinkExists(def prodName) {
		def prodExists = false
		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
				if (client["name"] == prodName) {
					prodExists = true
				}
			}
		}
		return prodExists
	}

	@And('^I do not see a link to line item "(.+?)"$')
	static void verifyLinkToLineitemNotPresent(String prodName) {
		assertThat(isProductLinkExists(prodName))
				.as("A link to $prodName should not exist")
				.isFalse()
	}

	@Given('^I have previously made a purchase with item "(.+?)" quantity "(.+?)" and item "(.+?)" quantity "(.+?)"$')
	static void purchaseItemsWithQuantity(def prod1, def qty1, def prod2, def qty2) {
		FindItemBy.productName(prod1)
		Item.addItemToCart(qty1)

		FindItemBy.productName(prod2)
		Item.addItemToCart(qty2)

		Order.selectAnyShippingOption()
		Order.submitPurchase()
	}

	@And('^I go back to the purchase$')
	static void navigateToPurchases() {
		client.list()
				.shipment()
				.list()
				.purchase()
				.stopIfFailure()
	}

	@Then('^I see (.+?) address$')
	static void findAddressByName(String name) {
		assertAddress(client[ADDRESS_FIELD], ADDRESS)
	}

	@Then('^I can follow a link back to the shipment$')
	static void clickShipmentLink() {
		client.shipment()
				.stopIfFailure()
	}

	@Then('^I follow the shipping option link$')
	static void clickShipmentoptionLink() {
		client.shippingoption()
				.stopIfFailure()
	}

	@Then('^I follow the shipment total link$')
	static void clickTotalLink() {
		client.total()
				.stopIfFailure()
	}

	@Then('^I see shipping (.+) carrier information (.+)$')
	static void verifyCarrierInformation(String shippingDisplayName, String carrierName) {
		assertThat(client[DISPLAY_NAME_FIELD].toString())
				.as("The display name is not as expected")
				.isEqualTo(shippingDisplayName)
		assertThat(client[CARRIER_FIELD].toString())
				.as("The shipping carrier is not as expected")
				.isEqualTo(carrierName)
	}

	@Then('^I can follow the link to options for that shipment line item$')
	static void clickOptionsLink() {
		assertLinkExists(client, "options")
		client.options()
				.stopIfFailure()
	}

	@Then('^I view purchase line item option (.+)$')
	static void navigateToPurchaseLineitemOption(String optionDisplayName) {
		client.GET("/")
				.defaultprofile()
				.purchases()
				.element()
				.lineitems()
				.element()
				.options()
		client.findElement { option ->
			option[DISPLAY_NAME_FIELD] == optionDisplayName
		}
		.stopIfFailure()
	}

	@And('^I should see item option value is (.+)$')
	static void verifyItemOptionValue(String valueDisplayName) {
		client.value()
				.stopIfFailure()
		assertThat(client[DISPLAY_NAME_FIELD].toString())
				.as("The display name is not as expected")
				.isEqualTo(valueDisplayName)
	}

	@And('^I can view the \"([^\"]+)\" option for that shipment line item$')
	static void viewLineitemShipmentOption(String optionDisplayName) {
		client.findElement { option ->
			option[DISPLAY_NAME_FIELD] == optionDisplayName
		}
		.stopIfFailure()
	}

	@And('^I can view the \"([^\"]+)\" value for that option$')
	static void verifyOptionValue(String valueDisplayName) {
		client.value()
				.stopIfFailure()
		assertThat(client[DISPLAY_NAME_FIELD].toString())
				.as("The display name is not as expected")
				.isEqualTo(valueDisplayName)
	}

	@And('^I can follow back links from a shipment option value all the way to the purchase$')
	static void navigateToPurchase() {
		client.option()
				.list()
				.lineitem()
				.list()
				.shipment()
				.list()
				.purchase()
				.stopIfFailure()
	}

	@When('^I go to the purchases$')
	static void navigateToProfilePurchases() {
		Purchase.resume()
	}

	@Then('^the purchase status is (.+)$')
	static void verifyPurchaseStatus(String valueStatus) {
		client.GET("/")
				.defaultprofile()
				.purchases()
				.element()
				.stopIfFailure()
		assertThat(client["status"])
				.as("The purchase status is not as expected")
				.isEqualTo(valueStatus)
	}

	@Then('^there are no shipments$')
	static void verifyShipmentLinkNotPresent() {
		Purchase.shipments()
		assertLinkDoesNotExist(client, "element")
	}

	@Then('^I do not see a link to options on a single sku item$')
	static void verifyOptinsLinkNotPresent() {
		assertLinkDoesNotExist(client, "options")
	}

	@Then('^I can follow the total link$')
	static void followTotalLink() {
		client.total()
				.stopIfFailure()
	}

	@And('^I can follow line item back links all the way to the purchase$')
	static void returnBackToPurchaseFromLineitem() {
		client.lineitem()
				.list()
				.shipment()
				.list()
				.purchase()
				.stopIfFailure()
	}

	@Then('^I can follow the price link$')
	static void followPriceLink() {
		client.price()
				.stopIfFailure()
	}

	@And('^I can follow the back link to the shipment line item option$')
	static void clickOptionLink() {
		client.option()
				.stopIfFailure()
	}

	@And('^I can follow the back link to the shipment line item options list$')
	static void followListLink() {
		client.list()
				.stopIfFailure()
	}

	@When('^Adding an item with item code (.+) and quantity (.*) to the cart$')
	static void addItemToCartWithQty(def skuCode, def quantity) {
		FindItemBy.skuCode(skuCode)
		Item.addItemToCartWithoutFollow(quantity)
	}

	@Then('^the order is submitted$')
	static void submitOrder() {
		Order.submitPurchase()
	}

	@Then('^the payment instrument created is used for the order$')
	static void verifyPaymentInstrumentUsedForOrder() {
		client.paymentinstruments()
				.element()
				.stopIfFailure()

		assertThat(client["name"])
				.as("Instrument name is not as expected")
				.isEqualTo(DEFAULT_PAYMENT_CONFIGURATION_NAME)
	}

	@And('^The store the shopper is in fulfils shipments with no delay$')
	static void fulfilShipmentNoDelay() {
		// This can't be controlled for Cortex, but the default store's warehouse has a pick-pack delay of 0
	}

	@Then('^the number of purchase lineitems is (\\d+)$')
	static void verifyPurchaseLineitemQty(int numLineItems) {
		Purchase.resume()
		Purchase.lineitems()
		assertThat(client.body.links.findAll { it.rel == "element" })
				.size()
				.as("Number of purchase line items is not as expected")
				.isEqualTo(numLineItems)
	}

	@Then('^there exists a purchase line item for item (.+) with configurable fields:$')
	static void verifyPurchaseContainsItemWithConfiguredFields(String itemCode, DataTable modifierFieldsTable) {
		Map<String, String> configurationFields = modifierFieldsTable.asMap(String, String)
		def actualMap
		Purchase.resume()
		Purchase.lineitems()

		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				actualMap = client.body.configuration
				if (configurationFields == actualMap) {
					return true
				}
			}
		}

		assertThat(actualMap)
				.as("Configurable field values are not as expected")
				.isEqualTo(configurationFields)
	}

	static void getLineItemByName(def itemName) {
		client.GET("/")
				.defaultprofile()
				.purchases()
				.element()
				.lineitems()
				.findElement { lineitem ->
			lineitem[NAME_FIELD] == itemName
		}
		.stopIfFailure()
	}

	static void addNewUSAddressForZeroTaxState() {
		Profile.createAddress("US", "", "Anchorage", "", "", "99501", "AK",
				"123 Main Street", "testFamilyName", "testGivenName")
	}

	@When('^I lookup the newly purchased number$')
	static void getLatestPurchaseNumber() {
		CommonMethods.purchaseNumberLookup(savedPurchaseNumber)
	}

	@Then('^the purchase number matches my new purchase$')
	static void verifyPurchaseNumberMatches() {
		assertThat(client.body.'purchase-number')
				.as("The purchase number is not as expected")
				.isEqualTo(savedPurchaseNumber)
	}

	@When('^I look up an invalid purchase number (.+?)$')
	static void lookupInvalidPurchaseNumber(String purchaseNumber) {
		purchaseLookupByInvalidNumber(purchaseNumber)
	}

	static void purchaseLookupByInvalidNumber(final String purchaseNumber) {
		client.GET("/")
				.lookups().purchaselookupform()
				.purchaselookupaction(["purchase-number": purchaseNumber])
				.stopIfFailure()
	}

	@And('^I lookup other user\'s purchase number$')
	static void lookupOthersPurchaseNumber() {
		purchaseLookupByInvalidNumber(savedPurchaseNumber)
	}

	@And('^I (?:create|have) an order for scope (.+) with following skus?$')
	static void purchaseSKUsWithDefaultPaymentAndBillingAddressAndWaitForRelease(final String scope, final DataTable dataTable) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope)
		CommonMethods.addItemsToCart(dataTable)
		CommonMethods.addPaymentInstrumentAndBillingAddress()
		Order.submitPurchaseAndWaitForRelease()
	}

	@And('^I (?:create|have) an order that will be on hold for scope (.+) with following skus?$')
	static void purchaseSKUsWithDefaultPaymentAndBillingAddress(final String scope, final DataTable dataTable) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope)
		CommonMethods.addItemsToCart(dataTable)
		CommonMethods.addPaymentInstrumentAndBillingAddress()
		Order.submitPurchase()
	}

	@And('^I (?:create|have) a failed order for scope (.+) with following skus?$')
	static void purchaseSKUsWithFailingReservationPaymentAndBillingAddress(String scope, DataTable dataTable) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope)
		CommonMethods.addItemsToCart(dataTable)
		Profile.addUSBillingAddress()
		Payment.createUnsavedPaymentInstrument(RESERVE_FAILS_PAYMENT_CONFIGURATION_NAME)
		Order.submitPurchaseWithoutFollow()
	}

	@And('^I (?:create|have) an account order for scope (.+) and user (.+) and account (.+) with following skus?$')
	static void purchaseAccountSKUsWithDefaultPaymentAndBillingAddress(String scope, String username, String accountSharedId, DataTable dataTable) {
		LoginSteps.loginAsRegisteredShopperOnScope(username, scope)
		LoginSteps.addHeaderValue("x-ep-account-shared-id", accountSharedId)
		CommonMethods.addItemsToCart(dataTable)
		Order.addOrderPaymentInstrument()
		Order.submitPurchase()
	}

	@And('^I (?:create|have) an order with (.+) address with following skus?$')
	static void purchaseWithDefaulBillingAddress(String countryCode, DataTable dataTable) {
		CommonMethods.addItemsToCart(dataTable)
		if (countryCode.equals("CA")) {
			Profile.addCanadianBillingAddress()
		} else if (countryCode.equals("US")) {
			Profile.addUSBillingAddress()
		} else if (countryCode.equals("GB")) {
			Profile.addGBBillingAddress()
		}
		AddressQuery.setEmail()
		Order.submitPurchase()
	}

	@And('^I (?:create|have) an order with Canadian address for scope (.+) with following skus?$')
	static void purchaseWithCanadianAddress(String scope, DataTable dataTable) {
		purchaseWithBillingCountryCode(scope, "CA", dataTable)
	}

	@And('^I (?:create|have) an order with GB address for scope (.+) with following skus?$')
	static void purchaseWithGBAddress(String scope, DataTable dataTable) {
		purchaseWithBillingCountryCode(scope, "GB", dataTable)
	}

	@And('^I (?:create|have) an order with US address for scope (.+) with following skus?$')
	static void purchaseWithUSAddress(String scope, DataTable dataTable) {
		purchaseWithBillingCountryCode(scope, "US", dataTable)
	}

	static void purchaseWithBillingCountryCode(String scope, String countryCode, DataTable dataTable) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope)
		CommonMethods.addItemsToCart(dataTable)
		if (countryCode.equals("CA")) {
			Profile.addCanadianBillingAddress()
		} else if (countryCode.equals("US")) {
			Profile.addUSBillingAddress()
		} else if (countryCode.equals("GB")) {
			Profile.addGBBillingAddress()
		}
		Order.addOrderPaymentInstrument()
		Order.submitPurchase()
	}

	@And('^I have an order for scope (.+) with sku (.+), quantity (.+) and following configurable fields$')
	static void createOrderWithConfigurableFields(String scope, String skuCode, int quantity, DataTable dataTable) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope)
		Map<String, String> configurationFields = dataTable.asMap(String, String)
		FindItemBy.skuCode(skuCode)
		Item.addItemToCart(quantity, configurationFields)
		CommonMethods.addPaymentInstrumentAndBillingAddress()
		Order.submitPurchase()
	}

	@And('^I create an order for scope (.+) with coupon (.+) for following sku$')
	static void createOrderWithPromotion(String scope, String couponCode, DataTable dataTable) {
		LoginSteps.registerNewShopperAndLoginWithScope(scope)
		CommonMethods.addItemsToCart(dataTable)
		CommonMethods.addPaymentInstrumentAndBillingAddress()
		Order.applyCoupon(couponCode)
		Order.submitPurchase()
	}

	@Then('^purchase shipping option cost is (.+)$')
	static void verifyPurchaseShippingCost(String shippingCost) {
		assertThat(Purchase.getShippingOptionCost())
				.as("The purchase shipping option cost is not as expected")
				.isEqualTo(shippingCost)
	}

	@And('^I fill in all the required purchase info with (.+) address$')
	static void createOrderWithPromotion(String countryCode) {
		Profile.addEmailWithoutFollow(TEST_EMAIL_VALUE)
		if (countryCode.equals("CA")) {
			Profile.addCanadianBillingAddress()
		} else if (countryCode.equals("US")) {
			Profile.addUSBillingAddress()
		}
		Order.addDefaultToken()
	}

	@Then('^there is an element for the newly create order$')
	static void findElementForSavedPurchaseNumber() {
		def elementExists = false
		def resultUri = client.body.self.uri
		client.findElement {
			element ->
				if (element["purchase-number"] == savedPurchaseNumber)
					elementExists = true
				resultUri = element.body.self.uri
		}

		assertThat(elementExists)
				.as("element with purchase-number = $savedPurchaseNumber not found")
				.isTrue()

		client.GET(resultUri)
	}

	@Then('^there is not an element for the newly create order$')
	static void verifyNoElementForSavedPurchaseNumber() {
		def elementExists = false
		def resultUri = client.body.self.uri
		client.findElement {
			element ->
				if (element["purchase-number"] == savedPurchaseNumber)
					elementExists = true
				resultUri = element.body.self.uri
		}

		assertThat(elementExists)
				.as("element with purchase-number = $savedPurchaseNumber was found")
				.isFalse()

		client.GET(resultUri)
	}
	
}
