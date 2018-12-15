package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Cart.
 */
class Cart extends CommonMethods {


	static void getCart() {
		client.GET("/")
				.defaultcart()
				.stopIfFailure()
		CortexResponse.cartResponse = client.save()
	}

	static void resume() {
		client.resume(CortexResponse.cartResponse)
	}

	static int getCartTotalQuantity() {
		return client["total-quantity"]
	}


	static void lineitems() {
		getCart()
		client.lineitems()
				.stopIfFailure()
	}

	static void discount() {
		getCart()
		client.discount()
				.stopIfFailure()
	}

	static void order() {
		client.order()
				.stopIfFailure()
		CortexResponse.orderResponse = client.save()
	}

	static void total() {
		getCart()
		client.total()
				.stopIfFailure()
	}

	static void appliedpromotions() {
		client.appliedpromotions()
				.stopIfFailure()
	}

	static void verifyCartItemsBySkuCode(List<String> skuCodeList) {
		CortexResponse.cartResponse = null
		getCart()
		LineItems.verifyLineItemsBySkuCode(skuCodeList)
	}

	static void verifyCartItemsBySkuCode(String skuCode) {
		List<String> skuList = new ArrayList<>()
		skuList.add(skuCode)
		verifyCartItemsBySkuCode(skuList)
	}

	static void findCartElementBySkuCode(def skuCode) {
		verifyCartItemsBySkuCode(skuCode)
	}

	static void verifyCartItemsByProductName(List<String> prodNameList) {
		LineItems.verifyLineItemsByName(prodNameList)
	}

	static void verifyCartItemsByProductName(String productName) {
		List<String> prodNameList = new ArrayList<>()
		prodNameList.add(productName)
		verifyCartItemsByProductName(prodNameList)
	}

	static void findCartElementByProductName(def productName) {
		verifyCartItemsByProductName(productName)
	}

	static void verifyPromotionByName(List<String> promoNameList) {
		appliedpromotions()
		def appliedPromoResponse = client.save()
		boolean promoExists = false
		for (String promoName : promoNameList) {
			client.resume(appliedPromoResponse)
			client.body.links.find {
				if (it.rel == "element") {
					client.GET(it.uri)
					if (promoName == client["name"]) {
						return promoExists = true
					}
				}
			}

			assertThat(promoExists)
					.as("Unable to find promotion: $promoName")
					.isTrue()
		}
	}

	static void verifyPromotionByName(String promoName) {
		List<String> promoNameList = new ArrayList<>()
		promoNameList.add(promoName)
		verifyPromotionByName(promoNameList)
	}

	static void clearCart() {
		getCart()
		client.lineitems()
		client.DELETE(client.body.self.uri)
	}

}