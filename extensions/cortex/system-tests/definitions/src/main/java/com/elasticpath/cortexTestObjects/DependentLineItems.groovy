package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

class DependentLineItems extends LineItems{

	static def getDependentLineItemUri(def lineitemSkuCode, def depndentLineitemSkuCode) {
		def dependentLineItemUri = ""

		Cart.lineitems()
		Cart.findCartElementBySkuCode(lineitemSkuCode)
		LineItem.dependentlineitems()
		client.findElement {
			lineItem ->
				def uri = lineItem.body.self.uri
				lineItem.item().code()
				if (client["code"] == depndentLineitemSkuCode) {
					dependentLineItemUri = uri
				}
		}
		assertThat(dependentLineItemUri)
				.as("Impossible to provide dependent line item uri. Dependent line item with provided sku code was not found.")
				.isNotEqualTo("")
		return dependentLineItemUri
	}

	static List getDependentLineItemsCodes(String rootItemUri) {
		def lineItemCodes = []

		client.GET(rootItemUri)
				.dependentlineitems()

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
						.item()
						.code()
				lineItemCodes.add(client["code"])
			}
		}

		return lineItemCodes
	}

	static void getDependentLineItemsOfDependentLineItem(String lineItemSkuCode, String dependentLineItemSkuCode){
		def dependentLineItemUri = getDependentLineItemUri(lineItemSkuCode, dependentLineItemSkuCode)

		client.GET(dependentLineItemUri)
				.dependentlineitems()
	}
}
