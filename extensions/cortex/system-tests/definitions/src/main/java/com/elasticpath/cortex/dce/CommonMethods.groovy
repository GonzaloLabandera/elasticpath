/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortex.dce

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.ELEMENT_LINK
import static com.elasticpath.cortex.dce.SharedConstants.TEST_EMAIL_VALUE
import static org.assertj.core.api.Assertions.assertThat

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import cucumber.api.DataTable
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.auth.AUTH
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.EntityTemplate
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients

import com.elasticpath.cortexTestObjects.*

/**
 * Shared methods.
 */
class CommonMethods {

	static def getDisplayName() {
		return client["display-name"]
	}

	static String getLineItemUriForItemName(String itemDisplayName) {
		def lineitemUri

		client.findElement { element ->
			lineitemUri = client.body.self.uri
			client.item()
					.definition()
			assertThat(client["display-name"])
					.as("Cannot find item for the given item name.")
					.isEqualTo(itemDisplayName)
		}
		.stopIfFailure()
		return lineitemUri
	}

	static String getLineItemUriForItemCode(String itemCode) {
		def lineitemUri

		client.findElement { element ->
			lineitemUri = client.body.self.uri
			client.item()
					.code()
			assertThat(client["code"])
					.as("Cannot find item for the given item code.")
					.isEqualTo(itemCode)
		}
		.stopIfFailure()
		return lineitemUri
	}

	static void verifyLineitemsContainElementWithDisplayName(String itemDisplayName) {
		assertThat(getLineitemNamesList())
				.as("The lineitems do not contain the expected item.")
				.contains(itemDisplayName)
	}

	static void verifyLineitemsContainElementWithCode(String itemCode) {
		assertThat(getLineitemCodesList())
				.as("The lineitems do not contain the expected item.")
				.contains(itemCode)
	}

	static void verifyDependentLineItemsContainAllDependentLineItems(String lineItemSkuCode, String dependentLineItemSkuCode) {
		def itemCodes = getConstituentsCodes(dependentLineItemSkuCode).toSorted()
		def dependentLineItemsCodes = getNestedDependentLineItemsCodesList(lineItemSkuCode, dependentLineItemSkuCode).toSorted()
		assertThat(itemCodes)
				.as("The Dependent LineItems do not contain the expected items.")
				.containsExactlyInAnyOrderElementsOf(dependentLineItemsCodes)
	}

	static void verifyDependentLineItemsContainAllConstituents(String purchasableItemSkuCode) {
		def itemCodes = getConstituentsCodes(purchasableItemSkuCode).toSorted()
		def dependentLineItemsCodes = getDependentLineItemsCodesList(purchasableItemSkuCode).toSorted()
		assertThat(itemCodes)
				.as("The Dependent LineItems do not contain the expected items.")
				.containsExactlyInAnyOrderElementsOf(dependentLineItemsCodes)
	}

