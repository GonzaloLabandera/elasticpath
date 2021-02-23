package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * MultiCart.
 */
class MultiCart extends CommonMethods {

	static void getCarts() {

		client.GET("/")
				.carts()
				.stopIfFailure()
	}

	static void createCart(def cartId) {

		def fields = [:]
		fields.put('name', cartId)

		client.GET("/")
				.carts()
				.createcartform()
				.createcartaction(
						[
								descriptor: fields
						])
				.stopIfFailure()
	}

	static void getCreateCartForm() {

		client.GET("/")
				.carts()
				.createcartform()
				.stopIfFailure()
	}

	static void getCreateCartFormWithoutCheck() {

		client.GET("/")
				.carts()
				.createcartform()
	}

	static void updateName(def cartName) {

		def uri = client.descriptor().body.self.uri
		client.PUT(uri, [
				name: cartName
		])
	}

	static void getCart(def cartName) {
		getCarts()
		def cartResponse = null
		def cartExist = false
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.href)
				cartResponse = client.save()
				client.descriptor()
				if (client["name"] == cartName) {
					cartExist = true
				}
			}
		}

		assertThat(cartExist)
				.as("Unable to find the cart with name - $cartName")
				.isTrue()

		client.resume(cartResponse)
	}

	static def findCartLineItemUriBySkuCode(String cartName, String skuCode) {
		getCart(cartName)
		LineItems.verifyLineItemsBySkuCode(skuCode)
		client.stopIfFailure()
		return client.body.self.uri
	}

	static void clearCart(String cartName) {
		getCart(cartName)
		client.lineitems()
		client.DELETE(client.body.self.uri)
	}

	static void order() {
		client.order()
				.stopIfFailure()
	}

	static void getOrder(String cartName) {
		getCart(cartName)
		order()
	}


	static void purchaseForm() {
		client.purchaseform()
				.stopIfFailure()
	}

	static void submitPurchase() {
		submitPurchaseWithoutFollow()
		client.follow()
				.stopIfFailure()
		CortexResponse.purchaseResponse = client.save()
		Purchase.setPurchaseNumber()
		println("purchase number: " + Purchase.getPurchaseNumber())
	}

	static void submitPurchaseWithoutFollow() {
		purchaseForm()
		client.submitorderaction()
				.stopIfFailure()
	}

	static void deleteCart(String cartName) {
		getCart(cartName)
		client.DELETE(client.body.self.uri)
	}
}