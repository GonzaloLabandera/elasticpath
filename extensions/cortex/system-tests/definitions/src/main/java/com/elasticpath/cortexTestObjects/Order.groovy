package com.elasticpath.cortexTestObjects

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.client
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN
import static com.elasticpath.cortex.dce.SharedConstants.TEST_TOKEN_DISPLAY_NAME
import static org.assertj.core.api.Assertions.assertThat

import com.elasticpath.cortex.dce.CommonMethods

/**
 * Order.
 */
class Order extends CommonMethods {

	static void getOrder() {
		client.GET("/")
				.defaultcart()
				.order()
		CortexResponse.orderResponse = client.save()
	}

	static void resume() {
		if (CortexResponse.orderResponse == null) {
			getOrder()
		}
		client.resume(CortexResponse.orderResponse)
	}

	static void purchaseform() {
		getOrder()
		client.purchaseform()
				.stopIfFailure()
	}

	static void total() {
		getOrder()
		client.total()
				.stopIfFailure()
	}

	static void couponinfo() {
		getOrder()
		client.couponinfo()
				.stopIfFailure()
	}

	static void billingaddressinfo() {
		getOrder()
		client.billingaddressinfo()
				.stopIfFailure()
	}

	static void billingAddress() {
		billingaddressinfo()
		client.billingaddress()
				.stopIfFailure()
	}

	static void deliveries() {
		getOrder()
		client.deliveries()
				.stopIfFailure()
	}

	static void destinationinfo() {
		deliveries()
		client.element()
				.destinationinfo()
				.stopIfFailure()
	}

	static void destination() {
		destinationinfo()
		client.destination()
				.stopIfFailure()
	}

	static void email() {
		getOrder()
		client.emailinfo()
				.email()
				.stopIfFailure()
	}

	static void tax() {
		getOrder()
		client.tax()
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
		purchaseform()
		client.submitorderaction()
				.stopIfFailure()
	}

	static void applyCoupon(def couponCode) {
		couponinfo()
		client.couponform()
				.applycouponaction(["code": couponCode])
	}

	static void verifyAppliedCoupon(def couponCode) {
		couponinfo()
		boolean couponExists = false
		client.body.links.find {
			if (it.rel == "coupon") {
				client.GET(it.uri)
				if (couponCode == client["code"]) {
					return couponExists = true
				}
			}
		}
		assertThat(couponExists)
				.as("Unable to find applied coupon code: $couponCode")
				.isTrue()
	}

	static void removeAppliedCoupon(def couponCode) {
		verifyAppliedCoupon(couponCode)
		client.DELETE(client.body.self.uri)
	}

	static void removeAllAppliedCoupons() {
		couponinfo()
		client.body.links.findAll {
			if (it.rel == "coupon") {
				client.GET(it.uri)
				client.DELETE(client.body.self.uri)
			}
		}
	}

	static void paymentmethodinfo() {
		getOrder()
		client.paymentmethodinfo()
				.stopIfFailure()
	}

	static void addToken(String displayName, String token) {
		paymentmethodinfo()
		client.paymenttokenform()
				.createpaymenttokenfororderaction(
				['display-name': displayName,
				 'token'       : token]
		)
				.follow()
				.stopIfFailure()
	}

	static void addDefaultToken() {
		addToken(TEST_TOKEN_DISPLAY_NAME, TEST_TOKEN)
	}

	static def getPaymentMethodDisplayName() {
		paymentmethodinfo()
		client.paymentmethod()
				.stopIfFailure()
		return client["display-name"]
	}

	static void selectShippingAddressByPostalCode(def postalCode) {
		CortexResponse.orderResponse = null
		def shippingAddressExists = false

		destinationinfo()
		client.selector()

		client.body.links.find {
			if (it.rel == "chosen" || it.rel == "choice") {
				client.GET(it.uri)
				def choiceResponse = client.save()
				client.description()
				if (client["address"]["postal-code"] == postalCode) {
					if (it.rel == "choice") {
						client.resume(choiceResponse)
						client.selectaction()
								.follow()
								.stopIfFailure()
					}
					client.resume(CortexResponse.getOrderResponse())
					return shippingAddressExists = true
				}
			}
		}
		assertThat(shippingAddressExists)
				.as("Unable to find address with postal code $postalCode")
				.isTrue()
	}

	static void selectAnyShippingAddress() {
		destinationinfo()
		client.selector()
				.choice()
				.selectaction()
				.stopIfFailure()

	}

	static void selectAnyBillingAddress() {
		getOrder()
		client.billingaddressinfo()
				.selector()
				.choice()
				.selectaction()
				.stopIfFailure()

	}

	static void shippingOptionSelector() {
		deliveries()
		client.element()
				.shippingoptioninfo()
				.selector()
				.stopIfFailure()
	}

	static void selectShippingServiceLevel(String name) {
		def serviceLevelExists = false
		shippingOptionSelector()

		client.body.links.find {
			if (it.rel == "chosen" || it.rel == "choice") {
				client.GET(it.uri)
				def choiceResponse = client.save()
				client.description()
				if (client["name"] == name) {
					if (it.rel == "choice") {
						client.resume(choiceResponse)
						client.selectaction()
								.follow()
								.stopIfFailure()
					}
					client.resume(CortexResponse.getOrderResponse())
					return serviceLevelExists = true
				}
			}
		}
		assertThat(serviceLevelExists)
				.as("Unable to find shipping service level $name")
				.isTrue()
	}

	static getShippingServiceLevel(String name) {
		boolean serviceLevelExists = false
		shippingOptionSelector()

		client.body.links.find {
			if (it.rel == "chosen" || it.rel == "choice") {
				client.GET(it.uri)
				client.description()
				if (client["name"] == name) {
					return serviceLevelExists = true
				}
			}
		}
		assertThat(serviceLevelExists)
				.as("Unable to find shipping service level $name")
				.isTrue()
	}

	static void chosenShippingOptionDescription() {
		shippingOptionSelector()
		client.chosen()
				.description()
				.stopIfFailure()
	}

	static void choiceShippingOptionDescription() {
		shippingOptionSelector()
		client.chosen()
				.description()
				.stopIfFailure()
	}

	static def getChosenShippingOptionDisplayName() {
		chosenShippingOptionDescription()
		return getDisplayName()
	}

	static void paymentMethodSelector() {
		paymentmethodinfo()
		client.selector()
				.stopIfFailure()
	}

	static void chosenPaymentMethod() {
		paymentMethodSelector()
		client.chosen()
				.stopIfFailure()
	}

	static void chosenPaymentMethodDescription() {
		chosenPaymentMethod()
		client.description()
				.stopIfFailure()
	}

	static def getChosenPaymentMethodDisplayName() {
		chosenPaymentMethodDescription()
		return getDisplayName()
	}

	static void paymentMethodChoiceDescription() {
		paymentMethodSelector()
		client.choice()
				.description()
				.stopIfFailure()
	}

	static void chooseAnyPaymentMethod() {
		paymentMethodSelector()
		client.choice()
				.selectaction()
				.follow()
	}

	static def getChoicePaymentMethodDisplayName() {
		paymentMethodChoiceDescription()
		return getDisplayName()
	}

}