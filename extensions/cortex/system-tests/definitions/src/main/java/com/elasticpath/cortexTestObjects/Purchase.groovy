package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Order.
 */
class Purchase extends CommonMethods {

	static String purchaseNumber

	static void setPurchaseNumber() {
		purchaseNumber = client["purchase-number"]
	}

	static String getPurchaseNumber(){
		return purchaseNumber
	}

	static void resume() {
		client.resume(CortexResponse.purchaseResponse)
	}

	static void coupons() {
		client.coupons()
				.stopIfFailure()
	}

	static void discount() {
		client.discount()
				.stopIfFailure()
	}

	static void billingaddress() {
		client.billingaddress()
				.stopIfFailure()
	}

	static void purchase() {
		client.purchase()
				.stopIfFailure()
	}

	static void lineitems() {
		resume()
		client.lineitems()
				.stopIfFailure()
	}

	static void shipments() {
		client.shipments()
				.stopIfFailure()
	}

	static void shipment() {
		client.shipment()
				.stopIfFailure()
	}

	static void destination() {
		client.destination()
				.stopIfFailure()
	}

	static void shippingoption() {
		client.shippingoption()
				.stopIfFailure()
	}

	static void shipmentsElement() {
		shipments()
		client.element()
				.stopIfFailure()
	}

	static void paymentmeans() {
		client.paymentmeans()
				.stopIfFailure()
	}

	static shipmentLineItems() {
		shipments()
		client.element()
				.lineitems()
				.stopIfFailure()
	}

	static List getShipmentLineItemsNames() {
		def names = []

		client.body.links.findAll {
			if (it.rel == "element") {
				client.GET(it.href)
				names.add(client["name"])
			}
		}

		return names
	}

	static def getPaymentDisplayName() {
		paymentmeans()
		client.element()
				.stopIfFailure()
		return client["display-name"]
	}

	static void verifyPurchaseItemsBySkuCode(List<String> skuCodeList) {
		resume()
		LineItems.verifyLineItemsBySkuCode(skuCodeList)
	}

	static void verifyCoupon(def couponCode) {
		resume()
		coupons()
		boolean couponExists = false
		client.body.links.find {
			if (it.rel == "element") {
				client.GET(it.uri)
				if (couponCode == client["code"]) {
					return couponExists = true
				}
			}
		}
		assertThat(couponExists)
				.as("Unable to find coupon code: $couponCode")
				.isTrue()
	}

	static void selectCoupon(def couponCode) {
		verifyCoupon(couponCode)
	}

	static void verifyPurchaseItemsByProductName(List<String> prodNameList) {
		resume()
		LineItems.verifyLineItemsByName(prodNameList)
	}

	static void verifyPurchaseItemsByProductName(String productName) {
		List<String> prodNameList = new ArrayList<>()
		prodNameList.add(productName)
		verifyPurchaseItemsByProductName(prodNameList)
	}

	static void findPurchaseItemByProductName(String productName) {
		verifyPurchaseItemsByProductName(productName)
	}

	static void selectPaymentMeans(String paymentMeans) {
		paymentmeans()
		openLinkRelWithFieldWithValue("element", "display-name", paymentMeans)
	}

}