	static getConstituentsCodes(purchasableItemSkuCode) {
		def itemsCodes = []

		lookup(purchasableItemSkuCode)
		Item.definition_components()
		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
						.standaloneitem()
						.code()
				itemsCodes.add(client["code"])
			}
		}

		return itemsCodes
	}

	static findOfferByDisplayName(String itemDisplayName) {
		def offer = null
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				offer = client.save()
				client.definition()
				println("product name: " + client["display-name"])
				if (client["display-name"] == itemDisplayName) {
					CortexResponse.elementResponse = offer
					return true
				}
			}
		}

		assertThat(CortexResponse.elementResponse != null)
				.as("Unable to find " + itemDisplayName)
				.isTrue()

		client.resume(offer)
	}

	static void verifyLineitemsNotContainElementWithDisplayName(String itemDisplayName) {
		assertThat(getLineitemNamesList())
				.as("The lineitems contain the unexpected item.")
				.doesNotContain(itemDisplayName)
	}

	static void verifyLineitemsNotContainElementWithCode(String itemCode) {
		assertThat(getLineitemCodesList())
				.as("The lineitems contain the unexpected item.")
				.doesNotContain(itemCode)
	}

	static List getLineitemNamesList() {
		def lineItemNames = []

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
				client.item()
						.definition()
				lineItemNames.add(client["display-name"])
			}
		}
		return lineItemNames
	}

	static List getLineitemCodesList() {
		def lineItemCodes = []

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
				client.item()
						.code()
				lineItemCodes.add(client["code"])
			}
		}
		return lineItemCodes
	}

	static List getDependentLineItemsCodesList(String lineItemCode) {
		return DependentLineItems.getDependentLineItemsCodes(findCartLineItemUriBySkuCode(lineItemCode))
	}

	static List getNestedDependentLineItemsCodesList(String lineItemSkuCode, String dependentLineItemCode) {
		return DependentLineItems.getDependentLineItemsCodes(DependentLineItems.getDependentLineItemUri(lineItemSkuCode, dependentLineItemCode))
	}

	static void verifyNumberOfElements(int numElements) {
		def elements = client.body.links.findAll { link ->
			link.rel == ELEMENT_LINK
		}
		assertThat(elements)
				.as("Expected number of elements does not match")
				.hasSize(numElements)
	}

	static void addProductToCart(String triggerProduct, int quantity) {
		searchAndOpenItemWithKeyword(triggerProduct)

		client.addtocartform()
				.addtodefaultcartaction(quantity: quantity)
				.stopIfFailure()
	}

	static def findCartLineItemUriByDisplayName(String displayName) {
		Cart.getCart()
		Cart.findCartElementByProductName(displayName)
		return client.body.self.uri
	}

	static def findCartLineItemUriBySkuCode(String skuCode) {
		Cart.lineitems()
		Cart.findCartElementBySkuCode(skuCode)
		client.stopIfFailure()
		return client.body.self.uri
	}

	static def findCartLineItemUriBySkuCodeAndConfigurableFieldValues(String skuCode, DataTable dataTable) {
		Cart.lineitems()
		findCartElementBySkuCodeAndConfigurableFieldValues(skuCode, dataTable)
		client.stopIfFailure()
		return client.body.self.uri
	}

	static def findCartElementByDisplayName(displayName) {

		def itemExists = false
		def elementResponse = null

		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				elementResponse = client.save()
				client.item()
						.definition()
				if (client["display-name"] == displayName) {
					itemExists = true
				}
			}
		}
		assertThat(itemExists)
				.as("Item not found for item name - " + displayName,)
				.isEqualTo(true)
		client.resume(elementResponse)
	}

	static def findCartElementBySkuCodeAndConfigurableFieldValues(skuCode, DataTable dataTable) {
		def itemExists = false
		def elementResponse = null
		def configValueMatch = false
		def configMatch = true
		def failureKey
		def failureValue

		client.body.links.find {
			configMatch = true
			if (it.rel == "element") {
				client.GET(it.href)
				elementResponse = client.save()
				client.item()
				client.code()
				if (client["code"] == skuCode) {
					itemExists = true
				} else {
					itemExists = false
				}
			}

			if (!itemExists)
				return false

			client.resume(elementResponse)
			def mapList = dataTable.asMap(String, String)

			for (def map : mapList) {
				configValueMatch = false
				def key = map.getKey()
				def value = map.getValue()
				if (client.body.'configuration'."$key" == value) {
					configValueMatch = true
				} else {
					configValueMatch = false
					configMatch = false
					failureKey = key
					failureValue = value
				}
			}
			if (configValueMatch && configMatch)
				return true
		}

		assertThat(itemExists)
				.as("Unable to find skuCode $skuCode")
				.isTrue()

		assertThat(configMatch)
				.as("Unable to find configurable field: $failureKey or/and value: $failureValue")
				.isTrue()

		assertThat(configValueMatch)
				.as("Unable to find configurable field values")
				.isTrue()
	}

	static def searchAndOpenItemWithKeyword(keyword) {
		FindItemBy.productName(keyword)
	}

	static def searchForOfferAndOpenOfferWithKeyword(keyword) {
		FindItemBy.offerByName(keyword)
	}

	static def searchForOfferAndOpenOfferWithSkuCode(skuCode) {
		FindItemBy.offerBySkuCode(skuCode)
	}

	static def getKeywordSearchForm() {
		client.GET("/")
				.searches()
				.keywordsearchform()
	}

	static def search(keyword, pageSize) {
		getKeywordSearchForm()
				.itemkeywordsearchaction(
				['keywords' : keyword,
				 'page-size': pageSize]
		)
	}

	static def getOfferSearchForm() {
		client.GET("/")
				.searches()
				.offersearchform()
	}

	static def searchForOffer(keyword, pageSize) {
		getOfferSearchForm()
				.offersearchaction(
				['keywords' : keyword,
				 'page-size': pageSize]
		)
	}

	static def navigate(categoryName, itemName) {
		client.GET("/")
				.navigations()
				.findElement {
			category ->
				category["name"] == categoryName
		}
		.items()
		findItemByDisplayName(itemName)
	}

	static def findItemByDisplayName(displayName) {
		client.findElement {
			item ->
				def definition = item.definition()
				definition["display-name"] == displayName
		}
		.stopIfFailure()
	}

	static def selectAnyShippingOption() {
		client.GET("/")
				.defaultcart()
				.order()
				.deliveries()
				.element()
				.shippingoptioninfo()
				.selector()

		def choiceExists = false
		client.body.links.find {
			if (it.rel == "choice") {
				choiceExists = true
			}
		}
		if (choiceExists) {
			client.choice().selectaction()
		}
	}

	static def lookup(def skuCode) {
		FindItemBy.skuCode(skuCode)
	}

	static def lookupAndAddToCart(def skuCode, int qty) {
		FindItemBy.skuCode(skuCode)
		Item.addItemToCart(qty)
	}

	static def lookupAndAddToCartNoFollow(def itemCode, def qty) {
		FindItemBy.skuCode(itemCode)
		Item.addItemToCartWithoutFollow(qty)
	}

	static def searchAndAddProductToCart(triggerProduct) {
		FindItemBy.productName(triggerProduct)
		Item.addItemToCart(1)
	}

	static def searchAndAddProductToCart(triggerProduct, qty) {
		FindItemBy.productName(triggerProduct)
		Item.addItemToCart(qty)
	}

	static void addPersonalisationHeader(String personalisationKey, String personalisationValue) {
		String headerKey = "x-ep-user-traits"
		String headerValue = personalisationKey + "=" + personalisationValue
		client.headers.putAt(headerKey, headerValue)
	}

	static void selectShippingOption(String shippingOptionName) {
		findShippingOptionChoiceOrChosen(shippingOptionName)
		selectIfNotAlreadySelected()
	}

	static void createAddress(String countryCode, String extendedAddress, String locale, String organization, String phoneNumber, String postalCode, String regionCode,
							  String streetAddress, String familyName, String givenName) {
		client.GET("/")
				.defaultprofile()
				.addresses()
				.addressform()
				.createaddressaction(
				[
						address: ["country-name"    : countryCode,
								  "extended-address": extendedAddress,
								  "locality"        : locale,
								  "organization"    : organization,
								  "phone-number"    : phoneNumber,
								  "postal-code"     : postalCode,
								  "region"          : regionCode,
								  "street-address"  : streetAddress],
						name   : ["family-name": familyName,
								  "given-name" : givenName]
				]
		)
				.stopIfFailure()
	}

	static void selectShippingAddressByPostalCode(String postalCode) {
		client.GET("/")
				.defaultcart()
				.order()
				.deliveries()
				.findElement {
			element ->
				def destinationinfo = element.destinationinfo()
				followSelectedAddress(destinationinfo, postalCode)
		}

		client.GET("/").defaultcart()
				.stopIfFailure()
	}




	static void followSelectedAddress(destinationinfo, String postalCode) {
		destinationinfo.selector()
				.findChoice {
			addressOption ->
				def description = addressOption.description()
				description["address"]["postal-code"] == postalCode
		}
		.selectaction()
				.follow()
				.stopIfFailure()
	}

	static void findShippingOptionChoiceOrChosen(String shippingOptionName) {
		client.GET("/")
				.defaultcart()
				.order()
				.deliveries()
				.element()
				.shippingoptioninfo()
				.selector()
				.findChoiceOrChosen {
			shippingService ->
				def description = shippingService.description()
				description["display-name"] == shippingOptionName
		}
	}

	static void selectIfNotAlreadySelected() {
		def selectAction = client.body.links.findAll {
			link ->
				link.rel == "selectaction"
		}

		if (selectAction.toList().size() > 0) {
			client.selectaction()
					.follow() // back to selector
		} else {
			client.selector()
		}
	}

	static void openLinkRelWithFieldWithValue(def linkrel, def field, def value) {
		def elementExists = false
		def elementResponse
		client.body.links.find {
			if (it.rel == linkrel) {
				client.GET(it.href)
				if (client[field] == value) {
					elementResponse = client.save()
					elementExists = true
				}
			}
		}

		assertThat(elementExists)
				.as("Unable to find $linkrel with $field = $value")
				.isTrue()

		client.resume(elementResponse)
				.stopIfFailure()
	}

	static def purchaseNumberLookup(def purchaseNumber) {
		client.GET("/")
				.lookups()
				.purchaselookupform()
				.purchaselookupaction(["purchase-number": purchaseNumber])
				.follow()
				.stopIfFailure()
	}

	static def navigationLookupCode(def categoryCode) {
		client.GET("/")
				.lookups()
				.navigationlookupform()
				.navigationlookupaction([code: categoryCode])
				.follow()
				.stopIfFailure()
	}

	static void addEmailPaymentInstrumentAndAddress() {
		Profile.addEmailWithoutFollow(TEST_EMAIL_VALUE)
		Order.addOrderPaymentInstrument()
		Profile.createUniqueAddress()
	}

	static boolean isLineitemsContainElementWithDisplayName(def name) {
		return LineItems.isLineitemsContainElementWithDisplayName(name)
	}


	static def verifyNavigationLinkDoesNotExist(def expectedLink) {
		def expectedLinks = client.body.links.findAll {
			link ->
				link.rel == expectedLink
		}
		assertThat(expectedLinks)
				.size()
				.as("$expectedLink links found but should not exist: $client.body.links")
				.isEqualTo(0)
	}

	static def getSelfUri() {
		return client.body.self.uri
	}

	static void delete(def uri) {
		client.DELETE(uri)
				.stopIfFailure()
	}

	static void edit(def uri, def request) {
		client.PUT(uri, request)
				.stopIfFailure()
	}

	static def getPurchasePrice(def key) {
		return client.body."purchase-price"[0][key]
	}

	static def getListPrice(def key) {
		return client.body."list-price"[0][key]
	}

	static void list() {
		client.list()
				.stopIfFailure()
	}

	static void total() {
		client.total()
	}

	static void addPaymentInstrumentAndBillingAddress(){
		Order.addOrderPaymentInstrument()
		Profile.addUSBillingAddress()
	}

	static void addItemsToCart(DataTable dataTable){
		Map<String, String> orderDetails = dataTable.asMap(String, String)
		Iterator<Map.Entry<String, String>> it = orderDetails.entrySet().iterator()
		it.next() // skip skuCode -> quantity Pair. cannot remove due to map conversion above returning UnmodifiableMap
		while (it.hasNext()) {
			Map.Entry<String, String> pair = it.next()
			lookupAndAddToCart(pair.getKey(), Integer.parseInt(pair.getValue()))
		}
	}

	static def getActualList(String linkRel, String field) {
		def actualList = []
		client.body.links.findAll {
			if (it.rel == linkRel) {
				client.GET(it.href)
				actualList.add(client[field])
			}
		}
		return actualList
	}

	/**
	 * Searches for element by field name and open the element.
	 * @param keyword
	 */
	static void findElementBy(String resource, String field, String value) {
		CortexResponse.elementResponse = null
		def element = null
		client.body.links.find {
			if (it.rel == resource) {
				client.GET(it.uri)
				element = client.save()
				if (client["$field"] == value) {
					CortexResponse.elementResponse = element
					return true
				}
			}
		}

		assertThat(CortexResponse.elementResponse != null)
				.as("Unable to find " + value)
				.isTrue()

		client.resume(element)
	}

	static void submitPostNTimesConcurrently(String url, int numberOfConcurrentSubmissions) {
		def accessToken = client.headers[AUTH.WWW_AUTH_RESP].toString()
		ExecutorService threadPool = Executors.newFixedThreadPool(numberOfConcurrentSubmissions)
		for (int i = 0; i < numberOfConcurrentSubmissions; i++) {
			threadPool.execute({ ->
				submitPostRequest(url, accessToken)
			});
		}
		threadPool.awaitTermination(numberOfConcurrentSubmissions, TimeUnit.SECONDS)
	}

	private static void submitPostRequest(String url, String accessToken) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader(HttpHeaders.AUTHORIZATION, accessToken);
		httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
		client.execute(httpPost);
		client.close();
	}

}
