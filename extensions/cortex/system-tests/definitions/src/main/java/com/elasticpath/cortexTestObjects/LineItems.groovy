package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Line Items.
 */
class LineItems extends CommonMethods {


	static void verifyLineItemsBySkuCode(List<String> skuCodeList) {

		for (String skuCode : skuCodeList) {
			assertThat(isLineitemsContainElementWithSkuCode(skuCode))
					.as("Unable to find " + skuCode)
					.isTrue()
		}
		client.resume(CortexResponse.lineItemResponse)
	}

	static void verifyLineItemsByName(List<String> nameList) {

		for (String name : nameList) {
			assertThat(isLineitemsContainElementWithDisplayName(name))
					.as("Unable to find " + name)
					.isTrue()
		}
		client.resume(CortexResponse.lineItemResponse)
	}

	static boolean isLineitemsContainElementWithSkuCode(def skuCode) {
		client.lineitems()
		CortexResponse.lineItemResponse = null
		def lineItemsResponse = client.save()
		def skuExists = false
		client.resume(lineItemsResponse)
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				CortexResponse.lineItemResponse = client.save()
				LineItem.item()
				if (Item.getSkuCode() == skuCode) {
					return skuExists = true
				}
			}
		}
		return skuExists
	}

	static boolean isLineitemsContainElementWithDisplayName(def name) {
		client.lineitems()
		CortexResponse.lineItemResponse = null
		def lineItemsResponse = client.save()
		def skuExists = false
		client.resume(lineItemsResponse)
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				CortexResponse.lineItemResponse = client.save()
				LineItem.item()
				if (Item.getItemName() == name) {
					return skuExists = true
				}
			}
		}
		return skuExists
	}

}