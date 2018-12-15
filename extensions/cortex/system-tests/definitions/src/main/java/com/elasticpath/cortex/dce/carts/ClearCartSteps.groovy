package com.elasticpath.cortex.dce.carts

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static org.assertj.core.api.Assertions.assertThat

import cucumber.api.java.en.Then

import com.elasticpath.cortex.dce.CommonAssertion
import com.elasticpath.cortexTestObjects.Cart
import com.elasticpath.cortexTestObjects.Order

class ClearCartSteps {


	@Then('the total quantity in the cart is (.+)$')
	static void verifyTotalQuantityInCart(int quantity) {
		Cart.getCart()
		assertThat(client.body.'total-quantity')
				.as("The cart total quantity is not as expected")
				.isEqualTo(quantity.toInteger())
	}

	@Then('^I clear the cart$')
	static void clearDefaultCart() {
		Cart.clearCart()
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(204)
	}

	@Then('^there are no lineitems in the cart$')
	static void verifyCartHasNoLineitems() {
		Cart.lineitems()
		assertThat(client.body.links[0].rel)
				.as("Cart lineitem links are not as expected")
				.isEqualTo("cart")
		assertThat(client.body.links[1])
				.as("Cart lineitem links are not as expected")
				.isEqualTo(null)
		Cart.getCart()
	}

	@Then('^I am forbidden from (?:.+)$')
	static void verifyForbidden() {
		assertThat(client.response.status)
				.as("HTTP response status is not as expected")
				.isEqualTo(403)
	}

	@Then('^the first shopper\'s cart is not cleared$')
	static void verifySavedShopperCartIsNotCleared() {
		Cart.getCart()    //clear the 403 status that causes test to exit
		client.authAsRegisteredUser()
		Cart.getCart()
		assertThat(client.body.'total-quantity')
				.as("The cart total quantity is not as expected")
				.isEqualTo(1)
	}

	@Then('^the cart total has amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyCartTotalHasValues(String amount, String currency, String display) {
		Cart.total()
		CommonAssertion.assertCost(client.body.cost[0], amount, currency, display)
	}

	@Then('^the order total has amount: (.+), currency: (.+) and display: (.+)$')
	static void verifyOrderTotalHasValues(String amount, String currency, String display) {
		Order.total()
		CommonAssertion.assertCost(client.body.cost[0], amount, currency, display)
	}
}
