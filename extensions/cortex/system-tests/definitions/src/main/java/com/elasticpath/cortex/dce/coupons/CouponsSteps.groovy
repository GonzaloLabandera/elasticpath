package com.elasticpath.cortex.dce.coupons

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE
import static com.elasticpath.rest.ws.assertions.RelosAssert.assertLinkDoesNotExist

import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import com.elasticpath.cortex.dce.CommonMethods
import com.elasticpath.cortexTestObjects.FindItemBy
import com.elasticpath.cortexTestObjects.Item
import com.elasticpath.cortexTestObjects.Order
import com.elasticpath.cortexTestObjects.Purchase

class CouponsSteps {

	@Given('^I have no coupons applied to an order$') 
	static void verifyNoCouponsOnOrder() {
		client.authAsAPublicUser(DEFAULT_SCOPE)
				.stopIfFailure()
	}

	@When('^I login to the registered shopper with email ID (.+)$')
	static void loginWithEmailID(String userEmailId) {
		client.authRegisteredUserByName(DEFAULT_SCOPE, userEmailId)
				.stopIfFailure()
	}

	@When('^I transition to the registered shopper with email ID (.+)$')
	static void transitionToRegisteredShopper(String userEmailId) {
		client.roleTransitionToRegisteredUserByName(DEFAULT_SCOPE, userEmailId)
				.stopIfFailure()
	}

	@And('^I create a purchase for item (.+) with saved coupon$')
	static void purchaseProductWithSavedCoupon(String productName) {
		CommonMethods.searchAndOpenItemWithKeyword(productName)
		client.addtocartform()
				.addtodefaultcartaction(quantity: 1)
				.follow()
				.stopIfFailure()
		Order.submitPurchase()
		client.stopIfFailure()
	}

//matches:
//Shopper applies a coupon that has not been previously applied to the order
//Shopper applies a coupon to the order
	@When('^I (?:apply|applied) a coupon code (.+?) (?:that has not been previously applied )*to the order$')
	static void applyCoupon(String couponCode) {
		Order.applyCoupon(couponCode)
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(201)
	}

	@When('^I re-apply the same coupon code (.+) to the same order$')
	static void reapplyCoupon(String couponCode) {
		Order.applyCoupon(couponCode)
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(200)
	}

	@When('^I apply an invalid coupon (.+) to the order')
	static void applyInvalidCoupon(String couponCode) {
		Order.applyCoupon(couponCode)

	}

	@Given('^a coupon code (.+) exists')
	static void verifyCouponExists(String couponCode) {}

	@Given('^shopper (.+) has an auto applied coupon (.+)')
	static void verifyShopperHasAutoAppliedCoupon(String customerId, String couponCode) {}

	@And('^I retrieve the coupon info for the (?:new )*order$')
	static void getOrderCoupon() {
		Order.couponinfo()
	}

	@Then('^there (?:is|are) exactly (.+) (?:coupon|coupons) applied to the (?:order|purchase)$')
	static void getNumberOfAppliedCoupons(int expectedNumberOfCoupons) {
		Order.couponinfo()

		if (expectedNumberOfCoupons == 0) {
			assertLinkDoesNotExist(client, "coupon")
		} else {
			def newNumberOfCoupons = client.body.links.findAll { link ->
				link.rel == "coupon"
			}.size()
			assertThat(newNumberOfCoupons)
					.as("Number of coupons is not as expected")
					.isEqualTo(expectedNumberOfCoupons.toInteger())
		}
	}

	@Then('^the coupon is not accepted')
	static void checkCouponNotAccepted() {
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(409)
	}
//matches:
//the COUPON is the one that was applied
//the COUPON is the one that was auto applied
	@And('^the coupon (.+) is (?:auto )*applied to the order$')
	static void verifyCouponAppliedToOrder(String couponCode) {
		Order.verifyAppliedCoupon(couponCode)
	}

	@Then('^the code of the coupon (.+) is displayed$')
	static void verifyCouponCodeDisplayed(String couponCode) {
		assertThat(client["code"])
				.as("Coupon code is not as expected")
				.isEqualTo(couponCode)
	}

	@When('^I remove the coupon (.+) from the order$')
	static void deleteCouponFromOrder(String couponCode) {
		Order.removeAppliedCoupon(couponCode)
	}

	@When('^I retrieve the coupon (.+) details of my order$')
	static void getCouponDetails(String couponCode) {
		def criteria = { coupon ->
			coupon["code"] == couponCode
		}
		Order.couponinfo()
		client.findLink("coupon", criteria)
				.stopIfFailure()
	}

	@When('^Shopper retrieves the coupon info of their purchase$')
	static void getPurchaseCoupons() {
		Purchase.coupons()
	}

	@When('^I retrieve the coupon (.+) details of my purchase$')
	static void getPurchaseCouponInfo(String couponCode) {
		Purchase.selectCoupon(couponCode)
	}

	@Given('^Shopper has coupon (.+) applied to their order$')
	static void applyCouponToOrder(String couponCode) {
		client.authAsAPublicUser(DEFAULT_SCOPE)
		Order.applyCoupon(couponCode)
	}

	@Given('^I have the product (.+) with coupon (.+) applied to my purchase$')
	static void purchaseProductWithCoupon(String productName, String couponCode) {

		client.authAsRegisteredUser()
				.stopIfFailure()

		Order.removeAllAppliedCoupons()

		// for a purchase to have a coupon it must trigger a promotion.
		FindItemBy.productName(productName)
		Item.addItemToCart(1)
		Order.applyCoupon(couponCode)
		Order.submitPurchase()
	}
}
