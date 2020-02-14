/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cortex.dce.items

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DISPLAY_NAME_FIELD
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkExists
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.DataTable
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import org.json.JSONArray

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.*

class Items {

	@Then('^the (product|SKU|navigation node) attributes contain$')
	static void verifyAttributes(def description, DataTable dataTable) {
		def nameValueList = dataTable.asList(NameValue)

		JSONArray jsonArray = (JSONArray) client["details"];

		for (NameValue NameValue : nameValueList) {
			Map<String, List<String>> map = NameValue.getAttributeMap()
			map.each {
				boolean attributeFound = false
				if (jsonArray != null) {
					int len = jsonArray.length();
					for (int i = 0; i < len; i++) {
						if (jsonArray.get(i).getAt("name") == it.key) {
							ArrayList<String> values = it.value
							def attribute = jsonArray.get(i)
							if (attribute.getAt("display-name").toString().contains(values.get(0))
									&& attribute.getAt("display-value").toString().contains(values.get(1))) {
								attributeFound = true;
							}
						}
					}
				}
				assertThat(attributeFound)
						.as("Unable to find name: " + it.key + " value: " + it.value)
						.isTrue()
			}
		}
	}

	static class NameValue {
		String name
		String displayName
		String displayValue

		Map<String, List<String>> attributeMap
		List<String> valuesList

		def getAttributeMap() {
			attributeMap = new HashMap<String, List<String>>()
			valuesList = new ArrayList<String>()
			valuesList.add(displayName)
			valuesList.add(displayValue)
			attributeMap.put(name, valuesList)
			return attributeMap
		}
	}

	@Then('^I view the item definition for item with code (.+)$')
	static void viewItemDefinitionsByCode(def itemCode) {
		FindItemBy.skuCode(itemCode)
		Item.definition()
	}

	@Then('^I request the item definition of (.+) in language (.+)$')
	static void viewItemDefinitionsInLanguage(def itemCode, def locale) {
		client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
		FindItemBy.skuCode(itemCode)
		Item.definition()
	}

	@Then('^I request item with code (.+) in language (.+)$')
	static void viewItemByCodeInLanguage(def itemCode, def locale) {
		client.headers.put("x-ep-user-traits", "LOCALE=" + locale)
		CommonMethods.lookup(itemCode)
	}

	@Then('^the details do not contain an element with name (.+)$')
	static void verifyAttributeDetailsNotContainElement(def attributeName) {
		boolean found = false
		def details = client["details"]

		for (def detail : details) {
			if (detail["display-name"] == attributeName) {
				found = true
			}
		}
		assertThat(found)
				.as("Attribute $attributeName was found in the cortex response")
				.isFalse()
	}

	@Then('^the attribute with (.+) equal to (.+) has (.+) equal to (.+)$')
	static void verifyAttributeValue(def keyField, def keyValue, def valueField, def valueValue) {
		boolean found = false
		def details = client["details"]

		for (def detail : details) {
			if (detail[keyField] == keyValue) {
				found = true
				assertThat(detail[valueField])
						.as("$valueField should be $valueValue")
						.isEqualTo(valueValue)
			}
		}
		assertThat(found)
				.as("Attribute $keyValue was not found in the cortex response")
				.isTrue()
	}

	@Then('^the item has (.+) components$')
	static void verifyComponentQty(int expected) {
		Item.definition_components()
		CommonMethods.verifyNumberOfElements(expected)
	}

	@Then('^the (?:bundle|nested bundle component) has (.+) components$')
	static void verifyBundleComponentQty(int expected) {
		Item.components()
		CommonMethods.verifyNumberOfElements(expected)
	}

	@Then('^I examine the (?:bundle|nested bundle) component (.+)$')
	static void viewComponentByName(def nestedBundleName) {
		Item.definition_components()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
	}

	@Then('^the component has a reference to the standalone item (.+)$')
	static void verifyComponentHasReferenceToItem(def standaloneItem) {
		Item.definition_components()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", standaloneItem)
		assertLinkExists(client, "standaloneitem")
		Item.standaloneitem()
		Item.definition()

		assertThat(client.body."display-name")
				.as("display-name is not as expected")
				.isEqualTo(standaloneItem)
	}

	@Then('^I open the nested bundle component (.+)$')
	static void viewNestedBundleComponent(def nestedBundleName) {
		Item.components()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
	}

	@Then('^I view the list of (?:bundle|nested bundle) components')
	static void viewBundleComponentsList() {
		Item.components()
	}

	@Then('^I attempt to add the nested bundle (.+) component (.+) to cart$')
	static void addBundleComponentToCart(def nestedBundleName, def component) {
		Item.definition_components()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", nestedBundleName)
		Item.standaloneitem()
		Item.definition_components()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", component)
		Item.standaloneitem()
	}

	@Then('^I view the list of options$')
	static void viewItemOptionList() {
		Item.options()
	}

	@Then('^I view the option (.+)$')
	static void viewItemOption(def optionName) {
		Item.options()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", optionName)
	}

	@Then('^I view the value of option (.+)$')
	static void viewItemOptionValue(def optionName) {
		Item.options()
		CommonMethods.openLinkRelWithFieldWithValue("element", "display-name", optionName)
		Item.value()
	}

	@Then('^I select option (.+) value (.+)$')
	static void selectItemOption(def optionName, def optionValue) {
		Item.selectSkuOption(optionName, optionValue)
	}

	@Then('^I am presented with item having (.+)$')
	static void verifyItemByCode(def itemCode) {
		client.code()
				.stopIfFailure()

		assertThat(client.body.code)
				.as("sku code is not as expected")
				.isEqualTo(itemCode)
	}

	@Then('^a bundle with a selection rule of Select All$')
	static void getSelectAllRule() {}

	@When('^lookup the bundle by (.+) and add the (?:.+) to cart$')
	static void lookupAddToCartByCode(def itemCode) {
		CommonMethods.lookupAndAddToCart(itemCode, 1)
	}

	@Then('^the (.+) will be added to cart as a root LineItem$')
	static void verifyItemInCartByCode(def itemCode) {
		client.GET("/")
				.defaultcart()
				.lineitems()
				.element()
				.item()
				.code()

		assertThat(client.body.code)
				.as("Item not found for sku code: " + itemCode)
				.isEqualTo(itemCode)
	}

	@Then('^all (.+) constituents of the (.+) will be added to cart as DependentLineItems$')
	static void verifyAllConstituentsAddedToCart(int numberOfConstituents, def purchasableItemSkuCode) {
		Cart.verifyCartItemsBySkuCode(purchasableItemSkuCode)
		LineItem.dependentlineitems()
		CommonMethods.verifyNumberOfElements(numberOfConstituents)
		CommonMethods.verifyDependentLineItemsContainAllConstituents(purchasableItemSkuCode)
	}

	@Then('^all (.+) constituents of the dependent line item (.+) in line item (.+) will be added to cart as its DependentLineItems$')
	static void verifyAllConstituentsOfDependentLineItem(int numberOfConstituents, String dependentLineItemSkuCode, String lineItemSkuCode) {
		DependentLineItems.getDependentLineItemsOfDependentLineItem(lineItemSkuCode, dependentLineItemSkuCode)
		CommonMethods.verifyNumberOfElements(numberOfConstituents)
		CommonMethods.verifyDependentLineItemsContainAllDependentLineItems(lineItemSkuCode, dependentLineItemSkuCode)
	}

	@Given('^a cart containing a (.+) and its Constituents$')
	static void addItemToCartByCode(def itemCode) {
		CommonMethods.lookupAndAddToCart(itemCode, 1)
	}

	@When('^regardless of the type of selection rule of the Bundle, remove the (.+) from the Cart$')
	static void deleteLineitemByCode(def itemCode) {
		def lineitemUri = CommonMethods.findCartLineItemUriBySkuCode(itemCode)
		client.DELETE(lineitemUri)

		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@Then('^the Bundle is removed, as well as all of its DepedentLineItems$')
	static void verifyBundleRemoved() {}

	@Given('^a non-Bundle Item, lookup the item by (.+) and add the Item to Cart$')
	static void lookupAddItemToCartByCode(def skuCode) {
		CommonMethods.lookupAndAddToCart(skuCode, 1)
	}

	@Then('^the item will be added to cart and will present a link to the DependentLineItems resource, but the resource will be empty$')
	static void verifyListOfDependentItemsIsEmpty() {
		client.GET("/")
				.defaultcart()
				.lineitems()
				.element()
				.dependentlineitems()

		CommonMethods.verifyNumberOfElements(0)
	}

	@Given('^I add (.+) in the Cart with Dependent Line Items$')
	static void verifyItemWithCodeIsInCart(def itemCode) {
		CommonMethods.lookupAndAddToCart(itemCode, 1)
	}

	@When('^navigate to each of their DependentLineItems link$')
	static void clickOnDependLineitemsLink() {}

	@Then('^the DependentLineItems link will be present, but will be empty on all leaf items$')
	static void verifyNumberOfElementsForEachDependLineitem() {
		client.GET("/")
				.defaultcart()
				.lineitems()
				.element()
				.dependentlineitems()

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
				client.dependentlineitems()
				CommonMethods.verifyNumberOfElements(0)
			}
		}
	}

	@When('^all Order need infos solved$')
	static void setAllNeedinfos() {
		CommonMethods.addEmailPaymentInstrumentAndAddress()
	}

	@And('^I complete the purchase after providing all required order info$')
	static void setNeedinfosAndSubmitPurchase() {
		CommonMethods.addEmailPaymentInstrumentAndAddress()
		Order.submitPurchase()
	}

	@And('^I complete the purchase after providing all required order info for (.+) cart$')
	static void setNeedinfosAndSubmitPurchaseForCart(final String cartName) {
		CommonMethods.addEmailPaymentInstrumentAndAddress()
		Order.submitPurchaseForCart(cartName)
	}

	@And('^the LineItems structure under the created Purchase$')
	static void verifyPurchaseLineitemsQty() {
		client.lineitems()
				.element()
				.components()
				.stopIfFailure()
		CommonMethods.verifyNumberOfElements(2)
	}

	@Then('^I should see offer name is (.+?)$')
	static void verifyOfferName(String offerName) {
		assertThat(client.offer().definition()[DISPLAY_NAME_FIELD])
				.as("Offer name is not as expected")
				.isEqualTo(offerName)
	}

}
